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

import goldengate.common.logging.GgInternalLogger;
import goldengate.common.logging.GgInternalLoggerFactory;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;
import java.util.concurrent.locks.ReentrantLock;

import goldengate.common.database.DbConstant;
import goldengate.common.database.DbPreparedStatement;
import goldengate.common.database.DbRequest;
import goldengate.common.database.DbSession;
import goldengate.common.database.data.DbDataModel;
import goldengate.common.database.exception.GoldenGateDatabaseNoConnectionError;
import goldengate.common.database.exception.GoldenGateDatabaseNoDataException;
import goldengate.common.database.exception.GoldenGateDatabaseSqlError;

/**
 * MySQL Database Model implementation
 * @author Frederic Bregier
 *
 */
public abstract class DbModelMysql extends DbModelAbstract {
    /**
     * Internal Logger
     */
    private static final GgInternalLogger logger = GgInternalLoggerFactory
            .getLogger(DbModelMysql.class);

    public static DbType type = DbType.MySQL;
    /**
     * Create the object and initialize if necessary the driver
     * @throws GoldenGateDatabaseNoConnectionError
     */
    public DbModelMysql() throws GoldenGateDatabaseNoConnectionError {
        if (DbModelFactory.classLoaded) {
            return;
        }
        try {
            DriverManager.registerDriver(new com.mysql.jdbc.Driver());
            DbModelFactory.classLoaded = true;
        } catch (SQLException e) {
         // SQLException
            logger.error("Cannot register Driver " + type.name()+ "\n"+e.getMessage());
            DbSession.error(e);
            throw new GoldenGateDatabaseNoConnectionError(
                    "Cannot load database drive:" + type.name(), e);
        }

    }

    protected static enum DBType {
        CHAR(Types.CHAR, " CHAR(3) "),
        VARCHAR(Types.VARCHAR, " VARCHAR(254) "),
        LONGVARCHAR(Types.LONGVARCHAR, " TEXT "),
        BIT(Types.BIT, " BOOLEAN "),
        TINYINT(Types.TINYINT, " TINYINT "),
        SMALLINT(Types.SMALLINT, " SMALLINT "),
        INTEGER(Types.INTEGER, " INTEGER "),
        BIGINT(Types.BIGINT, " BIGINT "),
        REAL(Types.REAL, " FLOAT "),
        DOUBLE(Types.DOUBLE, " DOUBLE "),
        VARBINARY(Types.VARBINARY, " BLOB "),
        DATE(Types.DATE, " DATE "),
        TIMESTAMP(Types.TIMESTAMP, " TIMESTAMP ");

        public int type;

        public String constructor;

        private DBType(int type, String constructor) {
            this.type = type;
            this.constructor = constructor;
        }

