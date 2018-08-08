package org.waarp.common.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;

import org.waarp.common.database.properties.DbProperties;
import org.waarp.common.database.properties.H2Properties;
import org.waarp.common.database.properties.MariaDBProperties;
import org.waarp.common.database.properties.MySQLProperties;
import org.waarp.common.database.properties.OracleProperties;
import org.waarp.common.database.properties.PostgreSQLProperties;

import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;

public class ConnectionFactory {

    /**
     * Internal Logger
     */
    private static final WaarpLogger logger = WaarpLoggerFactory
        .getLogger(ConnectionFactory.class);

    /**
     * DbModel
     */
    private final DbProperties properties;

    /**
     * The connection url to be passed to the JDBC driver to establish a connection.
     */
    private final String server;

    /**
     * The connection username to be passed to the JDBC driver to establish a connection.
     */
    private final String user;

    /**
     * The connection password to be passed to the JDBC driver to establish a connection.
     */
    private final String password;

    /**
     * The datasource for connection pooling
     */
    private BasicDataSource ds;

    public static DbProperties propertiesFor(String server) {
        if(server.contains(H2Properties.getProtocolID())) {
            return new H2Properties();
        } else if(server.contains(MariaDBProperties.getProtocolID())) {
            return new MariaDBProperties();
        } else if(server.contains(MySQLProperties.getProtocolID())) {
            return new MySQLProperties();
        } else if(server.contains(OracleProperties.getProtocolID())) {
            return new OracleProperties();
        } else if(server.contains(PostgreSQLProperties.getProtocolID())) {
            return new PostgreSQLProperties();
        } else {
            return null;
        }
    }


    public Connection getConnection() throws SQLException {
        if (ds == null) {
            throw new SQLException("ConnectionFactory is not inialized.");
        }
        try {
            Connection con = ds.getConnection();
            return con;
        } catch (SQLException e) {
            throw new SQLException("Cannot access database", e);
        }
    }

    /**
     * @param model
     * @param server
     * @param user
     * @param password
     */
    public ConnectionFactory(DbProperties properties, String server, String user, String password) 
            throws SQLException {
            this.server = server;
            this.user = user;
            this.password = password;
            this.properties = properties;

            ds = new BasicDataSource();

            ds.setDriverClassName(properties.getDriverName());
            ds.setUrl(this.server);
            ds.setUsername(this.user);
            ds.setPassword(this.password);
            ds.setDefaultAutoCommit(true);
            ds.setDefaultReadOnly(true);
            ds.setValidationQuery(this.properties.getValidationQuery());
    }

    /**
     * Closes and releases all registered connections and connection pool
     */
    public void close() {
        logger.info("Closing ConnectionFactory");
        try {
            ds.close();
        } catch (SQLException e) {
            logger.debug("Cannot close properly the connection pool", e);
        }
    }

    /**
     * @return the JDBC connection url property
     */
    public String getServer() {
        return server;
    }

    /**
     * @return the JDBC connection username property
     */
    public String getUser() {
        return user;
    }

    /**
     * @return the JDBC connection password property
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return the associated DbProperties
     */
    public DbProperties getProperties() {
        return properties;
    }
}
