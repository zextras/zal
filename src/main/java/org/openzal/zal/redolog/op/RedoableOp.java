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

package org.openzal.zal.redolog.op;

import org.openzal.zal.redolog.DataExtractor;
import org.openzal.zal.redolog.RedoLogOutput;

import javax.annotation.Nonnull;


public class RedoableOp
{

    private final com.zimbra.cs.redolog.op.RedoableOp mRedoableOp;

  public RedoableOp(@Nonnull Object redoableOp)
  {
    mRedoableOp = (com.zimbra.cs.redolog.op.RedoableOp) redoableOp;
  }


    public String toString()
  {
    return mRedoableOp.toString();
  }

  public int getMailboxId()
  {
    return mRedoableOp.getMailboxId();
  }

  com.zimbra.cs.redolog.op.RedoableOp getProxiedObject()
  {
    return mRedoableOp;
  }

    public int getOpCode()
  {
    return mRedoableOp.getOperation().getCode();
  }

    public void extractData(RedoLogOutput redoLogOutput) throws Exception {
    DataExtractor.extract(mRedoableOp, redoLogOutput);
  }

  public org.openzal.zal.OperationContext getOperationContext() {
    return org.openzal.zal.OperationContext.buildFromZimbra(mRedoableOp.getOperationContext());
  }
}
