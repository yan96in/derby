/*
 *
 * Derby - Class TestConfiguration
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific 
 * language governing permissions and limitations under the License.
 */
package org.apache.derbyTesting.junit;

import java.security.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.derbyTesting.functionTests.util.TestDataSourceFactory;

/**
 * Class which holds information about the configuration of a Test.
 */
public class TestConfiguration {

    /**
     * Default Derby test configuration object.
     */
    private static final TestConfiguration DERBY_TEST_CONFIG = 
        new TestConfiguration(getSystemProperties());
    
    /**
     * Current configuration is stored in a ThreadLocal to
     * allow the potential for multiple tests to be running
     * concurrently with different configurations.
     */
    private static final ThreadLocal CURRENT_CONFIG = new ThreadLocal() {
        protected Object initialValue() {
            return DERBY_TEST_CONFIG;
        }
    };
   
    /**
     * Get this Thread's current configuraiton for running
     * the tests.
     * @return TestConfiguration to use.
     */
    public static TestConfiguration getCurrent() {
        return (TestConfiguration) CURRENT_CONFIG.get();
    }
    
    /**
     * Set this Thread's current configuration for running tests.
     * @param config Configuration to set it to.
     */
    private static void setCurrent(TestConfiguration config)
    {
        CURRENT_CONFIG.set(config);
    }
    
    /**
     * This constructor creates a TestConfiguration from a Properties object.
     *
     * @throws NumberFormatException if the port specification is not an integer.
     */
    private TestConfiguration(Properties props) 
        throws NumberFormatException {
		systemStartupProperties = props;
        dbName = props.getProperty(KEY_DBNAME, DEFAULT_DBNAME);
        userName = props.getProperty(KEY_USER_NAME, DEFAULT_USER_NAME);
        userPassword = props.getProperty(KEY_USER_PASSWORD, 
                                         DEFAULT_USER_PASSWORD);
        hostName = props.getProperty(KEY_HOSTNAME, DEFAULT_HOSTNAME);
        isVerbose = Boolean.valueOf(props.getProperty(KEY_VERBOSE)).booleanValue();
        String portStr = props.getProperty(KEY_PORT);
        singleLegXA = Boolean.valueOf(props.getProperty(KEY_SINGLE_LEG_XA)
                            ).booleanValue();
        if (portStr != null) {
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException nfe) {
                // We lose stacktrace here, but it is not important. 
                throw new NumberFormatException(
                        "Port number must be an integer. Value: " + portStr); 
            }
        } else {
            port = DEFAULT_PORT;
        }
        
        String framework = props.getProperty(KEY_FRAMEWORK, DEFAULT_FRAMEWORK);
        
