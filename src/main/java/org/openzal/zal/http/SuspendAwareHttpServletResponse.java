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

package org.openzal.zal.http;

import org.openzal.zal.Continuation;
import org.openzal.zal.ContinuationHttpServletRequest;
import org.openzal.zal.ContinuationJetty;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Response handed to {@link HttpHandler}s. While the request is suspended
 * ({@link Continuation#suspend()}) it never commits or completes the underlying response:
 * <code>flushBuffer()</code>, <code>flush()</code> and <code>close()</code> of the output stream and of the
 * writer are ignored.
 *
 * <p>Since {@code Continuation.suspend()} no longer unwinds the caller with an exception, code
 * running after it on the first dispatch (typically an unconditional
 * <code>getOutputStream().flush()</code>) would otherwise commit the response while the request is in
 * async state; a later async timeout/dispatch then fails with <code>IllegalStateException: COMMITTED</code>
 * and leaves the channel stuck. The suspended request is dispatched again (resume/timeout) and the
 * servlet writes the real response on that second pass, when the request is no more suspended.
 */
class SuspendAwareHttpServletResponse extends HttpServletResponseWrapper
{
  private final HttpServletRequest mRequest;

  SuspendAwareHttpServletResponse(HttpServletRequest request, HttpServletResponse response)
  {
    super(response);
    mRequest = request;
  }

  private boolean isSuspended()
  {
    if (mRequest instanceof ContinuationHttpServletRequest)
    {
      Continuation continuation = ((ContinuationHttpServletRequest) mRequest).getContinuation();
      return continuation != null && continuation.isSuspended();
    }
    Object continuation = mRequest.getAttribute(ContinuationJetty.CONTINUATION_ATTR);
    return continuation instanceof Continuation && ((Continuation) continuation).isSuspended();
  }

  @Override
  public void flushBuffer() throws IOException
  {
    if (!isSuspended())
    {
      super.flushBuffer();
    }
  }

  // Servlet spec: getOutputStream() and getWriter() are mutually exclusive. The container is always asked
  // first so it keeps enforcing that (IllegalStateException); the wrappers are cached per delegate instance.
  private SuspendAwareOutputStream mOutputStream;
  private SuspendAwarePrintWriter mWriter;

  @Override
  public synchronized ServletOutputStream getOutputStream() throws IOException
  {
    ServletOutputStream delegate = super.getOutputStream();
    if (mOutputStream == null || mOutputStream.mDelegate != delegate)
    {
      mOutputStream = new SuspendAwareOutputStream(delegate);
    }
    return mOutputStream;
  }

  @Override
  public synchronized PrintWriter getWriter() throws IOException
  {
    PrintWriter delegate = super.getWriter();
    if (mWriter == null || mWriter.mDelegate != delegate)
    {
      mWriter = new SuspendAwarePrintWriter(delegate);
    }
    return mWriter;
  }

  private class SuspendAwarePrintWriter extends PrintWriter
  {
    private final PrintWriter mDelegate;

    SuspendAwarePrintWriter(PrintWriter delegate)
    {
      super(delegate, false);
      mDelegate = delegate;
    }

    @Override
    public void flush()
    {
      if (!isSuspended())
      {
        super.flush();
      }
    }

    @Override
    public void close()
    {
      if (!isSuspended())
      {
        super.close();
      }
    }
  }

  private class SuspendAwareOutputStream extends ServletOutputStream
  {
    private final ServletOutputStream mDelegate;

    SuspendAwareOutputStream(ServletOutputStream delegate)
    {
      mDelegate = delegate;
    }

    @Override
    public boolean isReady()
    {
      return mDelegate.isReady();
    }

    @Override
    public void setWriteListener(WriteListener writeListener)
    {
      mDelegate.setWriteListener(writeListener);
    }

    @Override
    public void write(int b) throws IOException
    {
      mDelegate.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException
    {
      mDelegate.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
      mDelegate.write(b, off, len);
    }

    @Override
    public void flush() throws IOException
    {
      if (!isSuspended())
      {
        mDelegate.flush();
      }
    }

    @Override
    public void close() throws IOException
    {
      if (!isSuspended())
      {
        mDelegate.close();
      }
    }
  }
}
