package org.openzal.zal;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.DateUtil;
import com.zimbra.cs.ldap.LdapDateUtil;
import java.util.Date;

public class DateUtils {

  public static String toGeneralizedTimeWithMs(Date date) {
    return LdapDateUtil.toGeneralizedTimeWithMs(date);
  }

  public static long getTimeInterval(String value, long defaultValue) {
    return DateUtil.getTimeInterval(value, defaultValue);
  }

  public static long getTimeInterval(String value) {
    try {
      return DateUtil.getTimeInterval(value);
    } catch (ServiceException e) {
      throw new RuntimeException(e);
    }
  }

  public static Date parseGeneralizedTime(String time) {
    return LdapDateUtil.parseGeneralizedTime(time);
  }
}
