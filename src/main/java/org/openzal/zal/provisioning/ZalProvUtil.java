package org.openzal.zal.provisioning;

import org.openzal.zal.exceptions.ServiceException;
import com.zimbra.cs.account.ProvUtil;
import java.io.IOException;

public class ZalProvUtil {

  public static void main(String[] args) throws IOException, ServiceException {
    try {
      ProvUtil.main(args);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (com.zimbra.common.service.ServiceException e) {
      throw new ServiceException(e);
    }
  }

}