        if ("DerbyNetClient".equals(framework)) {
            jdbcClient = JDBCClient.DERBYNETCLIENT;
        } else if ("DerbyNet".equals(framework)) {
            jdbcClient = JDBCClient.DB2CLIENT;
        } else {
            jdbcClient = JDBCClient.EMBEDDED;
        }
        url = createJDBCUrlWithDatabaseName(dbName);
    }

    /**
     * Get the given system property as specified at startup.
     */
	public	String	getSystemStartupProperty( String key )
	{
		return systemStartupProperties.getProperty( key );
	}

    /**
     * Get the system properties in a privileged block.
     *
     * @return the system properties.
     */
    private static final Properties getSystemProperties() {
        // Fetch system properties in a privileged block.
        Properties sysProps = (Properties)AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        return System.getProperties();
                    }
                });
        return sysProps;
    }

    /**
     * Create JDBC connection url, including the name of the database.
     *
     * @return JDBC connection url, without attributes.
     */
    private String createJDBCUrlWithDatabaseName(String name) {
        if (jdbcClient == JDBCClient.EMBEDDED) {
            return jdbcClient.getUrlBase() + name;
        } else {
            return jdbcClient.getUrlBase() + hostName + ":" + port + "/" + name;
        }
    }

    /**
     * Get configured JDBCClient object.
     *
     * @return JDBCClient
     */
    public JDBCClient getJDBCClient() {
        return jdbcClient;
    }
    
    
    /**
     * Return the jdbc url for connecting to the default database.
     *
     * @return JDBC url.
     */
    public String getJDBCUrl() {
        return url;
    }

    /**
     * Return the jdbc url for a connecting to the database.
     * 
     * @param databaseName name of database.
     * @return JDBC connection url, including database name.
     */
    public String getJDBCUrl(String databaseName) {
        return createJDBCUrlWithDatabaseName(databaseName);
    }
    
    /**
     * Return the default database name.
     * 
     * @return default database name.
     */
    public String getDatabaseName() {
        return dbName;
    }
    
    /**
     * Return the user name.
     * 
     * @return user name.
     */
    public String getUserName() {
        return userName;
    }
    
    /**
     * Return the user password.
     * 
     * @return user password.
     */
    public String getUserPassword() {
        return userPassword;
    }

    /**
     * Return the host name for the network server.
     *
     * @return host name.
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * Get port number for network server.
     * 
     * @return port number.
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Open connection to the default database.
     * If the database does not exist, it will be created.
     * A default username and password will be used for the connection.
     *
     * @return connection to default database.
     */
    public Connection openDefaultConnection()
        throws SQLException {
        return openConnection(getDatabaseName());
    }
    
    /**
     * Open a connection to a database.
     * If the database does not exist, it will be created.
     * A default username and password will be used for the connection.
     *
     * @param databaseName database to connect to
     *
     * @return connection to database.
     */
    public Connection openConnection (String databaseName) throws SQLException {
        Connection con = null;
        JDBCClient client =getJDBCClient();
        if (JDBC.vmSupportsJDBC2()) {            
            loadJDBCDriver(client.getJDBCDriverName());
            if (!isSingleLegXA()) {
                con = DriverManager.getConnection(
                        getJDBCUrl(databaseName) + ";create=true",
                        getUserName(),
                        getUserPassword());
            }
            else {
                Properties attrs = 
                        getDataSourcePropertiesForDatabase(databaseName);
                con = TestDataSourceFactory.getXADataSource(attrs).
                        getXAConnection (getUserName(), 
                        getUserPassword()).getConnection();
            }
        } else {
            //Use DataSource for JSR169
            Properties attrs = getDataSourcePropertiesForDatabase(databaseName);
            con = TestDataSourceFactory.getDataSource(attrs).getConnection();
        }
        return con;
    }
    
    /**
     * Set the verbosity, i.e., whether debug statements print.
     */
    public void	setVerbosity( boolean isChatty )	{ isVerbose = isChatty; }
    
    /**
     * Return verbose flag.
     *
     * @return verbose flag.
     */
    public boolean isVerbose() {
        return isVerbose;
    }

	/**
	 * <p>
	 * Return true if we classes are being loaded from jar files. For the time
	 * being, this simply tests that the JVMInfo class (common to the client and
	 * the server) comes out of a jar file.
	 * </p>
	 */
	public	boolean	loadingFromJars()
	{
		//
		// jvm.java sets this property to the build jar directory
		// if we are using derbyTesting.jar.
		//
		
		return ( !UNUSED.equals( getSystemStartupProperty( "derbyTesting.codejar" ) ) );
	}

    /**
     * Return if it has to run under single legged xa transaction
     * @return singleLegXA
     */
    public boolean isSingleLegXA () {
        return singleLegXA;
    }
    
    /**
     * Immutable data members in test configuration
     */
	private	final Properties systemStartupProperties;
    private final String dbName;
    private final String url;
    private final String userName; 
    private final String userPassword; 
    private final int port;
    private final String hostName;
    private final JDBCClient jdbcClient;
    private boolean isVerbose;
    private final boolean singleLegXA;
    
    /**
     * Default values for configurations
     */
    private final static String DEFAULT_DBNAME = "wombat";
    private final static String DEFAULT_USER_NAME = "APP";
    private final static String DEFAULT_USER_PASSWORD = "APP";
    private final static int    DEFAULT_PORT = 1527;
    private final static String DEFAULT_FRAMEWORK = "embedded";
    private final static String DEFAULT_HOSTNAME = "localhost";
            
    /**
     * Keys to use to look up values in properties files.
     */
    private final static String KEY_DBNAME = "databaseName";
    private final static String KEY_FRAMEWORK = "framework";
    private final static String KEY_USER_PASSWORD = "password";
    private final static String KEY_USER_NAME = "user";
    private final static String KEY_HOSTNAME = "hostName";
    private final static String KEY_PORT = "port";
    private final static String KEY_VERBOSE = "derby.tests.debug";    
    private final static String KEY_SINGLE_LEG_XA = "derbyTesting.xa.single";

    /**
     * Possible values of system properties.
     */
    private final static String UNUSED = "file://unused/";

    /**
     * Generate properties which can be set on a
     * <code>DataSource</code> in order to connect to the default
     * database.
     *
     * @return a <code>Properties</code> object containing server
     * name, port number, database name and other attributes needed to
     * connect to the default database
     */
    public static Properties getDefaultDataSourceProperties() {
        return getDataSourcePropertiesForDatabase(
                DERBY_TEST_CONFIG.getDatabaseName());
    }
    
    /**
     * Generate properties which can be set on a
     * <code>DataSource</code> in order to connect to a given
     * database.
     *
     * @param databaseName database to connect to
     *
     * @return a <code>Properties</code> object containing server
     * name, port number, database name and other attributes needed to
     * connect to the database
     */
    public static Properties getDataSourcePropertiesForDatabase
            (String databaseName) 
    {
        Properties attrs = new Properties();
        if (!(DERBY_TEST_CONFIG.getJDBCClient() == JDBCClient.EMBEDDED)) {
            attrs.setProperty("serverName", DERBY_TEST_CONFIG.getHostName());
            attrs.setProperty("portNumber", Integer.toString(DERBY_TEST_CONFIG.getPort()));
        }
        attrs.setProperty("databaseName", databaseName);
        attrs.setProperty("connectionAttributes", "create=true");
        return attrs;
    }

    /**
     * Load the specified JDBC driver
     *
     * @param driverClass name of the JDBC driver class.
     * @throws SQLException if loading the driver fails.
     */
    private static void loadJDBCDriver(String driverClass) 
        throws SQLException {
        try {
            Class.forName(driverClass).newInstance();
        } catch (ClassNotFoundException cnfe) {
            throw new SQLException("Failed to load JDBC driver '" + 
                                    driverClass + "': " + cnfe.getMessage());
        } catch (IllegalAccessException iae) {
            throw new SQLException("Failed to load JDBC driver '" +
                                    driverClass + "': " + iae.getMessage());
        } catch (InstantiationException ie) {
            throw new SQLException("Failed to load JDBC driver '" +
                                    driverClass + "': " + ie.getMessage());
        }
    }
    
    /*
     * SecurityManager related configuration.
     */
    
    /**
     * Install the default security manager setup,
     * for the current configuration.
     * @throws PrivilegedActionException 
     */
    boolean defaultSecurityManagerSetup() throws PrivilegedActionException {
    	
    	// Testing with the DB2 client has not been performed
    	// under the security manager since it's not part
    	// of Derby so no real interest in tracking down issues.
    	if (jdbcClient.isDB2Client()) {
    		SecurityManagerSetup.noSecurityManager();
    		return false;
    	} else {
    		SecurityManagerSetup.installSecurityManager();
    		return true;
    	}
    }
    
        
}
