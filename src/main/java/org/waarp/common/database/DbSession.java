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
package org.waarp.common.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.TimerTask;
import org.waarp.common.database.exception.WaarpDatabaseNoConnectionException;
import org.waarp.common.database.exception.WaarpDatabaseSqlException;
import org.waarp.common.database.model.DbModel;
import org.waarp.common.database.model.DbModelFactory;
import org.waarp.common.logging.WaarpInternalLogger;
import org.waarp.common.logging.WaarpInternalLoggerFactory;
import org.waarp.common.utility.UUID;

// Notice, do not import com.mysql.jdbc.*
// or you will have problems!

/**
 * Class to handle session with the SGBD
 * 
 * @author Frederic Bregier
 * 
 */
public class DbSession {
    /**
     * Internal Logger
     */
    private static final WaarpInternalLogger logger = WaarpInternalLoggerFactory
            .getLogger(DbSession.class);

    /**
     * DbAdmin referent object
     */
    public DbAdmin admin = null;

    /**
     * The internal connection
     */
    public Connection conn = null;

    /**
     * Is this connection Read Only
     */
    public boolean isReadOnly = true;

    /**
     * Is this session using AutoCommit (true by default)
     */
    public boolean autoCommit = true;

    /**
     * Internal Id
     */
    public UUID internalId;

    /**
     * Number of threads using this connection
     */
    public AtomicInteger nbThread = new AtomicInteger(0);

    /**
     * To be used when a local Channel is over
     */
    public volatile boolean isDisconnected = true;

    /**
     * List all DbPrepareStatement with long term usage to enable the recreation when the associated
     * connection is reopened
     */
    private final Collection<DbPreparedStatement> listPreparedStatement = new ConcurrentLinkedQueue<DbPreparedStatement>();

    void setInternalId(DbSession session) {
        session.internalId = new UUID();
    }

