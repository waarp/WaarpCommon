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
 * You should have received a copy of the GNU General Public License along with Waarp . If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.waarp.common.database.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import org.waarp.common.database.DbPreparedStatement;
import org.waarp.common.database.DbSession;
import org.waarp.common.database.exception.WaarpDatabaseException;
import org.waarp.common.database.exception.WaarpDatabaseNoConnectionException;
import org.waarp.common.database.exception.WaarpDatabaseNoDataException;
import org.waarp.common.database.exception.WaarpDatabaseSqlException;
import org.waarp.common.json.JsonHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Abstract database table implementation without explicit COMMIT.<br>
 * <br>
 * 
 * If the connection is in autocommit, this is the right abstract to extend.<br>
 * If the connection is not in autocommit, one could use this implementation to explicitly commit
 * when needed.
 * 
 * @author Frederic Bregier
 * 
 */
public abstract class AbstractDbData {
	/**
	 * UpdatedInfo status
	 * 
	 * @author Frederic Bregier
	 * 
	 */
	public static enum UpdatedInfo {
		/**
		 * Unknown run status
		 */
		UNKNOWN,
		/**
		 * Not updated run status
		 */
		NOTUPDATED,
		/**
		 * Interrupted status (stop or cancel)
		 */
		INTERRUPTED,
		/**
		 * Updated run status meaning ready to be submitted
		 */
		TOSUBMIT,
		/**
		 * In error run status
		 */
		INERROR,
		/**
		 * Running status
		 */
		RUNNING,
		/**
		 * All done run status
		 */
		DONE;
	}

	/**
	 * To be implemented
	 */
	// public static String table;
	// public static final int NBPRKEY;
	// protected static String selectAllFields;
	// protected static String updateAllFields;
	// protected static String insertAllValues;
	protected DbValue[] primaryKey;
	protected DbValue[] otherFields;
	protected DbValue[] allFields;

	protected boolean isSaved = false;
	/**
	 * The DbSession to use
	 */
	protected final DbSession dbSession;

	/**
	 * Abstract constructor to set the DbSession to use
	 * 
	 * @param dbSession
	 */
	public AbstractDbData(DbSession dbSession) {
		this.dbSession = dbSession;
		initObject();
	}

	/**
	 * To setup primaryKey, otherFields, allFields. Note this initObject is called within
	 * constructor of AbstractDbData. Be careful that no data is actually initialized at this stage.
	 */
	protected abstract void initObject();

	/**
	 * 
	 * @return The Where condition on Primary Key
	 */
	protected abstract String getWherePrimaryKey();

	/**
	 * Set the primary Key as current value
	 */
	protected abstract void setPrimaryKey();

	protected abstract String getSelectAllFields();

	protected abstract String getTable();

	protected abstract String getInsertAllValues();

	protected abstract String getUpdateAllFields();

	/**
	 * Test the existence of the current object
	 * 
	 * @return True if the object exists
	 * @throws WaarpDatabaseException
	 */
	public boolean exist() throws WaarpDatabaseException {
		if (dbSession == null) {
			return false;
		}
		DbPreparedStatement preparedStatement = new DbPreparedStatement(
				dbSession);
		try {
			preparedStatement.createPrepareStatement("SELECT " +
					primaryKey[0].column + " FROM " + getTable() + " WHERE " +
					getWherePrimaryKey());
			setPrimaryKey();
			setValues(preparedStatement, primaryKey);
			preparedStatement.executeQuery();
			return preparedStatement.getNext();
		} finally {
			preparedStatement.realClose();
		}
	}

	/**
	 * Select object from table
	 * 
	 * @throws WaarpDatabaseException
	 */
	public void select() throws WaarpDatabaseException {
		if (dbSession == null) {
			throw new WaarpDatabaseNoDataException("No row found");
		}
		DbPreparedStatement preparedStatement = new DbPreparedStatement(
				dbSession);
		try {
			preparedStatement.createPrepareStatement("SELECT " + getSelectAllFields() +
					" FROM " + getTable() + " WHERE " +
					getWherePrimaryKey());
			setPrimaryKey();
			setValues(preparedStatement, primaryKey);
			preparedStatement.executeQuery();
			if (preparedStatement.getNext()) {
				getValues(preparedStatement, allFields);
				setFromArray();
				isSaved = true;
			} else {
				throw new WaarpDatabaseNoDataException("No row found");
			}
		} finally {
			preparedStatement.realClose();
		}
	}

