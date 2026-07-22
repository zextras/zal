package org.openzal.zal.db;

/**
 * Zimbra Collaboration Suite Server
 */

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.db.DbMailbox;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.HSQLDB;
import org.hsqldb.cmdline.SqlFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

public final class HSQLZimbraDatabase extends HSQLDB
{

  /**
   * Executes db.sql and create_database.sql scripts to populate the database.
   *
   * Populates ZIMBRA and MBOXGROUP1 schema.
   * @param basePath path where db.sql and create_database.sql are found.
   *
   * @throws Exception
   */
  public static void createDatabase(String basePath) throws Exception {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    com.zimbra.cs.db.DbPool.DbConnection  conn = DbPool.getConnection();

    try {
      stmt = conn.prepareStatement("SET DATABASE SQL SYNTAX MYS TRUE");
      stmt.execute();
      stmt = conn.prepareStatement("SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = ?");
      stmt.setString(1, "ZIMBRA");
      rs = stmt.executeQuery();
      if (rs.next() && rs.getInt(1) > 0) {
        return;  // already exists
      }
      executeFromClasspath(conn, basePath + "/db.sql");
      executeForAllGroups(conn, basePath + "/create_database.sql");
    } finally {
      DbPool.closeResults(rs);
      DbPool.quietCloseStatement(stmt);
      DbPool.quietClose(conn);
    }
  }

  public static void createDatabase() throws Exception {
    createDatabase("/dbsetup");
  }

  //
  // Deletes all records from all tables.
  // @param zimbraServerDir the directory that contains the ZimbraServer project
  // @throws Exception
  //
  public static void clearDatabase() throws Exception
  {
    clearDatabase("/dbsetup/clear.sql");
  }

  /**
   * Executes a clear script for all mailbox groups
   * @param clearSqlScript sql script containing clear instructions
   * @throws Exception
   */
  private static void clearDatabase(String clearSqlScript) throws Exception
  {
    com.zimbra.cs.db.DbPool.DbConnection conn = DbPool.getConnection();
    try {
      executeForAllGroups(
          conn,
          clearSqlScript
      );
    } finally {
      DbPool.quietClose(conn);
    }
  }

  private static void executeForAllGroups(com.zimbra.cs.db.DbPool.DbConnection conn, String classpathFile) throws Exception
  {
    for( int i=1; i <= LC.zimbra_mailbox_groups.intValue(); ++i ) executeFromClasspath(conn, classpathFile, i);
  }

  interface TempFileRunner {
    void run(File file) throws Exception;
  }

  private static void withTempFile(String classpathFile, TempFileRunner runner) throws Exception {
    File f = null;
    try {
      String content = readResource(classpathFile);
      Path tempFile = Files.createTempFile("zal-dbsetup", ".tmp");
      Files.writeString(tempFile, content);
      f = tempFile.toFile();
      runner.run(f);
    } finally {
      if (f != null && f.exists() && !f.delete()) {
        f.deleteOnExit();
      }
    }
  }

  private static String readResource(String path) throws IOException {
    try (InputStream is = HSQLZimbraDatabase.class.getResourceAsStream(path)) {
      if (is == null) {
        throw new IOException("Resource not found on classpath: " + path);
      }
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  private static void executeFromClasspath(DbPool.DbConnection conn, String classpathFile, int mboxId) throws Exception
  {
    withTempFile(classpathFile, file -> {
      Map<String, String> vars = Collections.singletonMap("DATABASE_NAME", DbMailbox.getDatabaseName(mboxId));
      SqlFile sql = new SqlFile(file);
      sql.addUserVars(vars);
      sql.setConnection(conn.getConnection());
      sql.execute();
      conn.commit();
    });
  }

  public static void executeFromClasspath(DbPool.DbConnection conn, String classpathFile) throws Exception
  {
    executeFromClasspath(conn,classpathFile,1);
  }

  private static void execute(DbPool.DbConnection conn, String file, int mboxId) throws Exception
  {
    Map<String, String> vars = Collections.singletonMap("DATABASE_NAME", DbMailbox.getDatabaseName(mboxId));
    SqlFile sql = new SqlFile(new File(file));
    sql.addUserVars(vars);
    sql.setConnection(conn.getConnection());
    sql.execute();
    conn.commit();
  }

  public static void execute(DbPool.DbConnection conn, String file) throws Exception
  {
    execute(conn,file,1);
  }

  public static void useMVCC() throws ServiceException, SQLException {
    //tell HSQLDB to use multiversion so our asserts can read while write is open
    PreparedStatement stmt = null;
    ResultSet rs = null;
    com.zimbra.cs.db.DbPool.DbConnection
      conn = DbPool.getConnection();
    try {
      stmt = conn.prepareStatement("SET DATABASE TRANSACTION CONTROL MVCC");
      stmt.executeUpdate();
    } finally {
      DbPool.closeResults(rs);
      DbPool.quietCloseStatement(stmt);
      DbPool.quietClose(conn);
    }
  }
}