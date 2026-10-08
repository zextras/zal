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

import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.MailServiceException;
import java.util.List;
import javax.annotation.Nonnull;
import org.openzal.zal.exceptions.ExceptionWrapper;
import org.openzal.zal.exceptions.ZimbraException;


public class Acl
{
  public static final byte GRANTEE_USER     = ACL.GRANTEE_USER;
  public static final byte GRANTEE_GROUP    = ACL.GRANTEE_GROUP;
  public static final byte GRANTEE_AUTHUSER = ACL.GRANTEE_AUTHUSER;
  public static final byte GRANTEE_DOMAIN   = ACL.GRANTEE_DOMAIN;
  public static final byte GRANTEE_COS      = ACL.GRANTEE_COS;
  public static final byte GRANTEE_PUBLIC   = ACL.GRANTEE_PUBLIC;
  public static final byte GRANTEE_GUEST    = ACL.GRANTEE_GUEST;
  public static final byte GRANTEE_KEY      = ACL.GRANTEE_KEY;

  public static final short RIGHT_READ     = ACL.RIGHT_READ;
  public static final short RIGHT_WRITE    = ACL.RIGHT_WRITE;
    public static final short RIGHT_ADMIN    = ACL.RIGHT_ADMIN;

    @Nonnull private final ACL mAcl;

  public Acl()
  {
    this(new ACL());
  }

  Acl(@Nonnull Object acl)
  {
    if (acl == null)
    {
      throw new NullPointerException();
    }
    mAcl = (ACL) acl;
  }

  public Short getGrantedRights(Account authuser)
    throws ZimbraException
  {
    try
    {
      return mAcl.getGrantedRights(authuser.toZimbra(com.zimbra.cs.account.Account.class));
    }
    catch (com.zimbra.common.service.ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public boolean isEmpty()
  {
    return getGrants().isEmpty();
  }

  public Grant grantAccess(String zimbraId, byte type, short rights, String secret)
    throws ZimbraException
  {
    ACL.Grant grant;
    try {
      grant = mAcl.grantAccess(zimbraId, type, rights, secret);
    } catch (com.zimbra.common.service.ServiceException e) {
      if (!e.getCode().equals(MailServiceException.GRANT_EXISTS)) {
        throw ExceptionWrapper.wrap(e);
      }

      List<ACL.Grant> grants = mAcl.getGrants();
      for (ACL.Grant g : grants) {
        //getting the grant even in case a GRANT_EXISTS exception has been thrown
        if (g.isGrantee(zimbraId) && g.getGrantedRights() == rights &&
            ((type != GRANTEE_GUEST && type != GRANTEE_KEY) ||
                StringUtil.equal(g.getPassword(), secret))) {
          return new Grant(g);
        }
      }
      throw ExceptionWrapper.wrap(e);
    }
    return new Grant(grant);
  }

    public List<Grant> getGrants()
  {
    return ZimbraListWrapper.wrapGrants(mAcl.getGrants());
  }

  public static String typeToString(byte type)
  {
    return ACL.typeToString(type);
  }

  public static String rightsToString(short rights)
  {
    return ACL.rightsToString(rights);
  }


    public <T> T toZimbra(Class<T> cls)
  {
    return cls.cast(mAcl);
  }
}