	/**
	 * Insert object into table
	 * 
	 * @throws WaarpDatabaseException
	 */
	public void insert() throws WaarpDatabaseException {
		if (isSaved) {
			return;
		}
		if (dbSession == null) {
			isSaved = true;
			return;
		}
		setToArray();
		DbPreparedStatement preparedStatement = new DbPreparedStatement(
				dbSession);
		try {
			preparedStatement.createPrepareStatement("INSERT INTO " + getTable() +
					" (" + getSelectAllFields() + ") VALUES " + getInsertAllValues());
			setValues(preparedStatement, allFields);
			int count = preparedStatement.executeUpdate();
			if (count <= 0) {
				throw new WaarpDatabaseNoDataException("No row found");
			}
			isSaved = true;
		} finally {
			preparedStatement.realClose();
		}
	}

	/**
	 * Update object to table
	 * 
	 * @throws WaarpDatabaseException
	 */
	public void update() throws WaarpDatabaseException {
		if (isSaved) {
			return;
		}
		if (dbSession == null) {
			isSaved = true;
			return;
		}
		setToArray();
		DbPreparedStatement preparedStatement = new DbPreparedStatement(
				dbSession);
		try {
			preparedStatement.createPrepareStatement("UPDATE " + getTable() +
					" SET " + getUpdateAllFields() + " WHERE " +
					getWherePrimaryKey());
			setValues(preparedStatement, allFields);
			int count = preparedStatement.executeUpdate();
			if (count <= 0) {
				throw new WaarpDatabaseNoDataException("No row found");
			}
			isSaved = true;
		} finally {
			preparedStatement.realClose();
		}
	}

	/**
	 * Delete object from table
	 * 
	 * @throws WaarpDatabaseException
	 */
	public void delete() throws WaarpDatabaseException {
		if (dbSession == null) {
			return;
		}
		DbPreparedStatement preparedStatement = new DbPreparedStatement(
				dbSession);
		try {
			preparedStatement.createPrepareStatement("DELETE FROM " + getTable() +
					" WHERE " + getWherePrimaryKey());
			setPrimaryKey();
			setValues(preparedStatement, primaryKey);
			int count = preparedStatement.executeUpdate();
			if (count <= 0) {
				throw new WaarpDatabaseNoDataException("No row found");
			}
			isSaved = false;
		} finally {
			preparedStatement.realClose();
		}
	}

	/**
	 * Change UpdatedInfo status
	 * 
	 * @param info
	 */
	public abstract void changeUpdatedInfo(UpdatedInfo info);

	/**
	 * Internal function to set to Array used to push data to database
	 */
	protected abstract void setToArray();

	/**
	 * Internal function to retrieve data from Array to pull data from database
	 * 
	 * @throws WaarpDatabaseSqlException
	 */
	protected abstract void setFromArray() throws WaarpDatabaseSqlException;

	/**
	 * Set Value into PreparedStatement
	 * 
	 * @param ps
	 * @param value
	 * @param rank
	 *            >= 1
	 * @throws WaarpDatabaseSqlException
	 */
	static public void setTrueValue(PreparedStatement ps, DbValue value, int rank)
			throws WaarpDatabaseSqlException {
		try {
			switch (value.type) {
				case Types.VARCHAR:
					if (value.value == null) {
						ps.setNull(rank, Types.VARCHAR);
						break;
					}
					ps.setString(rank, (String) value.value);
					break;
				case Types.LONGVARCHAR:
					if (value.value == null) {
						ps.setNull(rank, Types.LONGVARCHAR);
						break;
					}
					ps.setString(rank, (String) value.value);
					break;
				case Types.BIT:
					if (value.value == null) {
						ps.setNull(rank, Types.BIT);
						break;
					}
					ps.setBoolean(rank, (Boolean) value.value);
					break;
				case Types.TINYINT:
					if (value.value == null) {
						ps.setNull(rank, Types.TINYINT);
						break;
					}
					ps.setByte(rank, (Byte) value.value);
					break;
				case Types.SMALLINT:
					if (value.value == null) {
						ps.setNull(rank, Types.SMALLINT);
						break;
					}
					ps.setShort(rank, (Short) value.value);
					break;
				case Types.INTEGER:
					if (value.value == null) {
						ps.setNull(rank, Types.INTEGER);
						break;
					}
					ps.setInt(rank, (Integer) value.value);
					break;
				case Types.BIGINT:
					if (value.value == null) {
						ps.setNull(rank, Types.BIGINT);
						break;
					}
					ps.setLong(rank, (Long) value.value);
					break;
				case Types.REAL:
					if (value.value == null) {
						ps.setNull(rank, Types.REAL);
						break;
					}
					ps.setFloat(rank, (Float) value.value);
					break;
				case Types.DOUBLE:
					if (value.value == null) {
						ps.setNull(rank, Types.DOUBLE);
						break;
					}
					ps.setDouble(rank, (Double) value.value);
					break;
				case Types.VARBINARY:
					if (value.value == null) {
						ps.setNull(rank, Types.VARBINARY);
						break;
					}
					ps.setBytes(rank, (byte[]) value.value);
					break;
				case Types.DATE:
					if (value.value == null) {
						ps.setNull(rank, Types.DATE);
						break;
					}
					ps.setDate(rank, (Date) value.value);
					break;
				case Types.TIMESTAMP:
					if (value.value == null) {
						ps.setNull(rank, Types.TIMESTAMP);
						break;
					}
					ps.setTimestamp(rank, (Timestamp) value.value);
					break;
				case Types.CLOB:
					if (value.value == null) {
						ps.setNull(rank, Types.CLOB);
						break;
					}
					ps.setClob(rank, (Reader) value.value);
					break;
				case Types.BLOB:
					if (value.value == null) {
						ps.setNull(rank, Types.BLOB);
						break;
					}
					ps.setBlob(rank, (InputStream) value.value);
					break;
				default:
					throw new WaarpDatabaseSqlException("Type not supported: " +
							value.type + " at " + rank);
			}
		} catch (ClassCastException e) {
			throw new WaarpDatabaseSqlException("Setting values casting error: " +
					value.type + " at " + rank, e);
		} catch (SQLException e) {
			DbSession.error(e);
			throw new WaarpDatabaseSqlException("Setting values in error: " +
					value.type + " at " + rank, e);
		}
	}

