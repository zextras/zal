package com.zimbra.cs.db;

import java.sql.Connection;

public class DbConnectionSimulator extends DbPool.DbConnection
{
  public DbConnectionSimulator(Connection conn)
  {
    super(conn, Db.getInstance());
  }
}
