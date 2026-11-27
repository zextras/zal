package org.openzal.zal.db;

public class DbPool {

  public static void startup() {
    com.zimbra.cs.db.DbPool.global(com.zextras.db.MariaDB.fromLocalConfig());
  }

  public static void shutdown() throws Exception {
    com.zimbra.cs.db.DbPool.shutdown();
  }


}
