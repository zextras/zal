package org.openzal.zal.db;

/**
 * Zimbra Collaboration Suite Server
 */

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.db.DbMailbox;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.HSQLDB;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import org.hsqldb.cmdline.SqlFile;

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
      execute(conn, basePath + "/db.sql");
      executeForAllGroups(conn, basePath + "/create_database.sql");
    } finally {
      DbPool.closeResults(rs);
      DbPool.quietCloseStatement(stmt);
      DbPool.quietClose(conn);
    }
  }

  public static void createDatabase() throws Exception {
    createDatabase("it/data/carbonio/sql/");
  }

  //
  // Deletes all records from all tables.
  // @param zimbraServerDir the directory that contains the ZimbraServer project
  // @throws Exception
  //
  public static void clearDatabase() throws Exception
  {
    clearDatabase("it/data/carbonio/sql/clear.sql");
  }

  /**
   * Executes a clear script for all mailbox groups
   * @param clearSqlScript sql script containing clear instructions
   * @throws Exception
   */
  public static void clearDatabase(String clearSqlScript) throws Exception
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

  private static void executeForAllGroups(com.zimbra.cs.db.DbPool.DbConnection conn, String file) throws Exception
  {
    for( int i=1; i <= 100; ++i ) execute(conn, file, i);
  }

  public static void execute(DbPool.DbConnection conn, String file, int mboxId) throws Exception
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

  public boolean databaseExists(DbPool.DbConnection connection, String dbname)
  {
    return true;
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