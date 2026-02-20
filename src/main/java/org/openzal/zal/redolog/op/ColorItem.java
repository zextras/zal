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

public class ColorItem {
  private final int[] ids;
  private final MailItemType type;
  private final long color;

  public static class ColorItemInitializationException extends RuntimeException{

    public ColorItemInitializationException(Throwable cause) {
      super(cause);
    }

  }

  public ColorItem(RedoableOp op) {
    Field field;
    try {
      field = com.zimbra.cs.redolog.op.ColorItem.class.getDeclaredField("mIds");
      field.setAccessible(true);
      com.zimbra.cs.redolog.op.RedoableOp proxiedObject = op.getProxiedObject();
      ids = (int[]) field.get(proxiedObject);

      field = com.zimbra.cs.redolog.op.ColorItem.class.getDeclaredField("type");
      field.setAccessible(true);
      com.zimbra.cs.mailbox.MailItem.Type type = (com.zimbra.cs.mailbox.MailItem.Type) field.get(proxiedObject);
      this.type = new MailItemType(type);
      field = com.zimbra.cs.redolog.op.ColorItem.class.getDeclaredField("mColor");
      field.setAccessible(true);
      color = field.getLong(proxiedObject);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new ColorItemInitializationException(e);
    }

  }

  public int[] getIds() {
    return ids;
  }

  public MailItemType getType() {
    return type;
  }

  public long getColor() {
    return color;
  }
}
