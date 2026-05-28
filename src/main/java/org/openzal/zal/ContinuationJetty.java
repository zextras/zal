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

import javax.annotation.Nullable;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;

import java.util.concurrent.atomic.AtomicBoolean;

public class ContinuationJetty implements Continuation
{
  private static final String CONTINUATION_ATTR = "org.openzal.zal.ContinuationJetty";

  private final HttpServletRequest mReq;
  private AsyncContext mAsyncContext;
  private final AtomicBoolean mSuspended;
  private volatile boolean mExpired;
  private boolean mIsInitial;

  public static Continuation getOrCreateContinuation(HttpServletRequest req)
  {
    ContinuationJetty cont = (ContinuationJetty) req.getAttribute(CONTINUATION_ATTR);
    if (cont == null)
    {
      cont = new ContinuationJetty(req);
      req.setAttribute(CONTINUATION_ATTR, cont);
    }
    return cont;
  }

  private ContinuationJetty(HttpServletRequest req)
  {
    mReq = req;
    mSuspended = new AtomicBoolean(false);
    mExpired = false;
    mAsyncContext = null;
    mIsInitial = !req.isAsyncStarted();
  }

  @Override
  public boolean isSuspended()
  {
    return mSuspended.get();
  }

  @Override
  public void resume()
  {
    if (mAsyncContext != null)
    {
      mSuspended.set(false);
      mAsyncContext.dispatch();
    }
  }

  @Override
  public boolean isInitial()
  {
    return mIsInitial;
  }

  @Override
  public void suspend()
  {
    suspend(0);
  }

  private static final String sAttributeKey = "ZAL";

  @Override
  public void suspend(long timeoutMs) throws Error
  {
    try
    {
      if (mAsyncContext == null)
      {
        mAsyncContext = mReq.startAsync();
        mAsyncContext.addListener(new AsyncListener()
        {
          @Override
          public void onComplete(AsyncEvent event)
          {
            mSuspended.set(false);
          }

          @Override
          public void onTimeout(AsyncEvent event)
          {
            mSuspended.set(false);
            mExpired = true;
          }

          @Override
          public void onError(AsyncEvent event)
          {
            mSuspended.set(false);
          }

          @Override
          public void onStartAsync(AsyncEvent event)
          {
          }
        });
      }
      mExpired = false;
      mIsInitial = false;
      mSuspended.set(true);
      if (timeoutMs > 0)
      {
        mAsyncContext.setTimeout(timeoutMs);
      }
    }
    catch (Throwable ex)
    {
      throw new ContinuationThrowable(ex);
    }
  }

  @Override
  public boolean isExpired()
  {
    return mExpired;
  }

  @Override
  public void setObject(Object obj)
  {
    mReq.setAttribute(sAttributeKey, obj);
  }

  @Override
  public Object getObject()
  {
    return mReq.getAttribute(sAttributeKey);
  }

  @Override
  public String toString()
  {
    if (mAsyncContext != null)
    {
      return mAsyncContext.toString();
    }
    return super.toString();
  }

  @Override
  public boolean equals(@Nullable Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }

    ContinuationJetty that = (ContinuationJetty) o;

    if (mReq != that.mReq)
    {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    return mReq.hashCode();
  }
}
