package com.orphanagehub.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager() {

  private static String connectionUrl;

  public static Connection getConnection() throws SQLException() {
    if(connectionUrl == null) {
      try {
        Properties props = new Properties();
        try(InputStream input =
            DatabaseManager.class.getClassLoader().getResourceAsStream("app.properties")) {
          if(input == null) {
            throw new IOException("app.properties not found");
          }
          props.load(input);
          // Assuming app.properties has 'db.url' key - adjust if different
          connectionUrl = props.getProperty("db.url");
          if(connectionUrl == null) {
            throw new IllegalStateException("db.url not defined in app.properties");
          }
        }
      } catch(IOException e) {
        throw new SQLException("Failed to load database properties", e);
      }
    }
    // Establish and return the connection
    return DriverManager.getConnection(connectionUrl);
  }

  // Optional: Method to close connection(call this when done)
  public static void closeConnection(Connection conn) {
    if(conn != null) {
      try {
        conn.close();
      } catch(SQLException e) {
        // Log or handle error(add logging if needed)
        e.printStackTrace();
      }
    }
  }

  // Example: Test method(remove or use for debugging)
  public static void main(String[] args) {
    try {
      Connection conn = getConnection();
      System.out.println("Connection successful!");
      closeConnection(conn);
    } catch(SQLException e) {
      e.printStackTrace();
    }
  }
}