    private void initialize(DbModel dbModel, String server, String user, String passwd, boolean isReadOnly,
            boolean autoCommit) throws WaarpDatabaseNoConnectionException {
        if (!DbModelFactory.classLoaded.contains(dbModel.getDbType().name())) {
            throw new WaarpDatabaseNoConnectionException(
                    "DbAdmin not initialzed");
        }
        if (server == null) {
            conn = null;
            logger.error("Cannot set a null Server");
            throw new WaarpDatabaseNoConnectionException(
                    "Cannot set a null Server");
        }
        try {
            this.autoCommit = autoCommit;
            conn = dbModel.getDbConnection(server, user, passwd);
            conn.setAutoCommit(this.autoCommit);
            this.isReadOnly = isReadOnly;
            conn.setReadOnly(this.isReadOnly);
            setInternalId(this);
            logger.debug("Open Db Conn: " + internalId);
            DbAdmin.addConnection(internalId, this);
            isDisconnected = false;
            checkConnection();
        } catch (SQLException ex) {
            isDisconnected = true;
            // handle any errors
            logger.error("Cannot create Connection");
            error(ex);
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {}
            }
            conn = null;
            throw new WaarpDatabaseNoConnectionException(
                    "Cannot create Connection", ex);
        }
    }

    /**
     * Create a session and connect the current object to the server using the DbAdmin object. The
     * database access use auto commit.
     * 
     * If the initialize is not call before, call it with the default value.
     * 
     * @param admin
     * @param isReadOnly
     * @throws WaarpDatabaseSqlException
     */
    public DbSession(DbAdmin admin, boolean isReadOnly)
            throws WaarpDatabaseNoConnectionException {
        try {
            this.admin = admin;
            initialize(admin.getDbModel(), admin.getServer(), admin.getUser(), admin.getPasswd(), isReadOnly, true);
        } catch (NullPointerException ex) {
            // handle any errors
            isDisconnected = true;
            logger.error("Cannot create Connection:" + (admin == null), ex);
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {}
            }
            conn = null;
            throw new WaarpDatabaseNoConnectionException(
                    "Cannot create Connection", ex);
        }
    }

    /**
     * Create a session and connect the current object to the server using the DbAdmin object.
     * 
     * If the initialize is not call before, call it with the default value.
     * 
     * @param admin
     * @param isReadOnly
     * @param autoCommit
     * @throws WaarpDatabaseSqlException
     */
    public DbSession(DbAdmin admin, boolean isReadOnly, boolean autoCommit)
            throws WaarpDatabaseNoConnectionException {
        try {
            this.admin = admin;
            initialize(admin.getDbModel(), admin.getServer(), admin.getUser(), admin.getPasswd(), isReadOnly, autoCommit);
        } catch (NullPointerException ex) {
            // handle any errors
            logger.error("Cannot create Connection:" + (admin == null), ex);
            isDisconnected = true;
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {}
            }
            conn = null;
            throw new WaarpDatabaseNoConnectionException(
                    "Cannot create Connection", ex);
        }
    }

    /**
     * Change the autocommit feature
     * 
     * @param autoCommit
     * @throws WaarpDatabaseNoConnectionException
     */
    public void setAutoCommit(boolean autoCommit)
            throws WaarpDatabaseNoConnectionException {
        if (conn != null) {
            this.autoCommit = autoCommit;
            try {
                conn.setAutoCommit(autoCommit);
            } catch (SQLException e) {
                // handle any errors
                logger.error("Cannot create Connection");
                error(e);
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e1) {}
                }
                conn = null;
                isDisconnected = true;
                throw new WaarpDatabaseNoConnectionException(
                        "Cannot create Connection", e);
            }
        }
    }

    /**
     * @return the admin
     */
    public DbAdmin getAdmin() {
        return admin;
    }

    /**
     * @param admin
     *            the admin to set
     */
    public void setAdmin(DbAdmin admin) {
        this.admin = admin;
    }

    /**
     * Print the error from SQLException
     * 
     * @param ex
     */
    public static void error(SQLException ex) {
        // handle any errors
        logger.error("SQLException: " + ex.getMessage() + " SQLState: " +
                ex.getSQLState() + "VendorError: " + ex.getErrorCode());
    }

    /**
     * To be called when a client will start to use this DbSession (once by client)
     */
    public void useConnection() {
        int val = nbThread.incrementAndGet();
        synchronized (this) {
            if (isDisconnected) {
                try {
                    initialize(admin.getDbModel(), admin.getServer(), admin.getUser(), admin.getPasswd(), isReadOnly, autoCommit);
                } catch (WaarpDatabaseNoConnectionException e) {
                    logger.error("ThreadUsing: " + nbThread + " but not connected");
                    return;
                }
            }
        }
        logger.debug("ThreadUsing: " + val);
    }

    /**
     * To be called when a client will stop to use this DbSession (once by client)
     */
    public void endUseConnection() {
        int val = nbThread.decrementAndGet();
        logger.debug("ThreadUsing: " + val);
        if (val <= 0) {
            disconnect();
        }
    }

    /**
     * To be called when a client will stop to use this DbSession (once by client). This version is
     * not blocking.
     */
    public void enUseConnectionNoDisconnect() {
        int val = nbThread.decrementAndGet();
        logger.debug("ThreadUsing: " + val);
        if (val <= 0) {
            DbAdmin.dbSessionTimer.newTimeout(new TryDisconnectDbSession(this), DbAdmin.WAITFORNETOP * 10,
                    TimeUnit.MILLISECONDS);
        }
    }

    /**
     * To disconnect in asynchronous way the DbSession
     * 
     * @author "Frederic Bregier"
     * 
     */
    private static class TryDisconnectDbSession implements TimerTask {
        private final DbSession dbSession;

        private TryDisconnectDbSession(DbSession dbSession) {
            this.dbSession = dbSession;
        }

        public void run(Timeout timeout) throws Exception {
            int val = dbSession.nbThread.get();
            if (val <= 0) {
                dbSession.disconnect();
            }
            logger.debug("ThreadUsing: " + val);
        }
    }

    @Override
    public int hashCode() {
        return this.internalId.hashCode();

    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof DbSession))
            return false;
        return (this == o) || this.internalId.equals(((DbSession) o).internalId);
    }

    /**
     * Force the close of the connection
     */
    public void forceDisconnect() {
        if (this.internalId.equals(admin.session.internalId)) {
            logger.debug("Closing internal db connection");
        }
        this.nbThread.set(0);
        if (conn == null) {
            logger.debug("Connection already closed");
            return;
        }
        try {
            Thread.sleep(DbAdmin.WAITFORNETOP);
        } catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
        }
        logger.debug("DbConnection still in use: " + nbThread);
        removeLongTermPreparedStatements();
        DbAdmin.removeConnection(internalId);
        isDisconnected = true;
        try {
            logger.debug("Fore close Db Conn: " + internalId);
            if (conn != null) {
                conn.close();
                conn = null;
            }
        } catch (SQLException e) {
            logger.warn("Disconnection not OK");
            error(e);
        } catch (ConcurrentModificationException e) {
            // ignore
        }
        logger.info("Current cached connection: "
                + admin.getDbModel().currentNumberOfPooledConnections());
    }

    /**
     * Close the connection
     * 
     */
    public void disconnect() {
        if (this.internalId.equals(admin.session.internalId)) {
            logger.debug("Closing internal db connection: " + nbThread.get());
        }
        if (conn == null || isDisconnected) {
            logger.debug("Connection already closed");
            return;
        }
        try {
            Thread.sleep(DbAdmin.WAITFORNETOP);
        } catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
        }
        logger.debug("DbConnection still in use: " + nbThread);
        if (nbThread.get() > 0) {
            logger.info("Still some clients could use this Database Session: " +
                    nbThread);
            return;
        }
        synchronized (this) {
            removeLongTermPreparedStatements();
            DbAdmin.removeConnection(internalId);
            isDisconnected = true;
            try {
                logger.debug("Close Db Conn: " + internalId);
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            } catch (SQLException e) {
                logger.warn("Disconnection not OK");
                error(e);
            } catch (ConcurrentModificationException e) {
                // ignore
            }
        }
        logger.info("Current cached connection: "
                + admin.getDbModel().currentNumberOfPooledConnections());
    }

    /**
     * Check the connection to the Database and try to reopen it if possible
     * 
     * @throws WaarpDatabaseNoConnectionException
     */
    public void checkConnection() throws WaarpDatabaseNoConnectionException {
        try {
            admin.getDbModel().validConnection(this);
            isDisActive = false;
            if (admin != null)
                admin.isConnected = true;
        } catch (WaarpDatabaseNoConnectionException e) {
            isDisconnected = true;
            if (admin != null)
                admin.isConnected = false;
            throw e;
        }
    }

    /**
     * 
     * @return True if the connection was successfully reconnected
     */
    public boolean checkConnectionNoException() {
        try {
            checkConnection();
            return true;
        } catch (WaarpDatabaseNoConnectionException e) {
            return false;
        }
    }

    /**
     * Add a Long Term PreparedStatement
     * 
     * @param longterm
     */
    public void addLongTermPreparedStatement(DbPreparedStatement longterm) {
        this.listPreparedStatement.add(longterm);
    }

    /**
     * Due to a reconnection, recreate all associated long term PreparedStatements
     * 
     * @throws WaarpDatabaseNoConnectionException
     * @throws WaarpDatabaseSqlException
     */
    public void recreateLongTermPreparedStatements()
            throws WaarpDatabaseNoConnectionException,
            WaarpDatabaseSqlException {
        WaarpDatabaseNoConnectionException elast = null;
        WaarpDatabaseSqlException e2last = null;
        logger.info("RecreateLongTermPreparedStatements: " + listPreparedStatement.size());
        for (DbPreparedStatement longterm : listPreparedStatement) {
            try {
                longterm.recreatePreparedStatement();
            } catch (WaarpDatabaseNoConnectionException e) {
                logger.warn(
                        "Error while recreation of Long Term PreparedStatement",
                        e);
                elast = e;
            } catch (WaarpDatabaseSqlException e) {
                logger.warn(
                        "Error while recreation of Long Term PreparedStatement",
                        e);
                e2last = e;
            }
        }
        if (elast != null) {
            throw elast;
        }
        if (e2last != null) {
            throw e2last;
        }
    }

    /**
     * Remove all Long Term PreparedStatements (closing connection)
     */
    public void removeLongTermPreparedStatements() {
        for (DbPreparedStatement longterm : listPreparedStatement) {
            if (longterm != null) {
                longterm.realClose();
            }
        }
        listPreparedStatement.clear();
    }

    /**
     * Remove one Long Term PreparedStatement
     * 
     * @param longterm
     */
    public void removeLongTermPreparedStatements(DbPreparedStatement longterm) {
        listPreparedStatement.remove(longterm);
    }

    /**
     * Commit everything
     * 
     * @throws WaarpDatabaseSqlException
     * @throws WaarpDatabaseNoConnectionException
     */
    public void commit() throws WaarpDatabaseSqlException,
            WaarpDatabaseNoConnectionException {
        if (conn == null) {
            logger.warn("Cannot commit since connection is null");
            throw new WaarpDatabaseNoConnectionException(
                    "Cannot commit since connection is null");
        }
        if (this.autoCommit) {
            return;
        }
        if (isDisconnected) {
            checkConnection();
        }
        try {
            conn.commit();
        } catch (SQLException e) {
            logger.error("Cannot Commit");
            error(e);
            throw new WaarpDatabaseSqlException("Cannot commit", e);
        }
    }

    /**
     * Rollback from the savepoint or the last set if null
     * 
     * @param savepoint
     * @throws WaarpDatabaseNoConnectionException
     * @throws WaarpDatabaseSqlException
     */
    public void rollback(Savepoint savepoint)
            throws WaarpDatabaseNoConnectionException,
            WaarpDatabaseSqlException {
        if (conn == null) {
            logger.warn("Cannot rollback since connection is null");
            throw new WaarpDatabaseNoConnectionException(
                    "Cannot rollback since connection is null");
        }
        if (isDisconnected) {
            checkConnection();
        }
        try {
            if (savepoint == null) {
                conn.rollback();
            } else {
                conn.rollback(savepoint);
            }
        } catch (SQLException e) {
            logger.error("Cannot rollback");
            error(e);
            throw new WaarpDatabaseSqlException("Cannot rollback", e);
        }
    }

    /**
     * Make a savepoint
     * 
     * @return the new savepoint
     * @throws WaarpDatabaseNoConnectionException
     * @throws WaarpDatabaseSqlException
     */
    public Savepoint savepoint() throws WaarpDatabaseNoConnectionException,
            WaarpDatabaseSqlException {
        if (conn == null) {
            logger.warn("Cannot savepoint since connection is null");
            throw new WaarpDatabaseNoConnectionException(
                    "Cannot savepoint since connection is null");
        }
        if (isDisconnected) {
            checkConnection();
        }
        try {
            return conn.setSavepoint();
        } catch (SQLException e) {
            logger.error("Cannot savepoint");
            error(e);
            throw new WaarpDatabaseSqlException("Cannot savepoint", e);
        }
    }

    /**
     * Release the savepoint
     * 
     * @param savepoint
     * @throws WaarpDatabaseNoConnectionException
     * @throws WaarpDatabaseSqlException
     */
    public void releaseSavepoint(Savepoint savepoint)
            throws WaarpDatabaseNoConnectionException,
            WaarpDatabaseSqlException {
        if (conn == null) {
            logger.warn("Cannot release savepoint since connection is null");
            throw new WaarpDatabaseNoConnectionException(
                    "Cannot release savepoint since connection is null");
        }
        if (isDisconnected) {
            checkConnection();
        }
        try {
            conn.releaseSavepoint(savepoint);
        } catch (SQLException e) {
            logger.error("Cannot release savepoint");
            error(e);
            throw new WaarpDatabaseSqlException("Cannot release savepoint", e);
        }
    }
}
