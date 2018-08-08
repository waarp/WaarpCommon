package org.waarp.common.database.properties;

/**
 * MySQL Database Model
 */
public class MySQLProperties extends DbProperties {
    public static final String PROTOCOL = "mysql";

    private final String DRIVER_NAME = "com.mysql.jdbc.Driver";
    private final String VALIDATION_QUERY = "select 1";
	
    public MySQLProperties() {
    }

    public static String getProtocolID() {
        return PROTOCOL;
    }

    @Override
    public String getDriverName() {
        return DRIVER_NAME;
    }

    @Override
    public String getValidationQuery() {
        return VALIDATION_QUERY;
    }
}
