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

import java.sql.SQLException;
import java.util.ConcurrentModificationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.waarp.common.database.exception.WaarpDatabaseNoConnectionException;
import org.waarp.common.database.exception.WaarpDatabaseSqlException;
import org.waarp.common.database.model.DbModel;
import org.waarp.common.database.model.DbModelFactory;
import org.waarp.common.database.model.DbType;
import org.waarp.common.database.model.EmptyDbModel;
import org.waarp.common.logging.WaarpInternalLogger;
import org.waarp.common.logging.WaarpInternalLoggerFactory;
import org.waarp.common.utility.UUID;
import org.waarp.common.utility.WaarpThreadFactory;

/**
 * Class for access to Database
 * 
 * @author Frederic Bregier
 * 
 */
public class DbAdmin {
    /**
     * Internal Logger
     */
    private static final WaarpInternalLogger logger = WaarpInternalLoggerFactory
            .getLogger(DbAdmin.class);

    public static int RETRYNB = 3;

    public static long WAITFORNETOP = 100;

    /**
     * Database type
     */
    public final DbType typeDriver;

    /**
     * DbModel
     */
    private final DbModel dbModel;

    /**
     * DB Server
     */
    private final String server;

    /**
     * DB User
     */
    private final String user;

    /**
     * DB Password
     */
    private final String passwd;

    /**
     * Is this DB Admin connected
     */
    public boolean isConnected = false;

    /**
     * Is this DB Admin connected
     */
    public boolean isActive = false;

    /**
     * Is this DB Admin Read Only
     */
    public boolean isReadOnly = false;

    /**
     * session is the Session object for all type of requests
     */
    public DbSession session = null;

    protected static final Timer dbSessionTimer = new HashedWheelTimer(new WaarpThreadFactory("TimerClose"),
            50, TimeUnit.MILLISECONDS, 1024);

    /**
     * Validate connection
     * 
     * @throws WaarpDatabaseNoConnectionException
     */
    public void validConnection() throws WaarpDatabaseNoConnectionException {
        try {
            dbModel.validConnection(session);
        } catch (WaarpDatabaseNoConnectionException e) {
            session.isDisconnected = true;
            isConnected = false;
            throw e;
        }
        session.isDisconnected = false;
        isConnected = true;
    }

    /**
     * Use a default server for basic connection. Later on, specific connection to database for the
     * scheme that provides access to the table R66DbIndex for one specific Legacy could be done.
     * 
     * A this time, only one driver is possible! If a new driver is needed, then we need to create a
     * new DbSession object. Be aware that DbSession.initialize should be call only once for each
     * driver, whatever the number of DbSession objects that could be created (=> need a hashtable
     * for specific driver when created). Also, don't know if two drivers at the same time (two
     * different DbSession) is allowed by JDBC.
     * 
     * @param model
     * @param server
     * @param user
     * @param passwd
     * @throws WaarpDatabaseNoConnectionException
     */
    public DbAdmin(DbModel model, String server, String user, String passwd)
            throws WaarpDatabaseNoConnectionException {
        this.server = server;
        this.user = user;
        this.passwd = passwd;
        this.dbModel = model;
        this.typeDriver = model.getDbType();
        if (typeDriver == null) {
            logger.error("Cannot find TypeDriver");
            throw new WaarpDatabaseNoConnectionException(
                    "Cannot find database drive");
        }
        session = new DbSession(this, false);
        session.admin = this;
        isReadOnly = false;
        validConnection();
        session.useConnection(); // default since this is the top connection
    }

