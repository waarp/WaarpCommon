/**
 * Copyright 2009, Frederic Bregier, and individual contributors
 * by the @author tags. See the COPYRIGHT.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package goldengate.common.crypto.ssl;

import goldengate.common.exception.CryptoException;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManagerFactorySpi;

/**
 * A SecureTrustManagerFactory
 * @author Frederic Bregier
 *
 */
public class GgSecureTrustManagerFactory extends TrustManagerFactorySpi {
    private GgX509TrustManager ggTrustManager;
    private TrustManager[] trustManager;
    private boolean needAuthentication = false;
    /**
     * Accept all connections
     * @throws CryptoException
     */
    public GgSecureTrustManagerFactory() throws CryptoException {
        ggTrustManager = new GgX509TrustManager();
        trustManager = new TrustManager[] {ggTrustManager};
        needAuthentication = false;
    }
    /**
    *
    * @param tmf
    * @throws CryptoException
    */
   public GgSecureTrustManagerFactory(TrustManagerFactory tmf) throws CryptoException {
       ggTrustManager = new GgX509TrustManager(tmf);
       trustManager = new TrustManager[] {ggTrustManager};
       needAuthentication = true;
   }

   /**
    *
    * @return True if this TrustManager really check authentication
    */
   public boolean needAuthentication() {
       return needAuthentication;
   }
   public TrustManager[] getTrustManagers() {
       return trustManager;
   }
    /* (non-Javadoc)
     * @see javax.net.ssl.TrustManagerFactorySpi#engineGetTrustManagers()
     */
    @Override
    protected TrustManager[] engineGetTrustManagers() {
        return getTrustManagers();
    }

    /* (non-Javadoc)
     * @see javax.net.ssl.TrustManagerFactorySpi#engineInit(java.security.KeyStore)
     */
    @Override
    protected void engineInit(KeyStore arg0) throws KeyStoreException {
        // Unused
    }

    /* (non-Javadoc)
     * @see javax.net.ssl.TrustManagerFactorySpi#engineInit(javax.net.ssl.ManagerFactoryParameters)
     */
    @Override
    protected void engineInit(ManagerFactoryParameters arg0)
            throws InvalidAlgorithmParameterException {
        // Unused
    }

}
