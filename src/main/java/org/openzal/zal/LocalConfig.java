/*
 * ZAL - Zextras Abstraction Layer.
 * Copyright (C) 2023 ZeXtras S.r.l.
 *
 * This file is part of ZAL.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZAL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.openzal.zal;

import com.zimbra.common.localconfig.ConfigException;
import com.zimbra.common.localconfig.LC;
import javax.annotation.Nonnull;

import java.util.Arrays;
import java.util.List;
import org.dom4j.DocumentException;

public class LocalConfig
{
  public static final LocalConfigKey av_notify_user = new LocalConfigKey(LC.av_notify_user);
  public static final LocalConfigKey mailboxd_keystore = new LocalConfigKey(LC.mailboxd_keystore);
  public static final LocalConfigKey mailboxd_keystore_password = new LocalConfigKey(LC.mailboxd_keystore_password);
  public static final LocalConfigKey zimbra_activesync_contact_image_size = new LocalConfigKey(LC.zimbra_activesync_contact_image_size);
  public static final LocalConfigKey zimbra_home = new LocalConfigKey(LC.zimbra_home);
  public static final LocalConfigKey zimbra_http_originating_ip_header = new LocalConfigKey(LC.zimbra_http_originating_ip_header);
  public static final LocalConfigKey zimbra_ldap_password = new LocalConfigKey(LC.zimbra_ldap_password);
  public static final LocalConfigKey zimbra_ldap_user = new LocalConfigKey(LC.zimbra_ldap_user);
  public static final LocalConfigKey zimbra_ldap_userdn = new LocalConfigKey(LC.zimbra_ldap_userdn);
  public static final LocalConfigKey zimbra_mailbox_groups = new LocalConfigKey(LC.zimbra_mailbox_groups);
  public static final LocalConfigKey zimbra_server_hostname = new LocalConfigKey(LC.zimbra_server_hostname);

  public static void reload() {
    try {
      LC.reload();
    } catch (DocumentException e) {
      throw new RuntimeException(e);
    } catch (ConfigException e) {
      throw new RuntimeException(e);
    }
  }
  @Nonnull
  public static List<String> getAllKeys()
  {
    return Arrays.asList(LC.getAllKeys());
  }

  public static String get(LocalConfigKey key)
  {
    return key.value();
  }

  public static int getInt(LocalConfigKey key)
  {
    return key.intValue();
  }

  @Deprecated
  public static String get(String key)
  {
    return LC.get(key);
  }

  @Deprecated
  public static int getInt(String key)
  {
    return Integer.parseInt(LC.get(key));
  }

  public static void setDefaultTimeZonesFile(String path)
  {
    LC.timezone_file.setDefault(path);
  }

  public static void setLdapPort(long port) {
    LC.ldap_port.setDefault(port);
  }
}
