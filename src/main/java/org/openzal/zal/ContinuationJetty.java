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

import com.zimbra.common.util.ZimbraLog;

import javax.annotation.Nullable;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link Continuation} implemented on top of the Servlet 3 {@link AsyncContext}.
 *
 * <p>The semantics intentionally mirror the former Jetty 9 {@code Servlet3Continuation}:
 * <ul>
 *   <li>{@code suspend(0)} means <em>no timeout</em> ({@code AsyncContext.setTimeout(0)}): the
 *   caller owns the timeout (the container default of 30 s must not apply);</li>
 *   <li>when the async timeout does fire the request is re-dispatched, so the servlet runs
 *   again with {@link #isExpired()} set and can write the normal response;</li>
 *   <li>{@link #resume()} only dispatches a request that is currently suspended and is a
 *   no-op when it lost the race against timeout/completion.</li>
 * </ul>
 *
 * <p>Unlike Jetty 9, {@code suspend()} does <strong>not</strong> unwind the caller with an
 * exception: on Jetty 12 EE8 any exception escaping the servlet while async is started is
 * routed to {@code HttpChannelState.onError}, which fires {@code AsyncListener.onError} and
 * then sends an HTTP 500 (see commit message). Callers must simply return from the
 * servlet after {@code suspend()}; {@code org.openzal.zal.http.InternalHttpHandler} makes sure
 * the response is not flushed/committed while the request is suspended.
 */
public class ContinuationJetty implements Continuation
{
  /** Request attribute under which the continuation is cached. */
  public static final String CONTINUATION_ATTR = "org.openzal.zal.ContinuationJetty";

  private final HttpServletRequest mReq;
  private final AtomicBoolean mSuspended;
  private final AsyncListener mListener;
  private boolean mListenerRegistered = false; // guarded by this
  private AsyncContext mListenerContext = null; // guarded by this
  private volatile AsyncContext mAsyncContext;
  private volatile boolean mExpired;
  private volatile boolean mIsInitial;

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
    mListener = new ContinuationListener();
  }

  @Override
  public boolean isSuspended()
  {
    return mSuspended.get();
  }

  @Override
  public void resume()
  {
    AsyncContext asyncContext = mAsyncContext;
    // Only the thread that flips suspended -> resumed may dispatch. Not suspended yet,
    // already resumed, expired (timeout owns the dispatch) or completed: nothing to do.
    if (asyncContext == null || !mSuspended.compareAndSet(true, false))
    {
      return;
    }
    try
    {
      asyncContext.dispatch();
    }
    catch (IllegalStateException ex)
    {
      // lost the race against the async timeout / complete(): the container already owns the request
      ZimbraLog.extensions.debug("Continuation resume ignored, request is no more suspended: " + ex.getMessage());
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
      AsyncContext asyncContext = mAsyncContext;
      if (asyncContext == null || !mReq.isAsyncStarted())
      {
        // First suspension, or a new one after the previous async cycle was dispatched.
        asyncContext = mReq.isAsyncStarted() ? mReq.getAsyncContext() : mReq.startAsync();
        mAsyncContext = asyncContext;
      }
      // Container listeners are per async cycle (dropped at startAsync()): make sure ours is registered
      // on the current one, exactly once.
      registerListener(asyncContext);
      // 0 (or negative) means "never expire": the caller owns the timeout.
      // Without this the container default (30 s on Jetty) would apply.
      asyncContext.setTimeout(timeoutMs > 0 ? timeoutMs : 0);
      mExpired = false;
      mIsInitial = false;
      mSuspended.set(true);
    }
    catch (Throwable ex)
    {
      throw new ContinuationThrowable(ex);
    }
  }

  /**
   * Registers the listener on the given async context unless it already is registered for the current
   * async cycle. Jetty reuses the same AsyncContext object across cycles but drops its listeners at every
   * startAsync() (notifying them through onStartAsync, which clears the flag); other containers may hand out
   * a new AsyncContext instead: both cases are covered.
   */
  private synchronized void registerListener(AsyncContext asyncContext)
  {
    if (mListenerRegistered && mListenerContext == asyncContext)
    {
      return;
    }
    asyncContext.addListener(mListener);
    mListenerContext = asyncContext;
    mListenerRegistered = true;
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
    AsyncContext asyncContext = mAsyncContext;
    if (asyncContext != null)
    {
      return asyncContext.toString();
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

  private class ContinuationListener implements AsyncListener
  {
    @Override
    public void onComplete(AsyncEvent event)
    {
      mSuspended.set(false);
    }

    @Override
    public void onTimeout(AsyncEvent event)
    {
      // If resume() already won the race it owns the dispatch and the request is not expired.
      if (mSuspended.compareAndSet(true, false))
      {
        mExpired = true;
      }
      // Re-dispatch (as Servlet3Continuation did) so that the servlet runs again and writes
      // the regular response. Without a dispatch/complete the container would send a 500
      // "AsyncContext timeout" or, if the response is already committed, get stuck.
      try
      {
        event.getAsyncContext().dispatch();
      }
      catch (IllegalStateException ex)
      {
        ZimbraLog.extensions.debug("Continuation timeout dispatch ignored: " + ex.getMessage());
      }
    }

    @Override
    public void onError(AsyncEvent event)
    {
      mSuspended.set(false);
      try
      {
        event.getAsyncContext().complete();
      }
      catch (IllegalStateException ex)
      {
        ZimbraLog.extensions.debug("Continuation error completion ignored: " + ex.getMessage());
      }
    }

    @Override
    public void onStartAsync(AsyncEvent event) throws IOException
    {
      // The container drops all the listeners at every startAsync(): the new cycle starts with none.
      // suspend() registers this listener again, explicitly, on the new cycle.
      synchronized (ContinuationJetty.this)
      {
        mListenerRegistered = false;
      }
    }
  }
}
