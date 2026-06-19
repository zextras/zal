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
    com.zimbra.cs.redolog.op.ColorItem colorItem = (com.zimbra.cs.redolog.op.ColorItem) op.getProxiedObject();
    this.ids = colorItem.getIds();
    this.type = new MailItemType(colorItem.getType());
    this.color = colorItem.getColor();
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
