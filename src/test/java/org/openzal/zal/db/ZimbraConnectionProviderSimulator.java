package org.openzal.zal.db;

import com.zimbra.cs.db.DbConnectionSimulator;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.DbPool.DbConnection;
import javax.annotation.Nonnull;
import org.openzal.zal.exceptions.ExceptionWrapper;
import org.openzal.zal.exceptions.UnableToObtainDBConnectionException;
import org.openzal.zal.lib.ZimbraConnectionWrapper;
import org.openzal.zal.lib.ZimbraDatabase;

public class ZimbraConnectionProviderSimulator implements ZimbraDatabase.ConnectionProvider
{
  @Nonnull
  @Override
  public org.openzal.zal.Connection getConnection() throws UnableToObtainDBConnectionException
  {
    try {
      DbConnection connection = DbPool.getConnection();
      DbConnectionSimulator connectionSimulator = new DbConnectionSimulator(connection.getConnection());
      return new ZimbraConnectionWrapper(connectionSimulator);
    }
    catch (com.zimbra.common.service.ServiceException e)
    {
      throw ExceptionWrapper.createUnableToObtainDBConnection(e);
    }
  }
}
