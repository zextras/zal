package org.openzal.zal.db;

import com.zimbra.common.service.ServiceException;

public class DbPool {

  public static void startup() {
    com.zimbra.cs.db.DbPool.global();
  }

  public static com.zimbra.cs.db.DbPool.DbConnection getConnection() throws ServiceException {
    return com.zimbra.cs.db.DbPool.global().getConnectionInstance();
  }

  public static void shutdown() throws Exception {
    com.zimbra.cs.db.DbPool.shutdown();
  }


}
