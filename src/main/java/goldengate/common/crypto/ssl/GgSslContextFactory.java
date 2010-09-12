/**
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author
 * tags. See the COPYRIGHT.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3.0 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package goldengate.common.crypto.ssl;

import java.security.Security;
import java.util.concurrent.ExecutorService;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.jboss.netty.handler.ssl.SslHandler;

import goldengate.common.logging.GgInternalLogger;
import goldengate.common.logging.GgInternalLoggerFactory;

/**
 * SSL ContextFactory for Netty.<br>
 * <br>
 * Usage:<br>
 * <ul><li>First initiate the SecureKeyStore (only once):<br>
 *      GgSecureKeyStore.init(
 *      keyStoreFilename, _keyStorePasswd, _keyPassword,
 *      trustStoreFilename, _trustStorePasswd);</li>
 * <li>Then once initialized, use it within the PipelineFactory:<br>
 *      pipeline.addLast("ssl",
 *              GgSslContextFactory.initPipelineFactory(serverMode,
 *              clientAuthenticated, executorService));</li>
 * </ul>
 *
 * @author Frederic Bregier
 *
 */
public class GgSslContextFactory {
    /**
     * Internal Logger
     */
    private static final GgInternalLogger logger = GgInternalLoggerFactory
            .getLogger(GgSslContextFactory.class);

    /**
    *
    */
    private static final String PROTOCOL = "TLS";

    /**
    *
    */
    private static final SSLContext SERVER_CONTEXT;

    /**
    *
    */
    private static final SSLContext CLIENT_CONTEXT;

    static {
        String algorithm = Security
                .getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }

        SSLContext serverContext = null;
        SSLContext clientContext = null;
        try {
            // Initialize the SSLContext to work with our key managers.
            serverContext = SSLContext.getInstance(PROTOCOL);
            serverContext.init(GgSecureKeyStore.keyManagerFactory
                    .getKeyManagers(), GgSecureTrustManagerFactory
                    .getTrustManagers(), null);
        } catch (Exception e) {
            logger.error("Failed to initialize the server-side SSLContext", e);
            throw new Error("Failed to initialize the server-side SSLContext",
                    e);
        }

        try {
            clientContext = SSLContext.getInstance(PROTOCOL);
            clientContext.init(GgSecureKeyStore.keyManagerFactory
                    .getKeyManagers(), GgSecureTrustManagerFactory
                    .getTrustManagers(), null);
        } catch (Exception e) {
            logger.error("Failed to initialize the client-side SSLContext", e);
            throw new Error("Failed to initialize the client-side SSLContext",
                    e);
        }

        SERVER_CONTEXT = serverContext;
        CLIENT_CONTEXT = clientContext;
    }

    /**
     * @return the Server Context
     */
    public static SSLContext getServerContext() {
        return SERVER_CONTEXT;
    }

    /**
     * @return the Client Context
     */
    public static SSLContext getClientContext() {
        return CLIENT_CONTEXT;
    }
    /**
     * To be called before adding as first entry in the PipelineFactory as<br>
     * pipeline.addLast("ssl", sslhandler);<br>
     *
     * @param serverMode True if in Server Mode, else False in Client mode
     * @param needClientAuth True if the client needs to be authenticated (only if serverMode is True)
     * @param executorService if not Null, gives a specific executorService
     * @return the sslhandler
     */
    public static SslHandler initPipelineFactory(boolean serverMode,
            boolean needClientAuth, ExecutorService executorService) {
        // Add SSL handler first to encrypt and decrypt everything.
        SSLEngine engine;
        if (serverMode) {
            engine = getClientContext().createSSLEngine();
            engine.setUseClientMode(false);
            engine.setNeedClientAuth(needClientAuth);
        } else {
            engine = getServerContext().createSSLEngine();
            engine.setUseClientMode(true);
        }
        if (executorService != null) {
            return new SslHandler(engine, executorService);
        } else {
            return new SslHandler(engine);
        }
    }
}
