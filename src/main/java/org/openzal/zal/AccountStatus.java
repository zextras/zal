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

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.cs.account.Provisioning;
import javax.annotation.Nonnull;

public enum AccountStatus
{
  active(ZAttrProvisioning.AccountStatus.active),
  maintenance(ZAttrProvisioning.AccountStatus.maintenance),
  locked(ZAttrProvisioning.AccountStatus.locked),
  closed(ZAttrProvisioning.AccountStatus.closed),
  lockout(ZAttrProvisioning.AccountStatus.lockout),
  pending(ZAttrProvisioning.AccountStatus.pending);

  /**
   * @Deprecated: use the enum value
   */
  @Deprecated
  public static String ACCOUNT_STATUS_MAINTENANCE = Provisioning.ACCOUNT_STATUS_MAINTENANCE;
  /**
   * @Deprecated: use the enum value
   */
  @Deprecated
  public static String ACCOUNT_STATUS_LOCKED      = Provisioning.ACCOUNT_STATUS_LOCKED;
  /**
   * @Deprecated: use the enum value
   */
  @Deprecated
  public static String ACCOUNT_STATUS_LOCKOUT     = Provisioning.ACCOUNT_STATUS_LOCKOUT;
  /**
   * @Deprecated: use the enum value
   */
  @Deprecated
  public static String ACCOUNT_STATUS_ACTIVE      = Provisioning.ACCOUNT_STATUS_ACTIVE;
  /**
   * @Deprecated: use the enum value
   */
  @Deprecated
  public static String ACCOUNT_STATUS_CLOSED      = Provisioning.ACCOUNT_STATUS_CLOSED;

  @Nonnull private final ZAttrProvisioning.AccountStatus mAccountStatus;

  public static AccountStatus of(Object obj) {
    if (obj instanceof ZAttrProvisioning.AccountStatus) {
      return valueOf(obj.toString());
    } else {
      throw new IllegalArgumentException();
    }
  }

  AccountStatus(@Nonnull ZAttrProvisioning.AccountStatus accountStatus)
  {
    mAccountStatus = accountStatus;
  }

  public <T> T toZimbra(@Nonnull Class<T> cls)
  {
    return cls.cast(mAccountStatus);
  }

  @Override
  public String toString()
  {
    return mAccountStatus.toString();
  }
}
