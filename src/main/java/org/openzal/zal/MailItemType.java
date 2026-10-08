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

public class MailItemType
{
  public static MailItemType CONTACT              = new MailItemType(com.zimbra.cs.mailbox.MailItem.Type.CONTACT);
  public static          MailItemType UNKNOWN              = new MailItemType(com.zimbra.cs.mailbox.MailItem.Type.UNKNOWN);
  public static          MailItemType FOLDER               = new MailItemType(com.zimbra.cs.mailbox.MailItem.Type.FOLDER);
    public static          MailItemType TAG                  = new MailItemType(com.zimbra.cs.mailbox.MailItem.Type.TAG);
  public static          MailItemType CONVERSATION         = new MailItemType(com.zimbra.cs.mailbox.MailItem.Type.CONVERSATION);
  public static          MailItemType MESSAGE              = new MailItemType(com.zimbra.cs.mailbox.MailItem.Type.MESSAGE);
    public static          MailItemType APPOINTMENT          = new MailItemType(com.zimbra.cs.mailbox.MailItem.Type.APPOINTMENT);
    public static          MailItemType CHAT                 = new MailItemType(com.zimbra.cs.mailbox.MailItem.Type.CHAT);

  private final com.zimbra.cs.mailbox.MailItem.Type mType;


  public MailItemType(com.zimbra.cs.mailbox.MailItem.Type type)
  {
    mType = type;
  }

  public byte toByte()
  {
    return mType.toByte();
  }

    public static MailItemType of(String itemType)
  {
    return new MailItemType(com.zimbra.cs.mailbox.MailItem.Type.of(itemType));
  }

  public String toString()
  {
    return mType.toString();
  }

  public static String of(byte type)
  {
    return new MailItemType(com.zimbra.cs.mailbox.MailItem.Type.of(type)).toString();
  }
}
