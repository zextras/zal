/*
 * ZAL - Zextras Abstraction Layer.
 * Copyright (C) 2026 ZeXtras S.r.l.
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

package org.openzal.zal.redolog.op;

import org.openzal.zal.MailItemType;

import java.lang.reflect.Field;

public class RenameItem {
  protected int id;
  protected MailItemType type;
  protected int folderId;
  protected String name;
  protected Long date;

  public static class RenameItemInitializationException extends RuntimeException{
    public RenameItemInitializationException(Throwable cause) {
      super(cause);
    }
  }

  public RenameItem(RedoableOp op) {
    Field field;
    try {
      com.zimbra.cs.redolog.op.RedoableOp proxiedObject = op.getProxiedObject();
      field = com.zimbra.cs.redolog.op.RenameItem.class.getDeclaredField("mId");
      field.setAccessible(true);
      id = field.getInt(proxiedObject);

      field = com.zimbra.cs.redolog.op.RenameItem.class.getDeclaredField("type");
      field.setAccessible(true);
      com.zimbra.cs.mailbox.MailItem.Type type = (com.zimbra.cs.mailbox.MailItem.Type) field.get(proxiedObject);
      this.type = new MailItemType(type);

      field = com.zimbra.cs.redolog.op.RenameItem.class.getDeclaredField("mFolderId");
      field.setAccessible(true);
      folderId = field.getInt(proxiedObject);

      field = com.zimbra.cs.redolog.op.RenameItem.class.getDeclaredField("mName");
      field.setAccessible(true);
      name = (String) field.get(proxiedObject);

      field = com.zimbra.cs.redolog.op.RenameItem.class.getDeclaredField("mDate");
      field.setAccessible(true);
      date = field.getLong(proxiedObject);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RenameItemInitializationException(e);
    }

  }

  public int getId() {
    return id;
  }

  public MailItemType getType() {
    return type;
  }

  public int getFolderId() {
    return folderId;
  }

  public String getName() {
    return name;
  }

  public Long getDate() {
    return date;
  }
}
