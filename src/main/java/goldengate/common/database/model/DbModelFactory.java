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

import goldengate.common.database.DbAdmin;
import goldengate.common.database.exception.OpenR66DatabaseNoConnectionError;

/**
 * Factory to store the Database Model object
 *
 * @author Frederic Bregier
 *
 */
public class DbModelFactory {

    /**
     * Info on JDBC Class is already loaded or not
     */
    static public volatile boolean classLoaded = false;
    /**
     * Database Model Object
     */
    public static DbModel dbModel;
    /**
     * Initialize the Database Model according to arguments.
     * @param dbdriver
     * @param dbserver
     * @param dbuser
     * @param dbpasswd
     * @param write
     * @throws OpenR66DatabaseNoConnectionError
     */
    public static DbAdmin initialize(String dbdriver, String dbserver,
            String dbuser, String dbpasswd, boolean write)
            throws OpenR66DatabaseNoConnectionError {
        DbType type = DbType.getFromDriver(dbdriver);
        switch (type) {
            case H2:
                //dbModel = new DbModelH2();
                break;
            case Oracle:
                //dbModel = new DbModelOracle();
                break;
            case PostGreSQL:
                //dbModel = new DbModelPostgresql();
                break;
            case MySQL:
                //dbModel = new DbModelMysql();
                break;
            default:
                throw new OpenR66DatabaseNoConnectionError(
                        "TypeDriver unknown: " + type);
        }
        return new DbAdmin(type, dbserver, dbuser, dbpasswd,
                write);
    }
}