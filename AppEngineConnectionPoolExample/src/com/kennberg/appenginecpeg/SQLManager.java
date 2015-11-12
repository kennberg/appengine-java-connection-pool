/**
 * Google App Engine Connection Pool Example
 *
 * Copyright 2015 Alex Kennberg (https://github.com/kennberg/appengine-java-connection-pool)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kennberg.appenginecpeg;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.apache.commons.dbcp.BasicDataSource;

public class SQLManager {
  private static final Logger LOG = Logger.getLogger(SQLManager.class.getName());

  private static final int MIN_CONNECTION_POOL_SIZE = 2;
  private static final int MAX_CONNECTION_POOL_SIZE = 5;
  
  // Connection pool will start throwing errors when connections take more than
  // this amount to execute and there are no available spots for new connections.
  private static final long MAX_CONNECTION_WAIT_MS = 30000L;
  
  // This datasource lives as long as the instance lives.
  private static BasicDataSource dataSource = null;
  private static final Object dataSourceLock = new Object();

  private String driverClassName;
  private boolean useConnectionPool;
  private String address;
  private String username;
  private String password;
  
  
  public SQLManager(final String driverClassName, final boolean useConnectionPool,
      final String address, final String username, final String password) {
    this.driverClassName = driverClassName;
    this.useConnectionPool = useConnectionPool;
    this.address = address;
    this.username = username;
    this.password = password;
    
    try {
      Class.forName(driverClassName);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Cannot find the SQL driver in the classpath." +
          " driver=" + driverClassName, e);
    }
  }
  
  
  public Connection getConnection() throws SQLException {
    if (!useConnectionPool) {
      if (username == null) {
        return DriverManager.getConnection(address);
      } else {
        return DriverManager.getConnection(address, username, password);
      }
    }
    
    synchronized (dataSourceLock) {
      if (dataSource == null) {
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(address);
        if (username != null) {
          dataSource.setUsername(username);
          dataSource.setPassword(password);
        }
        dataSource.setInitialSize(MIN_CONNECTION_POOL_SIZE);
        dataSource.setMaxActive(MAX_CONNECTION_POOL_SIZE);
        dataSource.setMaxIdle(MAX_CONNECTION_POOL_SIZE);
        dataSource.setMaxWait(MAX_CONNECTION_WAIT_MS);

        dataSource.setTestOnBorrow(true);
        dataSource.setValidationQuery("SELECT 1");

        // From BasicDataSourceFactory.java, DBCP-215
        // Trick to make sure that initialSize connections are created
        if (dataSource.getInitialSize() > 0) {
          dataSource.getLogWriter();
        }
      }

      return dataSource.getConnection();
    } // synchronized dataSourceLock
  }
}
