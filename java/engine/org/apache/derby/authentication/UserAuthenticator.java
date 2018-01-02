package org.apache.derby.authentication;

import java.util.Properties;
import java.sql.SQLException;

/**
 * The UserAuthenticator interface provides operations to authenticate
 * a user's credentials in order to successfully connect to a database.
 * 这个接口用来提供用户登录数据库时的认证操作,
 * Any user authentication schemes could be implemented using this interface
 * and registered at start-up time.
 * 任何用户认证schemes都可以用这个接口实现在启动时注册
 * If an application requires its own authentication scheme, then it can
 * implement this interface and register as the authentication scheme
 * that Derby should call upon connection requests to the system.
   See the dcoumentation for the property <I>derby.authentication.provider</I>
 * 如果一个应用需要自己的认证方案
 * A typical example would be to implement user authentication using
 * LDAP, Sun NIS+, or even Windows User Domain, using this interface.
 * 例如:LDAP, Sun NIS+, or even Windows User Domain都是用的这个接口
 * <i>Note</i>: Additional connection attributes can be specified on the 
 * database connection URL and/or Properties object on jdbc connection. Values
 * for these attributes can be retrieved at runtime by the (specialized)
 * authentication scheme to further help user authentication, if one needs
 * additional info other than user, password, and database name.
 *
 *
 */

public interface UserAuthenticator//UserAuthenticator
{
	
	/**
	 * Authenticate a user's credentials.
     * <BR>
     * E.g. if connection url is 
     * <code>jdbc:derby:testdb;user=Fred;password=ScT7dmM2</code>
     * then the userName will be Fred and within the Derby user authorization 
     * system, Fred becomes a case-insensitive authorization identifier and 
     * is known as FRED
     * 若干db连接中user=后用户名没用双引号括起来,
	 * 说明这用户名是大小写不敏感的,认证时会被认为是其大写
     * if connection url is 
     * <code>jdbc:derby:testdb;user="Fred";password=ScT7dmM2</code>
     * then the userName will be "Fred" and within the Derby user authorization
     * system, Fred becomes a case-sensitive authorization identifier and is
     * known as Fred
     * <BR>
	 *
	 * @param userName		The user's name for the connection request. May be 
     *                      null.  The user name is passed in as is from the 
     *                      connection request.
     *                      Derby will pass in the user name that is set on
     *                      connection request as is, without changing the 
     *                      casing and without removing the delimiter quotes 
     *                      if any.
     *
	 * @param userPassword	The user's password for the connection request. 
     *                      May be null.
     *
	 * @param databaseName	The database that the user wants to connect to.
	 *						Will be null if this is system level authentication.
     *
	 * @param info			A Properties object that contains additional 
     *                      connection information, that can help to 
     *                      authenticate the user. It has properties of the 
     *                      'info' object passed as part of 
     *                      DriverManager.getConnection() call and any 
     *                      attributes set on the JDBC URL.
     *
     * @return	false if the connection request should be denied, true if the 
     *          connection request should proceed.  If false is returned the 
     *          connection attempt will receive a SQLException with SQL State 
     *          08004.
	 *
	 * @exception java.sql.SQLException An exception processing the request, 
     *            connection request will be denied.  The SQL exception will 
     *            be returned to the connection attempt.
	 */
	public boolean	authenticateUser(String userName,
								 String userPassword,
								 String databaseName,
								 Properties info
								)
		throws SQLException;
}
