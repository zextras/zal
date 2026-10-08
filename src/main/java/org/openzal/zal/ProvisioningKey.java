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

import javax.annotation.Nonnull;
import org.openzal.zal.exceptions.ExceptionWrapper;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.account.Key.CacheEntryBy;
import com.zimbra.common.account.Key.DistributionListBy;

public class ProvisioningKey
{
  public static class ByAccount
  {
    private final AccountBy mAccountBy;

    @Nonnull public static ByAccount adminName        = new ByAccount(AccountBy.adminName);
    @Nonnull public static ByAccount name             = new ByAccount(AccountBy.name);

    ByAccount(AccountBy accountBy)
    {
      mAccountBy = accountBy;
    }

    @Nonnull
    public static ByAccount fromString(String s)
      throws ServiceException
    {
      try
      {
        return new ByAccount(AccountBy.valueOf(s));
      }
      catch (IllegalArgumentException e)
      {
        throw ExceptionWrapper.wrap(ServiceException.INVALID_REQUEST("unknown key: " + s, e));
      }
    }

    AccountBy toZimbra()
    {
      return mAccountBy;
    }
  }

  public static class ByCacheEntry
  {
    private final CacheEntryBy mCacheEntryBy;

    @Nonnull public static ByCacheEntry id   = new ByCacheEntry(CacheEntryBy.id);

    ByCacheEntry(CacheEntryBy identityBy)
    {
      mCacheEntryBy = identityBy;
    }

    CacheEntryBy toZimbra()
    {
      return mCacheEntryBy;
    }
  }

  public static class ByDistributionList
  {
    private final DistributionListBy mDistributionListBy;

    @Nonnull public static ByDistributionList id   = new ByDistributionList(DistributionListBy.id);
    @Nonnull public static ByDistributionList name = new ByDistributionList(DistributionListBy.name);

    ByDistributionList(DistributionListBy identityBy)
    {
      mDistributionListBy = identityBy;
    }

    DistributionListBy toZimbra()
    {
      return mDistributionListBy;
    }

  }

}
