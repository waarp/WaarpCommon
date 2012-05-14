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

import ch.qos.logback.classic.Logger;

/**
 * Example of logger using SLF4J from LOGBACK
 *
 * @author Frederic Bregier
 *
 */
public class GgSlf4JLogger extends GgInternalLogger {
    /**
     * Internal logger
     */
    private final Logger logger;

    /**
     *
     * @param logger
     */
    public GgSlf4JLogger(org.slf4j.Logger logger) {
        super();
        this.logger = (Logger) logger;
    }

    /*
     * (non-Javadoc)
     *
     * @see goldengate.common.logging.GgInternalLogger#debug(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void debug(String format, String arg1, String arg2) {
        if (logger.isDebugEnabled())
            logger.debug(getLoggerMethodAndLine()+format, arg1, arg2);
    }

    /*
     * (non-Javadoc)
     *
     * @see goldengate.common.logging.GgInternalLogger#debug(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void debug(String format, String arg1) {
        if (logger.isDebugEnabled())
            logger.debug(getLoggerMethodAndLine()+format, arg1);
    }

    /*
     * (non-Javadoc)
     *
     * @see goldengate.common.logging.GgInternalLogger#error(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void error(String format, String arg1, String arg2) {
        logger.error(getLoggerMethodAndLine()+format, arg1, arg2);
    }

    /*
     * (non-Javadoc)
     *
     * @see goldengate.common.logging.GgInternalLogger#error(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void error(String format, String arg1) {
        logger.error(getLoggerMethodAndLine()+format, arg1);
    }

    /*
     * (non-Javadoc)
     *
     * @see goldengate.common.logging.GgInternalLogger#info(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void info(String format, String arg1, String arg2) {
        if (logger.isInfoEnabled())
            logger.info(getLoggerMethodAndLine()+format, arg1, arg2);
    }

    /*
     * (non-Javadoc)
     *
     * @see goldengate.common.logging.GgInternalLogger#info(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void info(String format, String arg1) {
        if (logger.isInfoEnabled())
            logger.info(getLoggerMethodAndLine()+format, arg1);
    }

    /*
     * (non-Javadoc)
     *
     * @see goldengate.common.logging.GgInternalLogger#warn(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void warn(String format, String arg1, String arg2) {
        logger.warn(getLoggerMethodAndLine()+format, arg1, arg2);
    }

    /*
     * (non-Javadoc)
     *
     * @see goldengate.common.logging.GgInternalLogger#warn(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void warn(String format, String arg1) {
        logger.warn(getLoggerMethodAndLine()+format, arg1);
    }

    // original form
    public void debug(String msg) {
        if (logger.isDebugEnabled())
            logger.debug(getLoggerMethodAndLine()+msg);
    }

    public void debug(String msg, Throwable cause) {
        if (logger.isDebugEnabled())
            logger.debug(getLoggerMethodAndLine()+msg, cause);
    }

    public void error(String msg) {
        logger.error(getLoggerMethodAndLine()+msg);
    }

    public void error(String msg, Throwable cause) {
        logger.error(getLoggerMethodAndLine()+msg, cause);
    }

    public void info(String msg) {
        if (logger.isInfoEnabled())
            logger.info(getLoggerMethodAndLine()+msg);
    }

    public void info(String msg, Throwable cause) {
        if (logger.isInfoEnabled())
            logger.info(getLoggerMethodAndLine()+msg, cause);
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    public void warn(String msg) {
        logger.warn(getLoggerMethodAndLine()+msg);
    }

    public void warn(String msg, Throwable cause) {
        logger.warn(getLoggerMethodAndLine()+msg, cause);
    }

    @Override
    public String toString() {
        return String.valueOf(logger.getName());
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (logger.isDebugEnabled())
            logger.debug(getLoggerMethodAndLine()+format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object arg1) {
        if (logger.isDebugEnabled())
            logger.debug(getLoggerMethodAndLine()+format, arg1);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        logger.error(getLoggerMethodAndLine()+format, arg1, arg2);
    }

    @Override
    public void error(String format, Object arg1) {
        logger.error(getLoggerMethodAndLine()+format, arg1);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (logger.isInfoEnabled())
            logger.info(getLoggerMethodAndLine()+format, arg1, arg2);
    }

    @Override
    public void info(String format, Object arg1) {
        if (logger.isInfoEnabled())
            logger.info(getLoggerMethodAndLine()+format, arg1);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        logger.warn(getLoggerMethodAndLine()+format, arg1, arg2);
    }

    @Override
    public void warn(String format, Object arg1) {
        logger.warn(getLoggerMethodAndLine()+format, arg1);
    }
}