        public static String getType(int sqltype) {
            switch (sqltype) {
                case Types.CHAR:
                    return CHAR.constructor;
                case Types.VARCHAR:
                    return VARCHAR.constructor;
                case Types.LONGVARCHAR:
                    return LONGVARCHAR.constructor;
                case Types.BIT:
                    return BIT.constructor;
                case Types.TINYINT:
                    return TINYINT.constructor;
                case Types.SMALLINT:
                    return SMALLINT.constructor;
                case Types.INTEGER:
                    return INTEGER.constructor;
                case Types.BIGINT:
                    return BIGINT.constructor;
                case Types.REAL:
                    return REAL.constructor;
                case Types.DOUBLE:
                    return DOUBLE.constructor;
                case Types.VARBINARY:
                    return VARBINARY.constructor;
                case Types.DATE:
                    return DATE.constructor;
                case Types.TIMESTAMP:
                    return TIMESTAMP.constructor;
                default:
                    return null;
            }
        }
    }

    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public void createTables(DbSession session) throws GoldenGateDatabaseNoConnectionError {
        // Create tables: configuration, hosts, rules, runner, cptrunner
        String createTableH2 = "CREATE TABLE IF NOT EXISTS ";
        String primaryKey = " PRIMARY KEY ";
        String notNull = " NOT NULL ";

        // Example
        String action = createTableH2 + DbDataModel.table + "(";
        DbDataModel.Columns[] ccolumns = DbDataModel.Columns
                .values();
        for (int i = 0; i < ccolumns.length - 1; i ++) {
            action += ccolumns[i].name() +
                    DBType.getType(DbDataModel.dbTypes[i]) + notNull +
                    ", ";
        }
        action += ccolumns[ccolumns.length - 1].name() +
                DBType.getType(DbDataModel.dbTypes[ccolumns.length - 1]) +
                primaryKey + ")";
        System.out.println(action);
        DbRequest request = new DbRequest(session);
        try {
            request.query(action);
        } catch (GoldenGateDatabaseNoConnectionError e) {
            e.printStackTrace();
            return;
        } catch (GoldenGateDatabaseSqlError e) {
            e.printStackTrace();
            return;
        } finally {
            request.close();
        }
        // Index Example
        action = "CREATE INDEX IDX_RUNNER ON "+ DbDataModel.table + "(";
        DbDataModel.Columns[] icolumns = DbDataModel.indexes;
        for (int i = 0; i < icolumns.length-1; i ++) {
            action += icolumns[i].name()+ ", ";
        }
        action += icolumns[icolumns.length-1].name()+ ")";
        System.out.println(action);
        try {
            request.query(action);
        } catch (GoldenGateDatabaseNoConnectionError e) {
            e.printStackTrace();
            return;
        } catch (GoldenGateDatabaseSqlError e) {
            return;
        } finally {
            request.close();
        }

        // example sequence
        /*
         * # Table to handle any number of sequences:
            CREATE TABLE Sequences (
              name VARCHAR(22) NOT NULL,
              seq INT UNSIGNED NOT NULL,  # (or BIGINT)
              PRIMARY KEY name
            );

            # Create a Sequence:
            INSERT INTO Sequences (name, seq) VALUES (?, 0);
            # Drop a Sequence:
            DELETE FROM Sequences WHERE name = ?;

            # Get a sequence number:
            UPDATE Sequences
              SET seq = LAST_INSERT_ID(seq + 1)
              WHERE name = ?;
            $seq = $db->LastInsertId();
         */
        action = "CREATE TABLE Sequences (name VARCHAR(22) NOT NULL PRIMARY KEY,"+
              "seq BIGINT NOT NULL)";
        System.out.println(action);
        try {
            request.query(action);
        } catch (GoldenGateDatabaseNoConnectionError e) {
            e.printStackTrace();
            return;
        } catch (GoldenGateDatabaseSqlError e) {
            e.printStackTrace();
            return;
        } finally {
            request.close();
        }
        action = "INSERT INTO Sequences (name, seq) VALUES ('"+DbDataModel.fieldseq+"', "+
            (DbConstant.ILLEGALVALUE + 1)+")";
        System.out.println(action);
        try {
            request.query(action);
        } catch (GoldenGateDatabaseNoConnectionError e) {
            e.printStackTrace();
            return;
        } catch (GoldenGateDatabaseSqlError e) {
            e.printStackTrace();
            return;
        } finally {
            request.close();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see openr66.database.model.DbModel#resetSequence()
     */
    @Override
    public void resetSequence(DbSession session,long newvalue) throws GoldenGateDatabaseNoConnectionError {
        String action = "UPDATE Sequences SET seq = " + newvalue+
            " WHERE name = '"+ DbDataModel.fieldseq + "'";
        DbRequest request = new DbRequest(session);
        try {
            request.query(action);
        } catch (GoldenGateDatabaseNoConnectionError e) {
            e.printStackTrace();
            return;
        } catch (GoldenGateDatabaseSqlError e) {
            e.printStackTrace();
            return;
        } finally {
            request.close();
        }
        System.out.println(action);
    }

    /*
     * (non-Javadoc)
     *
     * @see openr66.database.model.DbModel#nextSequence()
     */
    @Override
    public synchronized long nextSequence(DbSession dbSession)
        throws GoldenGateDatabaseNoConnectionError,
            GoldenGateDatabaseSqlError, GoldenGateDatabaseNoDataException {
        lock.lock();
        try {
            long result = DbConstant.ILLEGALVALUE;
            String action = "SELECT seq FROM Sequences WHERE name = '" +
            DbDataModel.fieldseq + "' FOR UPDATE";
            DbPreparedStatement preparedStatement = new DbPreparedStatement(
                    dbSession);
            try {
                dbSession.conn.setAutoCommit(false);
            } catch (SQLException e1) {
            }
            try {
                preparedStatement.createPrepareStatement(action);
                // Limit the search
                preparedStatement.executeQuery();
                if (preparedStatement.getNext()) {
                    try {
                        result = preparedStatement.getResultSet().getLong(1);
                    } catch (SQLException e) {
                        throw new GoldenGateDatabaseSqlError(e);
                    }
                } else {
                    throw new GoldenGateDatabaseNoDataException(
                            "No sequence found. Must be initialized first");
                }
            } finally {
                preparedStatement.realClose();
            }
            action = "UPDATE Sequences SET seq = "+(result+1)+
                " WHERE name = '"+DbDataModel.fieldseq+"'";
            try {
                preparedStatement.createPrepareStatement(action);
                // Limit the search
                preparedStatement.executeUpdate();
            } finally {
                preparedStatement.realClose();
            }
            return result;
        } finally {
            try {
                dbSession.conn.setAutoCommit(true);
            } catch (SQLException e1) {
            }
            lock.unlock();
        }
    }


    /* (non-Javadoc)
     * @see goldengate.common.database.model.DbModelAbstract#validConnectionString()
     */
    @Override
    protected String validConnectionString() {
        return "select 1 from dual";
    }

    @Override
    public String limitRequest(String allfields, String request, int nb) {
        return request+" LIMIT "+nb;
    }

}