	/**
	 * Set one value to a DbPreparedStatement
	 * 
	 * @param preparedStatement
	 * @param value
	 * @throws WaarpDatabaseNoConnectionException
	 * @throws WaarpDatabaseSqlException
	 */
	protected void setValue(DbPreparedStatement preparedStatement, DbValue value)
			throws WaarpDatabaseNoConnectionException, WaarpDatabaseSqlException {
		PreparedStatement ps = preparedStatement.getPreparedStatement();
		setTrueValue(ps, value, 1);
	}

	/**
	 * Set several values to a DbPreparedStatement
	 * 
	 * @param preparedStatement
	 * @param values
	 * @throws WaarpDatabaseNoConnectionException
	 * @throws WaarpDatabaseSqlException
	 */
	protected void setValues(DbPreparedStatement preparedStatement,
			DbValue[] values) throws WaarpDatabaseNoConnectionException,
			WaarpDatabaseSqlException {
		PreparedStatement ps = preparedStatement.getPreparedStatement();
		for (int i = 0; i < values.length; i++) {
			DbValue value = values[i];
			setTrueValue(ps, value, i + 1);
		}
	}

	/**
	 * Get one value into DbValue from ResultSet
	 * 
	 * @param rs
	 * @param value
	 * @throws WaarpDatabaseSqlException
	 */
	static public void getTrueValue(ResultSet rs, DbValue value)
			throws WaarpDatabaseSqlException {
		try {
			switch (value.type) {
				case Types.VARCHAR:
					value.value = rs.getString(value.column);
					break;
				case Types.LONGVARCHAR:
					value.value = rs.getString(value.column);
					break;
				case Types.BIT:
					value.value = rs.getBoolean(value.column);
					break;
				case Types.TINYINT:
					value.value = rs.getByte(value.column);
					break;
				case Types.SMALLINT:
					value.value = rs.getShort(value.column);
					break;
				case Types.INTEGER:
					value.value = rs.getInt(value.column);
					break;
				case Types.BIGINT:
					value.value = rs.getLong(value.column);
					break;
				case Types.REAL:
					value.value = rs.getFloat(value.column);
					break;
				case Types.DOUBLE:
					value.value = rs.getDouble(value.column);
					break;
				case Types.VARBINARY:
					value.value = rs.getBytes(value.column);
					break;
				case Types.DATE:
					value.value = rs.getDate(value.column);
					break;
				case Types.TIMESTAMP:
					value.value = rs.getTimestamp(value.column);
					break;
				case Types.CLOB:
					value.value = rs.getClob(value.column).getCharacterStream();
					break;
				case Types.BLOB:
					value.value = rs.getBlob(value.column).getBinaryStream();
					break;
				default:
					throw new WaarpDatabaseSqlException("Type not supported: " +
							value.type + " for " + value.column);
			}
		} catch (SQLException e) {
			DbSession.error(e);
			throw new WaarpDatabaseSqlException("Getting values in error: " +
					value.type + " for " + value.column, e);
		}
	}

	/**
	 * Get one value into DbValue from DbPreparedStatement
	 * 
	 * @param preparedStatement
	 * @param value
	 * @throws WaarpDatabaseNoConnectionException
	 * @throws WaarpDatabaseSqlException
	 */
	protected void getValue(DbPreparedStatement preparedStatement, DbValue value)
			throws WaarpDatabaseNoConnectionException, WaarpDatabaseSqlException {
		ResultSet rs = preparedStatement.getResultSet();
		getTrueValue(rs, value);
	}

