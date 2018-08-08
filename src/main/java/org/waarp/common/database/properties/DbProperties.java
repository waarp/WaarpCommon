package org.waarp.common.database.properties;

public abstract class DbProperties {
    
    /**
     * @return the driver class name associated with the DbModel
     */
    abstract public String getDriverName();
    
    /**
     * @return the validation query associated with the DbModel
     */
    abstract public String getValidationQuery();
}
