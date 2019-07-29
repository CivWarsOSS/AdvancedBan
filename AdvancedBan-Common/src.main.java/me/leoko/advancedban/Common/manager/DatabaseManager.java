package me.leoko.advancedban.Common.manager;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mysql.cj.jdbc.MysqlDataSource;

import me.leoko.advancedban.Common.MethodInterface;
import me.leoko.advancedban.Common.Universal;
import me.leoko.advancedban.Common.utils.SQLQuery;

public class DatabaseManager {

	private String ip;
	private String dbName;
	private String usrName;
	private String password;
	private int port = 3306;
	private Connection connection;
	private boolean failedMySQL = false;
	private boolean useMySQL;

	private static DatabaseManager instance = null;

	public static DatabaseManager get() {
		return instance == null ? instance = new DatabaseManager() : instance;
	}
	
	private MysqlDataSource getDataSource() throws SQLException {
		MysqlDataSource dataSource = new MysqlDataSource();
		dataSource.setServerName(ip);
		dataSource.setPort(port);
		dataSource.setDatabaseName(dbName);
		dataSource.setUser(usrName);
		dataSource.setPassword(password);
		dataSource.setAutoReconnect(true);
		dataSource.setServerTimezone("UTC");
		dataSource.setReconnectAtTxEnd(true);
		
		return dataSource;
	}

	public void setup(boolean useMySQLServer) {
		MethodInterface mi = Universal.get().getMethods();

		if (useMySQLServer) {
			File file = mi.getMySQLFile();

			if (!file.exists()) {
				mi.createMySQLFile(file);
				failedMySQL = true;
			} else {
				mi.loadMySQLFile(file);
				
				ip = mi.getString(mi.getMysql(), "MySQL.IP", "Unknown");
				dbName = mi.getString(mi.getMysql(), "MySQL.DB-Name", "Unknown");
				usrName = mi.getString(mi.getMysql(), "MySQL.Username", "Unknown");
				password = mi.getString(mi.getMysql(), "MySQL.Password", "Unknown");
				port = mi.getInteger(mi.getMysql(), "MySQL.Port", 3306);

				connectMySQLServer();
			}
		}

		useMySQL = useMySQLServer && !failedMySQL;

		if (!useMySQL) {
			try {
				Class.forName("org.hsqldb.jdbc.JDBCDriver");
			} catch (ClassNotFoundException ex) {
				Universal.get().log("§cERROR: failed to load HSQLDB JDBC driver.");
				Universal.get().debug(ex.getMessage());
				return;
			}
			try {
				connection = DriverManager.getConnection(
						"jdbc:hsqldb:file:" + mi.getDataFolder().getPath() + "/data/storage;hsqldb.lock_file=false",
						"SA", "");
			} catch (SQLException ex) {
				Universal.get()
						.log(" \n" + " HSQLDB-Error\n" + " Could not connect to HSQLDB-Server!\n"
								+ " Disabling plugin!\n"
								+ " Issue tracker: https://github.com/DevLeoko/AdvancedBan/issues\n" + " \n");
			}
		}

		executeStatement(SQLQuery.CREATE_TABLE_PUNISHMENT);
		executeStatement(SQLQuery.CREATE_TABLE_PUNISHMENT_HISTORY);
	}

	public void shutdown() {
		try {
			if (!useMySQL) {
				connection.prepareStatement("SHUTDOWN").execute();
				connection.close();
			}
		} catch (SQLException ex) {
			Universal.get().log("An unexpected error has occurred turning off the database");
			Universal.get().debug(ex);
		}
	}

	private void connectMySQLServer() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			
			connection = getDataSource().getConnection();
		} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException exc) {
			Universal.get()
					.log(" \n" + " MySQL-Error\n" + " Could not connect to MySQL-Server!\n" + " Using HSQLDB (local)!\n"
							+ " Check your MySQL Config\n"
							+ " Issue tracker: https://github.com/ironboundred/AdvancedBan/issues \n" + " \n");
			Universal.get().log(exc.getMessage());
			failedMySQL = true;
		}
	}

	public void executeStatement(SQLQuery sql, Object... parameters) {
		executeStatement(sql, false, parameters);
	}

	public ResultSet executeResultStatement(SQLQuery sql, Object... parameters) {
		return executeStatement(sql, true, parameters);
	}

	private ResultSet executeStatement(SQLQuery sql, boolean result, Object... parameters) {
		return executeStatement(sql.toString(), result, parameters);
	}

	public ResultSet executeStatement(String sql, boolean result, Object... parameters) {
		if(!checkConnection()) {
			try {
				connection.isValid(1);
			} catch (SQLException e) {
				if(e.getErrorCode() != 0) {
					//The error was somthing other then connection issue
					Universal.get().debug(e);
				}
			}
		}
		
		try {
			PreparedStatement statement = connection.prepareStatement(sql);

			for (int i = 0; i < parameters.length; i++) {
				Object obj = parameters[i];
				if (obj instanceof Integer) {
					statement.setInt(i + 1, (Integer) obj);
				} else if (obj instanceof String) {
					statement.setString(i + 1, (String) obj);
				} else if (obj instanceof Long) {
					statement.setLong(i + 1, (Long) obj);
				} else {
					statement.setObject(i + 1, obj);
				}
			}

			if (result) {
				ResultSet resultSet = statement.executeQuery();
				return resultSet;
			} else {
				statement.execute();
				statement.close();
			}
			return null;
		} catch (SQLException ex) {
			Universal.get()
					.log("An unexpected error has ocurred executing an Statement in the database\n"
							+ "Please check the plugins/AdvancedBan/logs/latest.log file and report this"
							+ "error in: https://github.com/ironboundred/AdvancedBan/issues");
			Universal.get().debug("Query: \n" + sql);
			Universal.get().debug(ex);
			return null;
		}
	}
	
	private boolean checkConnection() {
		if(!useMySQL) {
			return true;
		}
		
		try {
			if(connection != null || !connection.isClosed()) {
				return true;
			}
		} catch (SQLException ex) {
			Universal.get().log("An unexpected error has occurred with the database.");
			Universal.get().debug(ex);
		}
		
		return false;
	}

	public boolean isFailedMySQL() {
		return failedMySQL;
	}

	public boolean isUseMySQL() {
		return useMySQL;
	}
}