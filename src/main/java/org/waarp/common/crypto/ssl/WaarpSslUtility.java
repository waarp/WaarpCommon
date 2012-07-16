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

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.ssl.SslHandler;

/**
 * Utilities for SSL support
 * 
 * @author "Frederic Bregier"
 *
 */
public class WaarpSslUtility {
	/**
	 * Utility method to close a channel in SSL mode correctly (if any)
	 * @param channel
	 */
	public static void closingSslChannel(Channel channel) {
		ChannelHandler handler = channel.getPipeline().getFirst();
		if (handler instanceof SslHandler) {
			SslHandler sslHandler = (SslHandler) handler;
			try {
				sslHandler.close().await();
			} catch (InterruptedException e) {
			}
		}
		Channels.close(channel);
	}
	
	/**
	 * Remove the SslHandler (if any) cleanly
	 * @param channel
	 */
	public static void removingSslHandler(Channel channel) {
		channel.setReadable(true);
		ChannelHandler handler = channel.getPipeline().getFirst();
		if (handler instanceof SslHandler) {
			SslHandler sslHandler = (SslHandler) handler;
			try {
				sslHandler.close().await();
			} catch (InterruptedException e) {
			}
		}
		channel.setReadable(false);
		channel.getPipeline().removeFirst();
		channel.setReadable(true);
	}
}
