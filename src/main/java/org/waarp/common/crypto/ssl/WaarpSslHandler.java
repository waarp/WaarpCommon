/**
 * This file is part of Waarp Project.
 * 
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author tags. See the
 * COPYRIGHT.txt in the distribution for a full listing of individual contributors.
 * 
 * All Waarp Project is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Waarp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Waarp . If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.waarp.common.crypto.ssl;

import javax.net.ssl.SSLEngine;

import org.waarp.common.future.WaarpFuture;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.SslHandler;

/**
 * @author "Frederic Bregier"
 *
 */
public class WaarpSslHandler extends SslHandler {
    private WaarpFuture ready = new WaarpFuture(true);
    
    public WaarpSslHandler(SSLEngine engine) {
        super(engine);
    }

    public WaarpSslHandler(SSLEngine engine, boolean startTls) {
        super(engine, startTls);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        ready.setSuccess();
    }

    public void waitReady() {
        ready.awaitUninterruptibly(this.getHandshakeTimeoutMillis());
    }
    public WaarpFuture getFutureReady() {
        return ready;
    }
}
