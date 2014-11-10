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
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.SucceededChannelFuture;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.handler.ssl.SslHandler;
import org.waarp.common.future.WaarpFuture;
import org.waarp.common.logging.WaarpInternalLogger;
import org.waarp.common.logging.WaarpInternalLoggerFactory;

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
    private static final WaarpInternalLogger logger = WaarpInternalLoggerFactory.getLogger(WaarpSslUtility.class);

    /**
     * Waiter for SSL handshake is finished
     */
    private static final ConcurrentHashMap<Integer, WaarpFuture> waitForSsl = new ConcurrentHashMap<Integer, WaarpFuture>();
    /**
     * ChannelGroup for SSL
     */
    private static final ChannelGroup sslChannelGroup = new DefaultChannelGroup("SslChannelGroup");

    /**
     * Remover from SSL HashMap
     */
    private static final ChannelFutureListener remover = new ChannelFutureListener() {
        public void operationComplete(ChannelFuture future) {
            logger.debug("SSL remover");
            waitForSsl.remove(future.getChannel().getId());
        }
    };

    /**
     * Add the Channel as SSL handshake will start soon
     * 
     * @param channel
     */
    public static void addSslOpenedChannel(Channel channel) {
        WaarpFuture futureSSL = new WaarpFuture(true);
        waitForSsl.put(channel.getId(), futureSSL);
        sslChannelGroup.add(channel);
        channel.getCloseFuture().addListener(remover);
    }

    /**
     * Set the future of SSL handshake to status
     * 
     * @param channel
     * @param status
     */
    public static void setStatusSslConnectedChannel(Channel channel,
            boolean status) {
        WaarpFuture futureSSL = waitForSsl.get(channel.getId());
        logger.debug("Set Ssl channel with status: " + status + ":" + (futureSSL != null));
        if (futureSSL != null) {
            if (status) {
                futureSSL.setSuccess();
            } else {
                futureSSL.cancel();
            }
        }
    }

    /**
     * Run the handshake on the given channel
     * 
     * @param channel
     * @return True if the Handshake is done correctly
     */
    public static boolean runHandshake(Channel channel) {
        final ChannelHandler handler = channel.getPipeline().getFirst();
        if (handler instanceof SslHandler) {
            logger.debug("Start handshake SSL");
            final SslHandler sslHandler = (SslHandler) handler;
            // Get the SslHandler and begin handshake ASAP.
            // Get notified when SSL handshake is done.
            ChannelFuture handshakeFuture;
            handshakeFuture = sslHandler.handshake();
            try {
                handshakeFuture.await();
            } catch (InterruptedException e1) {}
            logger.debug("Handshake: " + handshakeFuture.isSuccess(), handshakeFuture.getCause());
            if (!handshakeFuture.isSuccess()) {
                handshakeFuture.getChannel().close();
                WaarpSslUtility.setStatusSslConnectedChannel(channel, false);
                return false;
            }
            WaarpSslUtility.setStatusSslConnectedChannel(channel, true);
            return true;
        } else {
            logger.error("SSL Not found but connected");
            WaarpSslUtility.setStatusSslConnectedChannel(channel, true);
            return false;
        }
    }

    /**
     * 
     * @param channel
     * @return the associated WaarpFuture that validate the handshake
     */
    public static WaarpFuture getFutureSslHandshake(Channel channel) {
        return waitForSsl.get(channel.getId());
    }

    /**
     * Waiting for the channel to be opened and ready (Client side) (blocking call)
     * 
     * @param future
     * @return the channel if correctly associated, else return null
     */
    public static Channel waitforChannelReady(ChannelFuture future) {
        // Wait until the connection attempt succeeds or fails.
        try {
            future.await();
        } catch (InterruptedException e1) {}
        if (!future.isSuccess()) {
            logger.error("Channel not connected", future.getCause());
            return null;
        }
        Channel channel = future.getChannel();
        WaarpFuture sslFuture = getFutureSslHandshake(channel);
        if (sslFuture != null) {
            try {
                sslFuture.await();
            } catch (InterruptedException e) {}
            if (!sslFuture.isSuccess()) {
                logger.error("Channel not handshake done", future.getCause());
                return null;
            }
        }
        return channel;
    }

    /**
     * Utility to force all channels to be closed
     */
    public static void forceCloseAllSslChannels() {
        for (Channel channel : sslChannelGroup) {
            closingSslChannel(channel);
        }
        sslChannelGroup.close();
    }

    /**
     * Utility method to close a channel in SSL mode correctly (if any)
     * 
     * @param channel
     */
    public static ChannelFuture closingSslChannel(Channel channel) {
        if (channel.isConnected()) {
            ChannelHandler handler = channel.getPipeline().getFirst();
            if (handler instanceof SslHandler) {
                SslHandler sslHandler = (SslHandler) handler;
                logger.debug("Found SslHandler and wait for Ssl.close()");
                try {
                    sslHandler.close().await();
                } catch (InterruptedException e) {}
                channel.getPipeline().removeFirst();
            }
            logger.debug("Close the channel and returns the ChannelFuture");
            return Channels.close(channel);
        }
        logger.debug("Already closed");
        return new SucceededChannelFuture(channel);
    }

    /**
     * Remove the SslHandler (if any) cleanly
     * 
     * @param channel
     */
    public static void removingSslHandler(Channel channel) {
        if (channel.isConnected()) {
            channel.setReadable(true);
            ChannelHandler handler = channel.getPipeline().getFirst();
            if (handler instanceof SslHandler) {
                SslHandler sslHandler = (SslHandler) handler;
                try {
                    sslHandler.close().await();
                } catch (InterruptedException e) {}
                channel.setReadable(false);
                channel.getPipeline().removeFirst();
                channel.setReadable(true);
            }
        }
    }

    /**
     * Thread used to ensure we are not in IO thread when waiting
     * 
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
            this.setName("SSLTHREAD_" + this.getName());
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
            if (future.getChannel().isConnected()) {
                SSLTHREAD thread = new SSLTHREAD(future.getChannel());
                thread.start();
            }
        }
    };

    /**
     * Wait for the channel with SSL to be closed
     * 
     * @param channel
     * @param delay
     */
    public static boolean waitForClosingSslChannel(Channel channel, long delay) {
        try {
            if (!channel.getCloseFuture().await(delay)) {
                try {
                    channel.getPipeline().remove(SslHandler.class);
                    logger.debug("try to close anyway");
                    channel.close().await();
                    return false;
                } catch (NoSuchElementException e) {
                    // ignore;
                    channel.getCloseFuture().await();
                }
            }
        } catch (InterruptedException e) {}
        return true;
    }

}
