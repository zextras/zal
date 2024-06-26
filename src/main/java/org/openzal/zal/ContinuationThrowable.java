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

public class ContinuationThrowable extends RuntimeException
{
  private final Throwable mJettyException;

  public ContinuationThrowable(Throwable ex)
  {
    if( !(ex instanceof Error) && !(ex instanceof RuntimeException) )
    {
      throw new RuntimeException("Invalid continuation: "+ex.getClass().getName());
    }
    mJettyException = ex;
  }

  public void throwJettyException()
  {
    if( mJettyException instanceof Error) {
      throw (Error)mJettyException;
    }

    if( mJettyException instanceof RuntimeException) {
      throw (RuntimeException)mJettyException;
    }
  }
}
