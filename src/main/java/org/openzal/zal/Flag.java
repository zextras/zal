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


public final class Flag extends Item
{
  public static int ID_FROM_ME           = com.zimbra.cs.mailbox.Flag.ID_FROM_ME;
  public static int ID_ATTACHED          = com.zimbra.cs.mailbox.Flag.ID_ATTACHED;
  public static int ID_REPLIED           = com.zimbra.cs.mailbox.Flag.ID_REPLIED;
  public static int ID_FORWARDED         = com.zimbra.cs.mailbox.Flag.ID_FORWARDED;
  public static int ID_COPIED            = com.zimbra.cs.mailbox.Flag.ID_COPIED;
  public static int ID_FLAGGED           = com.zimbra.cs.mailbox.Flag.ID_FLAGGED;
  public static int ID_DRAFT             = com.zimbra.cs.mailbox.Flag.ID_DRAFT;
  public static int ID_DELETED           = com.zimbra.cs.mailbox.Flag.ID_DELETED;
  public static int ID_NOTIFIED          = com.zimbra.cs.mailbox.Flag.ID_NOTIFIED;
  public static int ID_UNREAD            = com.zimbra.cs.mailbox.Flag.ID_UNREAD;
  public static int ID_HIGH_PRIORITY     = com.zimbra.cs.mailbox.Flag.ID_HIGH_PRIORITY;
  public static int ID_LOW_PRIORITY      = com.zimbra.cs.mailbox.Flag.ID_LOW_PRIORITY;
  public static int ID_VERSIONED         = com.zimbra.cs.mailbox.Flag.ID_VERSIONED;
  public static int ID_POPPED            = com.zimbra.cs.mailbox.Flag.ID_POPPED;
  public static int ID_NOTE              = com.zimbra.cs.mailbox.Flag.ID_NOTE;
  public static int ID_PRIORITY          = com.zimbra.cs.mailbox.Flag.ID_PRIORITY;
  public static int ID_POST              = com.zimbra.cs.mailbox.Flag.ID_POST;
  public static int ID_MUTED             = com.zimbra.cs.mailbox.Flag.ID_MUTED;
  public static int ID_INVITE            = com.zimbra.cs.mailbox.Flag.ID_INVITE;

  static com.zimbra.cs.mailbox.Flag.FlagInfo of(int id)
  {
    return com.zimbra.cs.mailbox.Flag.FlagInfo.of(id);
  }

  public static int BITMASK_FROM_ME           = com.zimbra.cs.mailbox.Flag.BITMASK_FROM_ME;
  public static int BITMASK_ATTACHED          = com.zimbra.cs.mailbox.Flag.BITMASK_ATTACHED;
  public static int BITMASK_REPLIED           = com.zimbra.cs.mailbox.Flag.BITMASK_REPLIED;
  public static int BITMASK_FORWARDED         = com.zimbra.cs.mailbox.Flag.BITMASK_FORWARDED;
  public static int BITMASK_FLAGGED           = com.zimbra.cs.mailbox.Flag.BITMASK_FLAGGED;
  public static int BITMASK_DRAFT             = com.zimbra.cs.mailbox.Flag.BITMASK_DRAFT;
  public static int BITMASK_DELETED           = com.zimbra.cs.mailbox.Flag.BITMASK_DELETED;
  public static int BITMASK_HIGH_PRIORITY     = com.zimbra.cs.mailbox.Flag.BITMASK_HIGH_PRIORITY;
  public static int BITMASK_LOW_PRIORITY      = com.zimbra.cs.mailbox.Flag.BITMASK_LOW_PRIORITY;
  public static int BITMASK_VERSIONED         = com.zimbra.cs.mailbox.Flag.BITMASK_VERSIONED;

  public static int BITMASK_NO_INHERIT       = com.zimbra.cs.mailbox.Flag.BITMASK_NO_INHERIT;

  public static int BITMASK_UNCACHED = com.zimbra.cs.mailbox.Flag.BITMASK_UNCACHED;

  private final com.zimbra.cs.mailbox.Flag mFlag;

  public Flag(@Nonnull Object item)
  {
    super(item);
    mFlag = (com.zimbra.cs.mailbox.Flag) item;
  }

  public boolean isSystemFlag()
  {
    return mFlag.isSystemFlag();
  }

  public String getName()
  {
    return mFlag.getName();
  }

  public byte getIndex()
  {
    return mFlag.getIndex();
  }

}
