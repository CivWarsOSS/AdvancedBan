package me.leoko.advancedban.manager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.hsqldb.jdbc.JDBCDataSource;

import com.mysql.cj.jdbc.MysqlDataSource;

import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;
import me.leoko.advancedban.utils.SQLQuery;

public class DatabaseManager {

	private String ip;
	private String dbName;
	private String usrName;
	private String password;
	private int port = 3306;
	//private Connection connection;
	private boolean failedMySQL = false;
	private boolean useMySQL;

	private static DatabaseManager instance = null;

	public static DatabaseManager get() {
		return instance == null ? instance = new DatabaseManager() : instance;
	}
	
	private MysqlDataSource getMysqlDataSource() throws SQLException {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
		}catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
			Universal.get().log("§cERROR: failed to load MySQL driver.");
			Universal.get().debug(ex.getMessage());
		}
		
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
	
	private JDBCDataSource getJDBCDataSource() throws SQLException{
		try {
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
		} catch (ClassNotFoundException ex) {
			Universal.get().log("§cERROR: failed to load HSQLDB JDBC driver.");
			Universal.get().debug(ex.getMessage());
		}
		
		MethodInterface mi = Universal.get().getMethods();
		JDBCDataSource dataSource = new JDBCDataSource();
		dataSource.setDatabase("jdbc:hsqldb:file:" + mi.getDataFolder().getPath() + "/data/storage");
		dataSource.setPassword("");
		dataSource.setUser("SA");
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

				try {
					Connection connection = getMysqlDataSource().getConnection();
					connection.close();
				} catch (SQLException exc) {
					Universal.get()
					.log(" \n" + " MySQL-Error\n" + " Could not connect to MySQL-Server!\n" + " Using HSQLDB (local)!\n"
							+ " Check your MySQL Config\n"
							+ " Issue tracker: https://github.com/ironboundred/AdvancedBan/issues \n" + " \n");
					Universal.get().log(exc.getMessage());
					failedMySQL = true;
				}
			}
		}

		useMySQL = useMySQLServer && !failedMySQL;

		if (!useMySQL) {
			try {
				Connection connection = getJDBCDataSource().getConnection();
				connection.close();
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

	public void executeStatement(SQLQuery sql, Object... parameters) {
		executeStatement(sql, false, parameters);
	}

	public Set<Punishment> executeResultStatement(SQLQuery sql, Object... parameters) {
		return executeStatement(sql, true, parameters);
	}

	private Set<Punishment> executeStatement(SQLQuery sql, boolean result, Object... parameters) {
		return executeStatement(sql.toString(), result, parameters);
	}

	public synchronized Set<Punishment> executeStatement(String sql, boolean result, Object... parameters) {
		Set<Punishment> punishments = new HashSet<>();

		if(useMySQL) {
			try {
				Connection connection = getMysqlDataSource().getConnection();
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
					punishments = buildResultSet(statement.executeQuery());
				} else {
					statement.execute();
				}
				
				statement.close();
				connection.close();
			} catch (SQLException ex) {
			
				Universal.get()
						.log("An unexpected error has ocurred executing an Statement in the database\n"
								+ "Please check the plugins/AdvancedBan/logs/latest.log file and report this"
								+ "error in: https://github.com/ironboundred/AdvancedBan/issues");
				Universal.get().debug("Query: \n" + sql);
				Universal.get().debug(ex);
			}
			
			return punishments;
		}else {
			try {
				Connection connection = getJDBCDataSource().getConnection();
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
					punishments = buildResultSet(statement.executeQuery());
				} else {
					statement.execute();
				}
				
				statement.close();
				connection.close();
			} catch (SQLException ex) {
				Universal.get()
						.log("An unexpected error has ocurred executing an Statement in the database\n"
								+ "Please check the plugins/AdvancedBan/logs/latest.log file and report this"
								+ "error in: https://github.com/ironboundred/AdvancedBan/issues");
				Universal.get().debug("Query: \n" + sql);
				Universal.get().debug(ex);
			}
			
			return punishments;
		}
	}

	public boolean isFailedMySQL() {
		return failedMySQL;
	}

	public boolean isUseMySQL() {
		return useMySQL;
	}
	
	private Set<Punishment> buildResultSet(ResultSet rs){
		Set<Punishment> results = new HashSet<>();
		
		try {
			while(rs.next()) {
				results.add(new Punishment(rs.getString("name"), rs.getString("uuid"), rs.getString("reason"),
						rs.getString("operator"), PunishmentType.valueOf(rs.getString("punishmentType")), rs.getLong("start"),
						rs.getLong("end"), rs.getString("calculation"), rs.getInt("id")));
			}
		} catch (SQLException ex) {
			Universal.get()
			.log("An unexpected error has ocurred reading results from the database\n"
					+ "Please check the plugins/AdvancedBan/logs/latest.log file and report this"
					+ "error in: https://github.com/ironboundred/AdvancedBan/issues");
			Universal.get().debug(ex);
		}
		
		return results;
	}
}