    /**
     * Use a default server for basic connection. Later on, specific connection to database for the
     * scheme that provides access to the table R66DbIndex for one specific Legacy could be done.
     * 
     * A this time, only one driver is possible! If a new driver is needed, then we need to create a
     * new DbSession object. Be aware that DbSession.initialize should be call only once for each
     * driver, whatever the number of DbSession objects that could be created (=> need a hashtable
     * for specific driver when created). Also, don't know if two drivers at the same time (two
     * different DbSession) is allowed by JDBC.
     * 
     * @param model
     * @param server
     * @param user
     * @param passwd
     * @param write
     * @throws WaarpDatabaseSqlException
     * @throws WaarpDatabaseNoConnectionException
     */
    public DbAdmin(DbModel model, String server, String user, String passwd,
            boolean write) throws WaarpDatabaseNoConnectionException {
        this.server = server;
        this.user = user;
        this.passwd = passwd;
        this.dbModel = model;
        this.typeDriver = model.getDbType();
        if (typeDriver == null) {
            logger.error("Cannot find TypeDriver");
            throw new WaarpDatabaseNoConnectionException(
                    "Cannot find database driver");
        }
        if (write) {
            for (int i = 0; i < RETRYNB; i++) {
                try {
                    session = new DbSession(this, false);
                } catch (WaarpDatabaseNoConnectionException e) {
                    logger.warn("Attempt of connection in error: " + i, e);
                    continue;
                }
                isReadOnly = false;
                session.admin = this;
                validConnection();
                session.useConnection(); // default since this is the top
                                         // connection
                return;
            }
        } else {
            for (int i = 0; i < RETRYNB; i++) {
                try {
                    session = new DbSession(this, true);
                } catch (WaarpDatabaseNoConnectionException e) {
                    logger.warn("Attempt of connection in error: " + i, e);
                    continue;
                }
                isReadOnly = true;
                session.admin = this;
                validConnection();
                session.useConnection(); // default since this is the top
                                         // connection
                return;
            }
        }
        session = null;
        isConnected = false;
        logger.error("Cannot connect to Database!");
        throw new WaarpDatabaseNoConnectionException(
                "Cannot connect to database");
    }

    /**
     * Empty constructor for no Database support (very thin client)
     */
    public DbAdmin() {
        // not true but to enable pseudo database functions
        isActive = false;
        typeDriver = DbType.none;
        DbModelFactory.classLoaded.add(DbType.none.name());
        dbModel = new EmptyDbModel();
        server = null;
        user = null;
        passwd = null;
    }

    /**
     * Close the underlying session. Can be call even for connection given from the constructor
     * DbAdmin(Connection, boolean).
     * 
     */
    public void close() {
        if (session != null) {
            session.endUseConnection(); // default since this is the top
                                        // connection
            session.forceDisconnect();
            session = null;
        }
        isConnected = false;
    }

    /**
     * Commit on connection (since in autocommit, should not be used)
     * 
     * @throws WaarpDatabaseNoConnectionException
     * @throws WaarpDatabaseSqlException
     * 
     */
    public void commit() throws WaarpDatabaseSqlException,
            WaarpDatabaseNoConnectionException {
        if (session != null) {
            session.commit();
        }
    }

    /**
     * @return the server
     */
    public String getServer() {
        return server;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @return the passwd
     */
    public String getPasswd() {
        return passwd;
    }

    /**
     * @return the associated dbModel
     */
    public DbModel getDbModel() {
        return dbModel;
    }

    @Override
    public String toString() {
        return "Admin: " + typeDriver.name() + ":" + server + ":" + user + ":" + (passwd.length());
    }

    /**
     * List all Connection to enable the close call on them
     */
    private static ConcurrentHashMap<UUID, DbSession> listConnection = new ConcurrentHashMap<UUID, DbSession>();

    /**
     * Number of HttpSession
     */
    public static int nbHttpSession = 0;

    /**
     * Add a Connection into the list
     * 
     * @param id
     * @param session
     */
    public static void addConnection(UUID id, DbSession session) {
        listConnection.put(id, session);
    }

    /**
     * Remove a Connection from the list
     * 
     * @param id
     *            Id of the connection
     */
    public static void removeConnection(UUID id) {
        listConnection.remove(id);
    }

    /**
     * 
     * @return the number of connection (so number of network channels)
     */
    public static int getNbConnection() {
        return listConnection.size() - 1;
    }

    /**
     * Close all database connections
     */
    public static void closeAllConnection() {
        for (DbSession session : listConnection.values()) {
            logger.debug("Close (all) Db Conn: " + session.internalId);
            try {
                session.conn.close();
            } catch (SQLException e) {} catch (ConcurrentModificationException e) {}
        }
        listConnection.clear();
        for (DbModel dbModel : DbModelFactory.dbModels) {
            if (dbModel != null) {
                dbModel.releaseResources();
            }
        }
        dbSessionTimer.stop();
    }

    /**
     * Check all database connections and try to reopen them if disconnected
     */
    public static void checkAllConnections() {
        for (DbSession session : listConnection.values()) {
            try {
                session.checkConnection();
            } catch (WaarpDatabaseNoConnectionException e) {
                logger.error("Database Connection cannot be reinitialized");
            }
        }
    }

    /**
     * 
     * @return True if this driver allows Thread Shared Connexion (concurrency usage)
     */
    public boolean isCompatibleWithThreadSharedConnexion() {
        return (typeDriver != DbType.MariaDB && typeDriver != DbType.MySQL);
    }
}
