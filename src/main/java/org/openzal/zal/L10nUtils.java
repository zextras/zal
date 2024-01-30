package org.openzal.zal;

import com.zimbra.common.util.L10nUtil;
import java.util.Locale;

public class L10nUtils {

  public static String getMessage(String key, Locale lc, Object... args) {
    return L10nUtil.getMessage(key, lc, args);
  }

}
