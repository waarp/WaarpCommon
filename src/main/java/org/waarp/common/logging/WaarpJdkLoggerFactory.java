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
package org.waarp.common.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.JdkLoggerFactory;

/**
 * Logger factory which creates a <a href=
 * "http://java.sun.com/javase/6/docs/technotes/guides/logging/index.html" >java.util.logging</a>
 * logger.
 * 
 * Based on The Netty Project (netty-dev@lists.jboss.org)
 * 
 * @author Trustin Lee (tlee@redhat.com)
 * @author Frederic Bregier
 */
public class WaarpJdkLoggerFactory extends JdkLoggerFactory implements WaarpInternalLoggerInterface {
	/**
	 * 
	 * @param level
	 */
	public WaarpJdkLoggerFactory(Level level) {
		super();

		Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		if (level == null) {
			logger.info("Default level: " + logger.getLevel());
		} else {
			logger.setLevel(level);
		}
	}

	@Override
	public InternalLogger newInstance(String name) {
		final Logger logger = Logger.getLogger(name);
		return new WaarpJdkLogger(logger, name);
	}
}
