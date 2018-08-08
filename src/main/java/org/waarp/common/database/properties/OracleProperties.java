package org.waarp.common.database.properties;

/**
 * Oracle Database Model
 */
public class OracleProperties extends DbProperties {
    public static final String PROTOCOL = "h2";

    private final String DRIVER_NAME = "oracle.jdbc.OracleDriver";
    private final String VALIDATION_QUERY = "select 1 from dual";
	
    public OracleProperties() {
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
