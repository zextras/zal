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

import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class Identity implements Comparable<Identity>
{
  @Nonnull private final com.zimbra.cs.account.Identity mIdentity;

  protected Identity(@Nonnull Object identity)
  {
    if (identity == null)
    {
      throw new NullPointerException();
    }
    mIdentity = (com.zimbra.cs.account.Identity) identity;
  }

  public Map<String, Object> getAttrs(boolean applyDefaults)
  {
    return new HashMap<String, Object>(mIdentity.getAttrs(applyDefaults));
  }

  protected <T> T toZimbra(Class<T> cls)
  {
    return cls.cast(mIdentity);
  }

  public InternetAddress getFriendlyEmailAddress() throws UnsupportedEncodingException
  {
    return mIdentity.getFriendlyEmailAddress();
  }

  @Override
  public int compareTo(Identity o)
  {
    return mIdentity.compareTo(o.toZimbra(com.zimbra.cs.account.Identity.class));
  }
}
