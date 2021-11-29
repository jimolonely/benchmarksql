/*
 * ExecJDBC - Command line program to process SQL DDL statements, from
 *             a text input file, to any JDBC Data Source
 *
 * Copyright (C) 2004-2016, Denis Lussier
 * Copyright (C) 2016, Jan Wieck
 *
 */

import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;

import javax.sql.DataSource;
import java.io.*;
import java.sql.*;
import java.util.*;


public class ExecJDBC {


  public static void main(String[] args) {

    Connection conn = null;
    Statement stmt = null;
    String rLine = null;
    StringBuffer sql = new StringBuffer();

    try {

    Properties ini = new Properties();
    ini.load( new FileInputStream(System.getProperty("prop")));

    // Register jdbcDriver
    Class.forName(ini.getProperty( "driver" ));

    String  ssJdbcYamlLocation  = ini.getProperty("ssJdbcYamlLocation");

    // make connection
    if (ssJdbcYamlLocation != null) {
        // 创建 ShardingSphereDataSource
        System.out.println("Creating ss datasource ..., jdbcLocation=" + ssJdbcYamlLocation);
        DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(new File(ssJdbcYamlLocation));
        conn = dataSource.getConnection();
    } else {
        conn = DriverManager.getConnection(ini.getProperty("conn"),
        ini.getProperty("user"),ini.getProperty("password"));
    }
    conn.setAutoCommit(true);

    // Create Statement
    stmt = conn.createStatement();

      // Open inputFile
      BufferedReader in = new BufferedReader
        (new FileReader(jTPCCUtil.getSysProp("commandFile",null)));

      // loop thru input file and concatenate SQL statement fragments
      while((rLine = in.readLine()) != null) {

         String line = rLine.trim();

         if (line.length() != 0) {
           if (line.startsWith("--")) {
              System.out.println(line);  // print comment line
           } else {
	       if (line.endsWith("\\;"))
	       {
	         sql.append(line.replaceAll("\\\\;", ";"));
		 sql.append("\n");
	       }
	       else
	       {
		   sql.append(line.replaceAll("\\\\;", ";"));
		   if (line.endsWith(";")) {
		      String query = sql.toString();

		      execJDBC(stmt, query.substring(0, query.length() - 1));
		      sql = new StringBuffer();
		   } else {
		     sql.append("\n");
		   }
	       }
           }

         } //end if

      } //end while

      in.close();

    } catch(IOException ie) {
        System.out.println(ie.getMessage());

    } catch(SQLException se) {
        System.out.println(se.getMessage());

    } catch(Exception e) {
        e.printStackTrace();

    //exit Cleanly
    } finally {
      try {
        if (conn !=null)
           conn.close();
      } catch(SQLException se) {
        se.printStackTrace();
      } // end finally

    } // end try

  } // end main


  static void execJDBC(Statement stmt, String query) {

    System.out.println(query + ";");

    try {
      stmt.execute(query);
    }catch(SQLException se) {
      System.out.println(se.getMessage());
    } // end try

  } // end execJDBCCommand

} // end ExecJDBC Class
