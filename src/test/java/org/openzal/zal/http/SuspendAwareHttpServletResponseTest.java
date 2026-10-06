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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openzal.zal.Continuation;
import org.openzal.zal.ContinuationHttpServletRequest;
import org.openzal.zal.ContinuationJetty;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SuspendAwareHttpServletResponseTest
{
  private HttpServletRequest mRequest;
  private HttpServletResponse mResponse;
  private ServletOutputStream mOut;
  private Continuation mContinuation;

  @BeforeEach
  public void setUp() throws IOException
  {
    mRequest = Mockito.mock(HttpServletRequest.class);
    mResponse = Mockito.mock(HttpServletResponse.class);
    mOut = Mockito.mock(ServletOutputStream.class);
    mContinuation = Mockito.mock(Continuation.class);
    Mockito.when(mResponse.getOutputStream()).thenReturn(mOut);
    Mockito.when(mRequest.getAttribute(ContinuationJetty.CONTINUATION_ATTR)).thenReturn(mContinuation);
  }

  @Test
  public void flush_is_ignored_while_suspended() throws IOException
  {
    Mockito.when(mContinuation.isSuspended()).thenReturn(true);
    HttpServletResponse response = new SuspendAwareHttpServletResponse(mRequest, mResponse);

    response.getOutputStream().flush();
    response.getOutputStream().close();
    response.flushBuffer();

    Mockito.verify(mOut, Mockito.never()).flush();
    Mockito.verify(mOut, Mockito.never()).close();
    Mockito.verify(mResponse, Mockito.never()).flushBuffer();
  }

  @Test
  public void flush_is_delegated_when_not_suspended() throws IOException
  {
    Mockito.when(mContinuation.isSuspended()).thenReturn(false);
    HttpServletResponse response = new SuspendAwareHttpServletResponse(mRequest, mResponse);

    response.getOutputStream().flush();
    response.flushBuffer();

    Mockito.verify(mOut).flush();
    Mockito.verify(mResponse).flushBuffer();
  }

  @Test
  public void flush_is_delegated_when_there_is_no_continuation() throws IOException
  {
    Mockito.when(mRequest.getAttribute(ContinuationJetty.CONTINUATION_ATTR)).thenReturn(null);
    HttpServletResponse response = new SuspendAwareHttpServletResponse(mRequest, mResponse);

    response.getOutputStream().flush();

    Mockito.verify(mOut).flush();
  }

  @Test
  public void writes_are_always_delegated() throws IOException
  {
    Mockito.when(mContinuation.isSuspended()).thenReturn(true);
    HttpServletResponse response = new SuspendAwareHttpServletResponse(mRequest, mResponse);
    byte[] data = new byte[] {1, 2, 3};

    response.getOutputStream().write(data, 0, 3);

    Mockito.verify(mOut).write(data, 0, 3);
  }

  @Test
  public void uses_the_continuation_of_a_continuation_request() throws IOException
  {
    ContinuationHttpServletRequest request = Mockito.mock(ContinuationHttpServletRequest.class);
    Mockito.when(request.getContinuation()).thenReturn(mContinuation);
    Mockito.when(mContinuation.isSuspended()).thenReturn(true);
    HttpServletResponse response = new SuspendAwareHttpServletResponse(request, mResponse);

    response.getOutputStream().flush();

    Mockito.verify(mOut, Mockito.never()).flush();
  }
}