	/**
	 * Get several values into DbValue from DbPreparedStatement
	 * 
	 * @param preparedStatement
	 * @param values
	 * @throws WaarpDatabaseNoConnectionException
	 * @throws WaarpDatabaseSqlException
	 */
	protected void getValues(DbPreparedStatement preparedStatement,
			DbValue[] values) throws WaarpDatabaseNoConnectionException,
			WaarpDatabaseSqlException {
		ResultSet rs = preparedStatement.getResultSet();
		for (DbValue value : values) {
			getTrueValue(rs, value);
		}
	}

	/**
	 * Get Values from PreparedStatement
	 * 
	 * @param preparedStatement
	 * @return True if OK, else False
	 */
	public boolean get(DbPreparedStatement preparedStatement) {
		try {
			getValues(preparedStatement, allFields);
			setFromArray();
		} catch (WaarpDatabaseNoConnectionException e1) {
			return false;
		} catch (WaarpDatabaseSqlException e1) {
			return false;
		}
		isSaved = true;
		return true;
	}
	/**
	 * 
	 * @return the runner as Json
	 * @throws OpenR66ProtocolBusinessException
	 */
	public String asJson() throws WaarpDatabaseSqlException {
		ObjectNode node;
		try {
			node = getJson();
		} catch (WaarpDatabaseSqlException e) {
			throw new WaarpDatabaseSqlException("Cannot read Data: " + e.getMessage());
		}
		return JsonHandler.writeAsString(node);
	}
	/**
	 * Create the equivalent object in Json (no database access)
	 * @return The ObjectNode Json equivalent
	 * @throws WaarpDatabaseSqlException
	 */
	public ObjectNode getJson() throws WaarpDatabaseSqlException {
		ObjectNode node = JsonHandler.createObjectNode();
		for (DbValue value : allFields) {
			if (value.column.equalsIgnoreCase("UPDATEDINFO")) {
				continue;
			}
			switch (value.type) {
				case Types.VARCHAR:
				case Types.LONGVARCHAR:
					node.put(value.column, (String) value.value);
					break;
				case Types.BIT:
					node.put(value.column, (Boolean) value.value);
					break;
				case Types.TINYINT:
					node.put(value.column, (Byte) value.value);
					break;
				case Types.SMALLINT:
					node.put(value.column, (Short) value.value);
					break;
				case Types.INTEGER:
					node.put(value.column, (Integer) value.value);
					break;
				case Types.BIGINT:
					node.put(value.column, (Long) value.value);
					break;
				case Types.REAL:
					node.put(value.column, (Float) value.value);
					break;
				case Types.DOUBLE:
					node.put(value.column, (Double) value.value);
					break;
				case Types.VARBINARY:
					node.put(value.column, (byte []) value.value);
					break;
				case Types.DATE:
					node.put(value.column, ((Date) value.value).getTime());
					break;
				case Types.TIMESTAMP:
					node.put(value.column, ((Timestamp) value.value).getTime());
					break;
				case Types.CLOB:
				case Types.BLOB:
				default:
					throw new WaarpDatabaseSqlException("Unsupported type: "+value.type);
			}
		}
		return node;
	}
	/**
	 * Set the values from the Json node to the current object (no database access)
	 * @param node
	 * @param ignorePrimaryKey True will ignore primaryKey from Json
	 * @throws WaarpDatabaseSqlException
	 */
	public void setFromJson(ObjectNode node, boolean ignorePrimaryKey) throws WaarpDatabaseSqlException {
		DbValue [] list = allFields;
		if (ignorePrimaryKey) {
			list = otherFields;
		}		
		for (DbValue value : list) {
			if (value.column.equalsIgnoreCase("UPDATEDINFO")) {
				continue;
			}
			JsonNode item = node.get(value.column);
			if (item != null && ! item.isMissingNode()) {
				isSaved = false;
				switch (value.type) {
					case Types.VARCHAR:
					case Types.LONGVARCHAR:
						value.setValue(item.asText());
						break;
					case Types.BIT:
						value.setValue(item.asBoolean());
						break;
					case Types.TINYINT:
					case Types.SMALLINT:
					case Types.INTEGER:
						value.setValue(item.asInt());
						break;
					case Types.BIGINT:
						value.setValue(item.asLong());
						break;
					case Types.REAL:
					case Types.DOUBLE:
						value.setValue(item.asDouble());
						break;
					case Types.VARBINARY:
						try {
							value.setValue(item.binaryValue());
						} catch (IOException e) {
							throw new WaarpDatabaseSqlException("Issue while assigning array of bytes", e);
						}
						break;
					case Types.DATE:
						value.setValue(new Date(item.asLong()));
						break;
					case Types.TIMESTAMP:
						value.setValue(new Timestamp(item.asLong()));
						break;
					case Types.CLOB:
					case Types.BLOB:
					default:
						throw new WaarpDatabaseSqlException("Unsupported type: "+value.type);
				}
			}
		}
		setFromArray();
	}
}
