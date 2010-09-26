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
 * <ul><li>First create the SecureKeyStore (only once):<br>
 *      new GgSecureKeyStore(
 *      keyStoreFilename, _keyStorePasswd, _keyPassword,
 *      trustStoreFilename, _trustStorePasswd);</li>
 * <li>Create the GgSslContextFactory (only once)<br>
 * new GgSslContextFactory(ggSecureKeyStore);</li>
 * <li>Then once initialized, use it within the PipelineFactory:<br>
 *      pipeline.addLast("ssl",
 *              ggSslContextFactory.initPipelineFactory(serverMode,
 *              true, executorService));</li>
 * </ul>
 * <br>
 * If no authorization is needed, then Usage is:<br>
 * <ul><li>First create the SecureKeyStore (only once):<br>
 *      new GgSecureKeyStore(
 *      keyStoreFilename, _keyStorePasswd, _keyPassword);</li>
 * <li>Create the GgSslContextFactory (only once)<br>
 * new GgSslContextFactory(ggSecureKeyStore);</li>
 * <li>Then once initialized, use it within the PipelineFactory:<br>
 *      pipeline.addLast("ssl",
 *              ggSslContextFactory.initPipelineFactory(serverMode,
 *              false, executorService));</li>
 * </ul>
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
    private final SSLContext SERVER_CONTEXT;

    /**
    *
    */
    private final SSLContext CLIENT_CONTEXT;

    private boolean hasTrustStore = false;

    public GgSslContextFactory(GgSecureKeyStore ggSecureKeyStore, boolean serverMode) {
        String algorithm = Security
                .getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }

        SSLContext serverContext = null;
        SSLContext clientContext = null;
        if (serverMode) {
            try {
                // Initialize the SSLContext to work with our key managers.
                serverContext = SSLContext.getInstance(PROTOCOL);
                GgSecureTrustManagerFactory secureTrustManagerFactory =
                    ggSecureKeyStore.getSecureTrustManagerFactory();
                if (secureTrustManagerFactory == null) {
                    logger.debug("No TrustManager");
                    serverContext.init(ggSecureKeyStore.getKeyManagerFactory().getKeyManagers(),
                            null, null);
                } else {
                    logger.debug("Has TrustManager");
                    hasTrustStore = true;
                    serverContext.init(ggSecureKeyStore.getKeyManagerFactory().getKeyManagers(),
                        secureTrustManagerFactory.getTrustManagers(), null);
                }
            } catch (Exception e) {
                logger.error("Failed to initialize the server-side SSLContext", e);
                throw new Error("Failed to initialize the server-side SSLContext",
                        e);
            }
        } else {
            try {
                clientContext = SSLContext.getInstance(PROTOCOL);
                GgSecureTrustManagerFactory secureTrustManagerFactory =
                    ggSecureKeyStore.getSecureTrustManagerFactory();
                if (secureTrustManagerFactory == null) {
                    logger.debug("No TrustManager");
                    clientContext.init(null,
                          //ggSecureKeyStore.getKeyManagerFactory().getKeyManagers(),
                            null, null);
                } else {
                    logger.debug("Has TrustManager");
                    hasTrustStore = true;
                    clientContext.init(null,
                            //ggSecureKeyStore.getKeyManagerFactory().getKeyManagers(),
                            secureTrustManagerFactory.getTrustManagers(), null);
                }
            } catch (Exception e) {
                logger.error("Failed to initialize the client-side SSLContext", e);
                throw new Error("Failed to initialize the client-side SSLContext",
                        e);
            }
        }
        SERVER_CONTEXT = serverContext;
        CLIENT_CONTEXT = clientContext;
    }

    /**
     * @return the Server Context
     */
    public SSLContext getServerContext() {
        return SERVER_CONTEXT;
    }

    /**
     * @return the Client Context
     */
    public SSLContext getClientContext() {
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
    public SslHandler initPipelineFactory(boolean serverMode,
            boolean needClientAuth, ExecutorService executorService) {
        // Add SSL handler first to encrypt and decrypt everything.
        SSLEngine engine;
        logger.debug("Has TrustManager? "+needClientAuth+" Is ServerMode? "+serverMode);
        if (serverMode) {
            engine = getServerContext().createSSLEngine();
            engine.setUseClientMode(false);
            engine.setNeedClientAuth(needClientAuth);
        } else {
            engine = getClientContext().createSSLEngine();
            engine.setUseClientMode(true);
        }
        if (executorService != null) {
            return new SslHandler(engine, executorService);
        } else {
            return new SslHandler(engine);
        }
    }
    /**
     *
     * @return True if the associated KeyStore has a TrustStore
     */
    public boolean hasTrustStore() {
        return hasTrustStore;
    }
}
