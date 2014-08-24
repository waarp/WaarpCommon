/**
   This file is part of Waarp Project.

   Copyright 2009, Frederic Bregier, and individual contributors by the @author
   tags. See the COPYRIGHT.txt in the distribution for a full listing of
   individual contributors.

   All Waarp Project is free software: you can redistribute it and/or 
   modify it under the terms of the GNU General Public License as published 
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Waarp is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Waarp .  If not, see <http://www.gnu.org/licenses/>.
 */
package org.waarp.common.crypto.ssl;

import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;

import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;

/**
 * Utilities for SSL support
 * 
 * @author "Frederic Bregier"
 *
 */
public class WaarpSslUtility {
    /**
     * Internal Logger
     */
    private static final WaarpLogger logger = WaarpLoggerFactory
            .getLogger(WaarpSslUtility.class);
    
    /**
     * EventExecutor associated with Ssl utility
     */
    private static final EventExecutor SSL_EVENT_EXECUTOR = new DefaultEventExecutor();
    /**
     * ChannelGroup for SSL
     */
    private static final ChannelGroup sslChannelGroup = new DefaultChannelGroup("SslChannelGroup", SSL_EVENT_EXECUTOR);

    /**
     * Add the Channel as SSL handshake will start soon
     * 
     * @param channel
     */
    public static void addSslOpenedChannel(Channel channel) {
        sslChannelGroup.add(channel);
    }
    
    /**
     * Wait for the handshake on the given channel
     * @param channel
     * @return True if the Handshake is done correctly
     */
    public static boolean waitForHandshake(Channel channel) {
		final ChannelHandler handler = channel.pipeline().first();
		if (handler instanceof SslHandler) {
			logger.debug("Start handshake SSL");
			final SslHandler sslHandler = (SslHandler) handler;
			// Get the SslHandler and begin handshake ASAP.
			// Get notified when SSL handshake is done.
			Future<Channel> handshakeFuture;
			handshakeFuture = sslHandler.handshakeFuture();
			try {
				handshakeFuture.await();
			} catch (InterruptedException e1) {
			}
			logger.debug("Handshake: " + handshakeFuture.isSuccess(), handshakeFuture.cause());
			if (!handshakeFuture.isSuccess()) {
				try {
                    handshakeFuture.get().close();
                } catch (InterruptedException e) {
                    logger.warn(e);
                } catch (ExecutionException e) {
                    logger.warn(e);
                }
				return false;
			}
			return true;
		} else {
			logger.error("SSL Not found but connected: "+handler.getClass().getName());
			return false;
		}
    }

    /**
     * Waiting for the channel to be opened and ready (Client side) (blocking call)
     * @param future
     * @return the channel if correctly associated, else return null
     */
    public static Channel waitforChannelReady(ChannelFuture future) {
    	// Wait until the connection attempt succeeds or fails.
    	try {
			future.await();
		} catch (InterruptedException e1) {
		}
        if (!future.isSuccess()) {
        	logger.error("Channel not connected", future.cause());
            return null;
        }
        if (waitForHandshake(future.channel())) {
            return future.channel();
        }
        return null;
    }
    
    /**
     * Utility to force all channels to be closed
     */
    public static void forceCloseAllSslChannels() {
    	for (Channel channel : sslChannelGroup) {
    		closingSslChannel(channel);
    	}
    	sslChannelGroup.close();
    	SSL_EVENT_EXECUTOR.shutdownGracefully();
    }
	/**
	 * Utility method to close a channel in SSL mode correctly (if any)
	 * @param channel
	 */
	public static ChannelFuture closingSslChannel(Channel channel) {
		if (channel.isActive()) {
			ChannelHandler handler = channel.pipeline().first();
			if (handler instanceof SslHandler) {
				SslHandler sslHandler = (SslHandler) handler;
				logger.debug("Found SslHandler and wait for Ssl.close()");
				try {
					sslHandler.close().await();
				} catch (InterruptedException e) {
				}
				channel.pipeline().removeFirst();
			}
			logger.debug("Close the channel and returns the ChannelFuture");
			return channel.close();
		}
		logger.debug("Already closed");
		return channel.newSucceededFuture();
	}
	
	/**
	 * Remove the SslHandler (if any) cleanly
	 * @param channel
	 */
	public static void removingSslHandler(Channel channel) {
		if (channel.isActive()) {
			channel.config().setAutoRead(true);
			ChannelHandler handler = channel.pipeline().first();
			if (handler instanceof SslHandler) {
				SslHandler sslHandler = (SslHandler) handler;
				try {
					sslHandler.close().await();
				} catch (InterruptedException e) {
				}
				channel.config().setAutoRead(false);
				channel.pipeline().removeFirst();
				channel.config().setAutoRead(true);
			}
		}
	}
	

	/**
	 * Thread used to ensure we are not in IO thread when waiting
	 * @author "Frederic Bregier"
	 *
	 */
	private static class SSLTHREAD extends Thread {
		private final Channel channel;
		
		/**
		 * @param channel
		 */
		private SSLTHREAD(Channel channel) {
			this.channel = channel;
			this.setDaemon(true);
			this.setName("SSLTHREAD_"+this.getName());
		}


		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			closingSslChannel(channel);
		}
		
	}
	/**
	 * Closing channel with SSL close at first step
	 */
	public static ChannelFutureListener SSLCLOSE = new ChannelFutureListener() {
		
		public void operationComplete(ChannelFuture future) throws Exception {
			if (future.channel().isActive()) {
				SSLTHREAD thread = new SSLTHREAD(future.channel());
				thread.start();
			}
		}
	};
	
	/**
	 * Wait for the channel with SSL to be closed
	 * @param channel
	 * @param delay
	 */
	public static boolean waitForClosingSslChannel(Channel channel, long delay) {
		try {
			if (!channel.closeFuture().await(delay)) {
				try{
					channel.pipeline().remove(SslHandler.class);
					logger.debug("try to close anyway");
					channel.close().await();
					return false;
				} catch (NoSuchElementException e) {
					// ignore;
					channel.closeFuture().await();
				}
			}
		} catch (InterruptedException e) {
		}
		return true;
	}
	
}
