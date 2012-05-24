/**
   This file is part of GoldenGate Project (named also GoldenGate or GG).

   Copyright 2009, Frederic Bregier, and individual contributors by the @author
   tags. See the COPYRIGHT.txt in the distribution for a full listing of
   individual contributors.

   All GoldenGate Project is free software: you can redistribute it and/or 
   modify it under the terms of the GNU General Public License as published 
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   GoldenGate is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with GoldenGate .  If not, see <http://www.gnu.org/licenses/>.
 */
package goldengate.common.database.model;

import java.sql.Connection;
import java.sql.SQLException;

import goldengate.common.database.DbSession;
import goldengate.common.database.exception.GoldenGateDatabaseNoConnectionError;
import goldengate.common.database.exception.GoldenGateDatabaseNoDataException;
import goldengate.common.database.exception.GoldenGateDatabaseSqlError;

/**
 * Interface for Database Model
 *
 * This class is an interface for special functions that needs special implementations according to
 * the database model used.
 *
 * @author Frederic Bregier
 *
 */
public interface DbModel {
    /**
     * 
     * @param server
     * @param user
     * @param passwd
     * @return a connection according to the underlying Database Model
     * @throws SQLException
     */
    public Connection getDbConnection(String server, String user, String passwd) throws SQLException;
    
    /**
     * Release any internal resources if needed
     */
    public void releaseResources();
    
    /**
     * 
     * @return the number of Pooled Connections if any
     */
    public int currentNumberOfPooledConnections();
    /**
     * 
     * @return the current DbType used
     */
    public DbType getDbType();
    /**
     * Create all necessary tables into the database
     * @param session SQL session
     * @throws GoldenGateDatabaseNoConnectionError
     */
    public void createTables(DbSession session) throws GoldenGateDatabaseNoConnectionError;

    /**
     * Reset the sequence (example)
     * @param session SQL session
     * @throws GoldenGateDatabaseNoConnectionError
     */
    public void resetSequence(DbSession session, long newvalue) throws GoldenGateDatabaseNoConnectionError;

    /**
     * @param dbSession
     * @return The next unique specialId
     */
    public long nextSequence(DbSession dbSession)
            throws GoldenGateDatabaseNoConnectionError, GoldenGateDatabaseSqlError,
            GoldenGateDatabaseNoDataException;

    /**
     * Validate connection
     * @param dbSession
     * @throws GoldenGateDatabaseNoConnectionError
     */
    public void validConnection(DbSession dbSession) throws GoldenGateDatabaseNoConnectionError;

    /**
     * Add a limit on the request to get the "limit" first rows. Note that
     * it must be compatible to add the "limit" condition.<br>
     * <b>DO NOT CHANGE (add) ANYTHING to the request</b><br>
     *
     * On Oracle: select allfield from (request) where rownnum <= limit<br>
     * On others: request LIMIT limit<br>
     *
     * @param allfields string representing the equivalent to "*" in "select *" but more precisely
     *          as "field1, field2" in "select field1, field2"
     * @param request
     * @param limit
     * @return the new request String which will limit the result to the specified number of rows
     */
    public String limitRequest(String allfields, String request, int limit);
}
