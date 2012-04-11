/**
 * This file is part of GoldenGate Project (named also GoldenGate or GG).
 * 
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author
 * tags. See the COPYRIGHT.txt in the distribution for a full listing of
 * individual contributors.
 * 
 * All GoldenGate Project is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * GoldenGate is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * GoldenGate . If not, see <http://www.gnu.org/licenses/>.
 */
package goldengate.common.database.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import goldengate.common.database.DbAdmin;
import goldengate.common.database.DbConstant;
import goldengate.common.database.DbSession;
import goldengate.common.database.exception.GoldenGateDatabaseNoConnectionError;
import goldengate.common.database.exception.GoldenGateDatabaseSqlError;
import goldengate.common.logging.GgInternalLogger;
import goldengate.common.logging.GgInternalLoggerFactory;

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
    private static final GgInternalLogger logger = GgInternalLoggerFactory
            .getLogger(DbModelAbstract.class);

    /**
     * Recreate the disconnected session
     * 
     * @param dbSession
     * @throws GoldenGateDatabaseNoConnectionError
     */
    private void recreateSession(DbSession dbSession)
            throws GoldenGateDatabaseNoConnectionError {
        DbAdmin admin = dbSession.getAdmin();
        if (admin == null) {
            if (dbSession.autoCommit) {
                admin = DbConstant.admin;
            } else {
                admin = DbConstant.noCommitAdmin;
            }
        }
        DbSession newdbSession = new DbSession(admin,
                dbSession.isReadOnly);
        try {
            if (dbSession.conn != null) {
                dbSession.conn.close();
            }
        } catch (SQLException e1) {
        }
        dbSession.conn = newdbSession.conn;
        DbAdmin.addConnection(dbSession.internalId, dbSession);
        DbAdmin.removeConnection(newdbSession.internalId);
        logger.warn("Database Connection lost: database connection reopened");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * goldengate.common.database.model.DbModel#validConnection(goldengate.common
     * .database.DbSession)
     */
    @Override
    public void validConnection(DbSession dbSession)
            throws GoldenGateDatabaseNoConnectionError {
        Statement stmt = null;
        try {
            stmt = dbSession.conn.createStatement();
            if (stmt.execute(validConnectionString())) {
                ResultSet set = stmt.getResultSet();
                if (!set.next()) {
                    stmt.close();
                    stmt = null;
                    // Give a try by closing the current connection
                    throw new SQLException("Cannot connect to database");
                }
            }
            dbSession.isDisconnected = false;
            if (dbSession.admin != null)
                dbSession.admin.isConnected = true;
        } catch (SQLException e2) {
            dbSession.isDisconnected = true;
            if (dbSession.admin != null)
                dbSession.admin.isConnected = false;
            try {
                try {
                    recreateSession(dbSession);
                } catch (GoldenGateDatabaseNoConnectionError e) {
                    try {
                        if (dbSession.conn != null) {
                            dbSession.conn.close();
                        }
                    } catch (SQLException e1) {
                    }
                    DbAdmin.removeConnection(dbSession.internalId);
                    throw e;
                }
                try {
                    if (stmt != null) {
                        stmt.close();
                        stmt = null;
                    }
                } catch (SQLException e) {
                    // ignore
                }
                try {
                    stmt = dbSession.conn.createStatement();
                } catch (SQLException e) {
                    // Not ignored
                    try {
                        if (dbSession.conn != null) {
                            dbSession.conn.close();
                        }
                    } catch (SQLException e1) {
                    }
                    DbAdmin.removeConnection(dbSession.internalId);
                    throw new GoldenGateDatabaseNoConnectionError(
                            "Cannot connect to database", e);
                }
                try {
                    if (stmt.execute(validConnectionString())) {
                        ResultSet set = stmt.getResultSet();
                        if (!set.next()) {
                            try {
                                if (dbSession.conn != null) {
                                    dbSession.conn.close();
                                }
                            } catch (SQLException e1) {
                            }
                            DbAdmin.removeConnection(dbSession.internalId);
                            if (stmt != null) {
                                stmt.close();
                                stmt = null;
                            }
                            throw new GoldenGateDatabaseNoConnectionError(
                                    "Cannot connect to database");
                        }
                    }
                } catch (SQLException e) {
                    // not ignored
                    try {
                        if (dbSession.conn != null) {
                            dbSession.conn.close();
                        }
                    } catch (SQLException e1) {
                    }
                    DbAdmin.removeConnection(dbSession.internalId);
                    try {
                        if (stmt != null) {
                            stmt.close();
                            stmt = null;
                        }
                    } catch (SQLException e1) {
                    }
                    throw new GoldenGateDatabaseNoConnectionError(
                            "Cannot connect to database", e);
                }
                dbSession.isDisconnected = false;
                if (dbSession.admin != null)
                    dbSession.admin.isConnected = true;
                dbSession.recreateLongTermPreparedStatements();
                return;
            } catch (GoldenGateDatabaseSqlError e1) {
                // ignore and will send a No Connection error
            }
            try {
                if (dbSession.conn != null) {
                    dbSession.conn.close();
                }
            } catch (SQLException e1) {
            }
            DbAdmin.removeConnection(dbSession.internalId);
            throw new GoldenGateDatabaseNoConnectionError(
                    "Cannot connect to database", e2);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 
     * @return the associated String to validate the connection (as
     *         "select 1 frm dual")
     */
    protected abstract String validConnectionString();
}
