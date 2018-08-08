package org.waarp.common.database.properties;

/**
 * MariaDB Database Model
 */
public class MariaDBProperties extends DbProperties {
    private static final String PROTOCOL = "mariadb";

    private final String DRIVER_NAME = "org.mariadb.jdbc.Driver";
    private final String VALIDATION_QUERY = "select 1";
	
    public MariaDBProperties() {
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
