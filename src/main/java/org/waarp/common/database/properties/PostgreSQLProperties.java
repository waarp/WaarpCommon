package org.waarp.common.database.properties;

/**
 * PostgreSQL Database Model
 */
public class PostgreSQLProperties extends DbProperties {
    public static final String PROTOCOL = "postgres";

    private final String DRIVER_NAME = "org.postgresql.Driver";
    private final String VALIDATION_QUERY = "select 1";
	
    public PostgreSQLProperties() {
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
