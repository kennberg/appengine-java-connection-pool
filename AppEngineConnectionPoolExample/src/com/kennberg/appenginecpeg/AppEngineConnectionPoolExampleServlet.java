package com.kennberg.appenginecpeg;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.utils.SystemProperty;

@SuppressWarnings("serial")
public class AppEngineConnectionPoolExampleServlet extends HttpServlet {
  private static final Logger LOG = Logger.getLogger(AppEngineConnectionPoolExampleServlet.class.getName());
  
  private static final boolean USE_CONNECTION_POOL = true;

  public static final String CLOUD_SQL_PROD_ADDRESS =
      "jdbc:google:mysql://APPENGINE_PROJECT:CLOUD_SQL_INSTANCE/DATABASE_NAME?user=root";
  public static final String SQL_DEV_ADDRESS =
      "jdbc:mysql://localhost:3306/DATABASE_NAME";
  public static final String SQL_DEV_USERNAME = "DATABASE_USER";
  public static final String SQL_DEV_PASSWORD = "DATABASE_PASS";

  
  @SuppressWarnings("unused")
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/plain");
    
    // Setup SQL manager for our application depending on environment.
    SQLManager sqlManager = null;
    if (SystemProperty.environment.value() ==
        SystemProperty.Environment.Value.Production) {
      sqlManager = new SQLManager("com.mysql.jdbc.GoogleDriver", USE_CONNECTION_POOL,
          CLOUD_SQL_PROD_ADDRESS, null /* username */, null /* password */);
    } else {
      sqlManager = new SQLManager("com.mysql.jdbc.Driver", USE_CONNECTION_POOL,
          SQL_DEV_ADDRESS, SQL_DEV_USERNAME, SQL_DEV_PASSWORD);      
    }
    
    if (sqlManager == null) {
      resp.getWriter().println("Failed connecting to the database.");
      return;
    }
    
    sampleQuery(sqlManager, resp);
  }


  private void sampleQuery(final SQLManager sqlManager, final HttpServletResponse resp)
      throws IOException {
    // Establish the connection. Note for conenction pool this will re-use
    // an existing connection whenever possible. At the end of the try-catch
    // sql.close() is called automatically which gives the connection back to
    // the pool.
    try (Connection sql = sqlManager.getConnection()) {

      // Execute the query.
      final String query = "SELECT 'Hello, world'";
      try (PreparedStatement statement = sql.prepareStatement(query)) {
        
        // Gather results.
        try (ResultSet result = statement.executeQuery()) {
          if (result.next()) {
            resp.getWriter().println(result.getString(1));
          }
        }
      }
      
    } catch (SQLException e) {
      LOG.log(Level.SEVERE, "SQLException", e);
      resp.getWriter().println("Failed to execute database query.");
    }
  }
}
