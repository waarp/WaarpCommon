/**
   This file is part of GoldenGate Project (named also GoldenGate or GG).

   Copyright 2009, Frederic Bregier, and individual contributors by the @author
   tags. See the COPYRIGHT.txt in the distribution for a full listing of
   individual contributors.

   All GoldenGate Project is free software: you can redistribute it and/or 
   modify it under the terms of the GNU General Public License as published 
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   GoldenGate is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with GoldenGate .  If not, see <http://www.gnu.org/licenses/>.
 */
package goldengate.common.logging;

import org.jboss.netty.logging.InternalLoggerFactory;

/**
 * Based on the Netty InternalLoggerFactory Based on The Netty Project
 * (netty-dev@lists.jboss.org)
 *
 * @author Trustin Lee (tlee@redhat.com)
 * @author Frederic Bregier
 *
 */
public abstract class GgInternalLoggerFactory extends
        org.jboss.netty.logging.InternalLoggerFactory {
    /**
     *
     * @param clazz
     * @return the GgInternalLogger
     */
    public static GgInternalLogger getLogger(Class<?> clazz) {
        InternalLoggerFactory factory = getDefaultFactory();
        if (factory instanceof GgInternalLoggerFactory) {
            return (GgInternalLogger) factory.newInstance(clazz.getName());
        } else {
            // Should be set first so default = JDK support
            InternalLoggerFactory.setDefaultFactory(new GgJdkLoggerFactory(null));
            return (GgInternalLogger) getDefaultFactory().newInstance(
                clazz.getName());
        }
    }

}
