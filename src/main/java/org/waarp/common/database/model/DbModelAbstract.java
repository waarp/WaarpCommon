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
 * You should have received a copy of the GNU General Public License along with Waarp. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.waarp.common.database.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.ConcurrentModificationException;

import org.waarp.common.database.DbAdmin;
import org.waarp.common.database.DbConstant;
import org.waarp.common.database.DbSession;
import org.waarp.common.database.exception.WaarpDatabaseNoConnectionException;
import org.waarp.common.database.exception.WaarpDatabaseSqlException;
import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;

/**
 * This Abstract class regroups common methods for all implementation classes.
 * 
 * @author Frederic Bregier
 * 
 */
public abstract class DbModelAbstract implements DbModel {
    /**
     * Internal Logger
     */
    private static final WaarpLogger logger = WaarpLoggerFactory
            .getLogger(DbModelAbstract.class);

    /**
     * Recreate the disActive session
     * 
     * @param dbSession
     * @throws WaarpDatabaseNoConnectionException
     */
    private void recreateSession(DbSession dbSession)
            throws WaarpDatabaseNoConnectionException {
        dbSession.renewConnection();
    }

    /**
     * Internal use for closing connection while validating it
     * 
     * @param dbSession
     */
    protected void closeInternalConnection(DbSession dbSession) {
        try {
            if (dbSession.getConn() != null) {
                dbSession.getConn().close();
            }
        } catch (SQLException e1) {
        } catch (ConcurrentModificationException e) {
        }
        dbSession.setDisActive(true);
        if (dbSession.getAdmin() != null)
            dbSession.getAdmin().setActive(false);
        DbAdmin.removeConnection(dbSession.getInternalId());
    }

    public String getValidationQuery(){
         return "select 1";
    }

    public void validConnection(DbSession dbSession)
            throws WaarpDatabaseNoConnectionException {
        // try to limit the number of check!
        synchronized (dbSession) {
	    //Check if connection is valid
            if (dbSession.getConn() != null) {
		try {
		     Statement stm = dbSession.getConn().createStatement();
		     stm.execute(getValidationQuery());
		     stm.close();
		     return;
		} catch (SQLTimeoutException e) {
                    logger.warn("Session " + dbSession.getInternalId() + 
                            " timed out on validation. DB may be busy.");
		    return;
		} catch (SQLException e) {
                    logger.warn("Session " + dbSession.getInternalId() + 
                            " failed validation. Will renew its connection.");
		}
            }
	    logger.debug("Will renew Session " + dbSession.getInternalId() +
			    " connection.");
	    dbSession.renewConnection();
        }
    }

    protected void validConnectionSelect(DbSession dbSession)
            throws WaarpDatabaseNoConnectionException {
        validConnection(dbSession);
    }

    /**
     * 
     * @return the associated String to validate the connection (as "select 1 from dual")
     */
    protected abstract String validConnectionString();

    public Connection getDbConnection(String server, String user, String passwd)
            throws SQLException {
        // Default implementation
        return DriverManager.getConnection(server, user, passwd);
    }

    public void releaseResources() {
    }

    public int currentNumberOfPooledConnections() {
        return DbAdmin.getNbConnection();
    }

}
