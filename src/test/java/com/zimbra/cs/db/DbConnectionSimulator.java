package com.zimbra.cs.db;

import com.zextras.db.MariaDB;
import java.sql.Connection;

public class DbConnectionSimulator extends DbPool.DbConnection
{
  public DbConnectionSimulator(Connection conn)
  {
    super(conn, MariaDB.fromLocalConfig());
  }
}
