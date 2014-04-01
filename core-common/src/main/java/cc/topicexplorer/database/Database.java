package cc.topicexplorer.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Database {
	private Connection connection;
	private Statement statement;
	private Properties properties;

	private int limit = -1; // limit for select queries
	private int numberOfRetries = 3; // this number gets overwritten by respective property
	
	private String dbUser; 
	private String dbPassword;
	private String dbLocation;
	
	private final Logger logger = Logger.getRootLogger();

	/**
	 * 
	 * @param prop
	 *            A {@link Properties} object which holds all the properties,
	 *            needed for the database to initialize
	 * @throws SQLException
	 *             if a database access error occurs
	 * @throws ClassNotFoundException
	 *             if the database driver could not be found
	 * @throws InstantiationException
	 *             if the instantiation fails for multiple reasons
	 * @throws IllegalAccessException
	 *             if the database driver or its default constructor is not
	 *             accessible
	 */
	public Database(Properties prop) {
		startUp(prop, false);
	}

	/**
	 * 
	 * @param prop
	 *            A {@link Properties} object which holds all the properties,
	 *            needed for the database to initialize
	 * @param otherDatabase
	 *            determines whether the another database than the default one
	 *            should be loaded. Property 'database.other' will be used to
	 *            define the dbLocation of the other database
	 * @throws SQLException
	 *             if a database access error occurs
	 * @throws ClassNotFoundException
	 *             if the database driver could not be found
	 * @throws InstantiationException
	 *             if the instantiation fails for different possible reasons
	 * @throws IllegalAccessException
	 *             if the database driver or its default constructor is not
	 *             accessible
	 */
	public Database(Properties prop, Boolean otherDatabase) {
		startUp(prop, otherDatabase);
	}

	private void startUp(Properties prop, Boolean otherDatabase) {
		this.properties = prop;

		this.setProperties(otherDatabase);
		this.loadDatabaseDriver();
		this.connect();
		this.prepareConnection();
	}

	private void prepareConnection() {
		try {
			this.statement = this.connection.createStatement();
			this.statement.setFetchSize(100);
		} catch (SQLException e) {
			this.logger
					.error("The statement caused an exception. SQL-State-Error-Code: "
							+ e.getSQLState());
			throw new RuntimeException(e);
		}

		try {
			this.connection.setAutoCommit(true);
		} catch (SQLException e) {
			this.logger.error("AutoCommit could not be set.");
			throw new RuntimeException(e);
		}
		
	}

	private void setProperties(Boolean otherDatabase) {
		this.dbLocation = this.properties.getProperty("database.DbLocation");

		if (otherDatabase) {
			dbLocation = dbLocation + "/"
					+ this.properties.getProperty("database.other");
		} else {
			dbLocation = dbLocation + "/"
					+ this.properties.getProperty("database.DB");
		}

		this.dbUser = this.properties.getProperty("database.DbUser");
		this.dbPassword = this.properties.getProperty("database.DbPassword");
		
		this.numberOfRetries = Integer.parseInt(this.properties.getProperty("database.NumberOfRetries"));
	}

	private void loadDatabaseDriver() {
		// load Database-Driver
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (ClassNotFoundException e1) {
			this.logger.error("Current Command : [ " + getClass() + " ]"
					+ " Database-Driver class could not be found");
			throw new RuntimeException(e1);
		} catch (InstantiationException e2) {
			this.logger
					.error("Current Command : [ "
							+ getClass()
							+ " ]"
							+ " Database-Driver instantiation fails for different possible reasons");
			throw new RuntimeException(e2);
		} catch (IllegalAccessException e3) {
			this.logger
					.error("Current Command : [ "
							+ getClass()
							+ " ]"
							+ " Database-Driver database driver or its default constructor is not accessible");
			throw new RuntimeException(e3);
		}
	}

	/**
	 * 
	 * @throws SQLException
	 *             if a database access error occurs. See the MySQL
	 *             specification for detailed information on the error code
	 */
	public void shutdownDB() throws SQLException {
		if (!this.connection.isClosed()) {
			this.connection.close();
		}
	}

	/**
	 * @param query
	 *            SQL query that will be executed
	 * @return a ResultSet object that contains the data produced by the given
	 *         query; never null
	 * @throws SQLException
	 *             if a database access error occurs,
	 *             <p>
	 *             this method is called on a closed statement,
	 *             <p>
	 *             the given SQL statement produces anything other than a single
	 *             ResultSet object,
	 *             <p>
	 *             the method is called on a PreparedStatement or
	 *             CallableStatement or
	 *             <p>
	 *             See the MySQL specification for detailed information on the
	 *             error code
	 */
	public ResultSet executeQuery(String query) throws SQLException {
		this.statement = this.connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		return this.statement.executeQuery(query);
	}

	/**
	 * for manipulation of database without "closing" the connection/ resultset
	 * for insert and update in loops
	 * 
	 * incl statement = connection..
	 * 
	 * @param query
	 *            SQL query that will be executed
	 * @return either (1) the row count for SQL Data Manipulation Language (DML)
	 *         statements or (2) 0 for SQL statements that return nothing
	 * @throws SQLException
	 *             if a database access error occurs,
	 *             <p>
	 *             this method is called on a closed statement,
	 *             <p>
	 *             the given SQL statement produces a ResultSet object,
	 *             <p>
	 *             the method is called on a PreparedStatement or
	 *             CallableStatement or
	 *             <p>
	 *             See the MySQL specification for detailed information on the
	 *             error code
	 */
	public int executeUpdateQueryForUpdate(String query) throws SQLException {
		// damit das RS offen bleibt, sonst funktionierten die alten
		// keywords_themen2 bzw tables.keytopic2 nicht
		// da es zur exception gekommen ist (cannot perfom ... rs is closed)
		this.statement = this.connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		return this.statement.executeUpdate(query);
	}

	/**
	 * for manipulation of database incl. "closing" connection/ resultset excl.
	 * statement
	 * <p>
	 * different to executeUpdateQueryForUpdate()
	 * 
	 * @param query
	 *            SQL query that will be executed
	 * @return row count or 0 for SQL statements that return nothing
	 * @throws SQLException
	 *             if a database access error occurs, the given SQL query
	 *             produces a {@link ResultSet} object or if the
	 *             {@link Statement} used for updating is already closed. See
	 *             the MySQL specification for detailed information on the error
	 *             code.
	 */
	public int executeUpdateQuery(String query) throws SQLException {
	
		boolean queryCompleted = false;
		int retryCount = this.numberOfRetries;
		int result = -1;
		do {
		try {
			// for manipulation , executeQuery couldn't manipulate database
			result = this.statement.executeUpdate(query);
			queryCompleted = true;
		} catch (SQLException e) {
			//
            // 'Retry-able' SQL-State is 08S01 for Communication Error
            // Retry only if error due to stale or dead connection
            //

            String sqlState = e.getSQLState();

            if ("08S01".equals(sqlState)) {
                retryCount--;
                connection.close();
                this.connect();
                this.prepareConnection();
            } else {
                retryCount = 0;
                this.logger.error("The statement "+ query +" caused a SQLException exception that is not retryable. "
                		+ "Error-Code: "+ e.getErrorCode()+ ", SQL-State-Error-Code: " + e.getSQLState());
//                throw new RuntimeException(e);
//                throw new SQLException(e);
                throw e;
            }
		}
		} while(!queryCompleted && (retryCount > 0) );
		
		if(!queryCompleted && (retryCount == 0) ) {
            this.logger.error("The statement  "+ query +" caused an retryable SQL exception and was " + this.numberOfRetries + " times retried. "
            		+ "Retryable Exceptions are those with SQL State 08S01.");
            throw new RuntimeException();
		}
		
		if(result < 0) {
            this.logger.error("The statement  "+ query +" returned a rowcount that is smaller than zero.");
            throw new RuntimeException();
		}
		return result;
	}

	public Connection getConnection() {
		return this.connection;
	}

	public Properties getProperties() {
		return this.properties;
	}

	public int getLimit() {
		return this.limit;
	}

	public void setLimit(int value) {
		this.limit = value;
	}

	/**
	 * @param tableName
	 *            name of the table which will be dropped
	 * @throws SQLException
	 *             if a database access error occurs, the given SQL query
	 *             produces a {@link ResultSet} object or if the
	 *             {@link Statement} used for updating is already closed. See
	 *             the MySQL specification for detailed information on the error
	 *             code.
	 **/
	public void dropTable(String tableName) throws SQLException {
		this.executeUpdateQuery("DROP TABLE IF EXISTS " + tableName);
	}

	/**
	 * @throws SQLException
	 *             if a database access error occurs, the analyze
	 *             {@link Statement} produces a {@link ResultSet object or if
	 *             the {@link Statement} used for updating is already closed.
	 *             See the MySQL specification for detailed information on the
	 *             error code.
	 */
	public void analyzeTable(String tableName) throws SQLException {
		this.executeUpdateQuery("ANALYZE TABLE  " + tableName);
	}

	/**
	 * @throws SQLException
	 *             if a database access error occurs, the optimize
	 *             {@link Statement} produces a {@link ResultSet object or if
	 *             the {@link Statement} used for updating is already closed.
	 *             See the MySQL specification for detailed information on the
	 *             error code.
	 */
	public void optimizeTable(String tableName) throws SQLException {
		this.executeUpdateQuery("OPTIMIZE TABLE  " + tableName);
	}

}
