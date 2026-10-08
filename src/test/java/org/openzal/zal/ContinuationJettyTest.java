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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContinuationJettyTest
{
  /** Minimal AsyncContext honouring the Jetty state machine rules that matter here. */
  static class FakeAsyncContext implements AsyncContext
  {
    enum State { ASYNC, EXPIRING, DISPATCHED, COMPLETE }

    State state = State.ASYNC;
    long timeout = 30000; // container default
    int dispatchCount;
    int completeCount;
    final List<AsyncListener> listeners = new ArrayList<>();

    @Override public ServletRequest getRequest() { return null; }
    @Override public ServletResponse getResponse() { return null; }
    @Override public boolean hasOriginalRequestAndResponse() { return true; }

    @Override
    public void dispatch()
    {
      if (state != State.ASYNC && state != State.EXPIRING)
      {
        throw new IllegalStateException("dispatch in " + state);
      }
      state = State.DISPATCHED;
      dispatchCount++;
    }

    @Override public void dispatch(String path) { dispatch(); }
    @Override public void dispatch(ServletContext context, String path) { dispatch(); }

    @Override
    public void complete()
    {
      if (state == State.DISPATCHED)
      {
        throw new IllegalStateException("complete in " + state);
      }
      state = State.COMPLETE;
      completeCount++;
    }

    @Override public void start(Runnable run) { run.run(); }
    @Override public void addListener(AsyncListener listener) { listeners.add(listener); }
    @Override public void addListener(AsyncListener l, ServletRequest rq, ServletResponse rs) { listeners.add(l); }
    @Override public <T extends AsyncListener> T createListener(Class<T> clazz) { return null; }
    @Override public void setTimeout(long timeoutMs) { timeout = timeoutMs; }
    @Override public long getTimeout() { return timeout; }

    /** What Jetty does when the async timer expires. */
    void expire() throws IOException
    {
      if (state != State.ASYNC)
      {
        return;
      }
      state = State.EXPIRING;
      AsyncEvent event = new AsyncEvent(this);
      for (AsyncListener listener : new ArrayList<>(listeners))
      {
        listener.onTimeout(event);
      }
    }

    void fail() throws IOException
    {
      AsyncEvent event = new AsyncEvent(this, new RuntimeException("boom"));
      for (AsyncListener listener : new ArrayList<>(listeners))
      {
        listener.onError(event);
      }
    }

    /** What Jetty does on startAsync(): the listeners of the previous cycle are notified and dropped. */
    void newCycle() throws IOException
    {
      List<AsyncListener> previous = new ArrayList<>(listeners);
      listeners.clear();
      state = State.ASYNC;
      AsyncEvent event = new AsyncEvent(this);
      for (AsyncListener listener : previous)
      {
        listener.onStartAsync(event);
      }
    }

    void finish() throws IOException
    {
      state = State.COMPLETE;
      AsyncEvent event = new AsyncEvent(this);
      for (AsyncListener listener : new ArrayList<>(listeners))
      {
        listener.onComplete(event);
      }
    }
  }

  private FakeAsyncContext mAsyncContext;
  private HttpServletRequest mRequest;
  private ContinuationJetty mContinuation;
  private boolean mAsyncStarted;

  @BeforeEach
  public void setUp() throws Exception
  {
    mAsyncContext = new FakeAsyncContext();
    mAsyncStarted = false;
    Map<String, Object> attributes = new HashMap<>();
    mRequest = Mockito.mock(HttpServletRequest.class);
    Mockito.when(mRequest.isAsyncStarted()).thenAnswer(i -> mAsyncStarted);
    Mockito.when(mRequest.startAsync()).thenAnswer(i -> {
      mAsyncStarted = true;
      mAsyncContext.newCycle();
      return mAsyncContext;
    });
    Mockito.when(mRequest.getAsyncContext()).thenReturn(mAsyncContext);
    Mockito.when(mRequest.getAttribute(Mockito.anyString())).thenAnswer(i -> attributes.get(i.<String>getArgument(0)));
    Mockito.doAnswer(i -> attributes.put(i.<String>getArgument(0), i.getArgument(1)))
      .when(mRequest).setAttribute(Mockito.anyString(), Mockito.any());
    mContinuation = ContinuationJetty.getOrCreateContinuation(mRequest);
  }

  @Test
  public void continuation_is_cached_on_the_request()
  {
    assertSame(mContinuation, ContinuationJetty.getOrCreateContinuation(mRequest));
    assertTrue(mContinuation.isInitial());
    assertFalse(mContinuation.isSuspended());
  }

  @Test
  public void suspend_without_timeout_disables_the_container_default_timeout()
  {
    mContinuation.suspend();

    assertEquals(0L, mAsyncContext.timeout);
    assertTrue(mContinuation.isSuspended());
    assertFalse(mContinuation.isInitial());
    assertEquals(0, mAsyncContext.dispatchCount);
  }

  @Test
  public void suspend_zero_sets_timeout_zero()
  {
    mContinuation.suspend(0);

    assertEquals(0L, mAsyncContext.timeout);
  }

  @Test
  public void suspend_with_timeout_sets_that_timeout()
  {
    mContinuation.suspend(1234);

    assertEquals(1234L, mAsyncContext.timeout);
    assertTrue(mContinuation.isSuspended());
  }

  @Test
  public void suspend_does_not_throw_to_unwind_the_caller()
  {
    assertDoesNotThrow(() -> mContinuation.suspend(0));
  }

  @Test
  public void suspend_failure_is_reported_as_continuation_throwable() throws Exception
  {
    Mockito.doThrow(new IllegalStateException("async not supported")).when(mRequest).startAsync();

    ContinuationThrowable ex = assertThrows(ContinuationThrowable.class, () -> mContinuation.suspend());
    assertThrows(IllegalStateException.class, ex::throwJettyException);
    assertFalse(mContinuation.isSuspended());
  }

  @Test
  public void timeout_dispatches_the_request_and_marks_it_expired() throws Exception
  {
    mContinuation.suspend(0);

    mAsyncContext.expire();

    assertEquals(1, mAsyncContext.dispatchCount);
    assertEquals(0, mAsyncContext.completeCount);
    assertTrue(mContinuation.isExpired());
    assertFalse(mContinuation.isSuspended());
  }

  @Test
  public void resume_after_timeout_does_not_dispatch_nor_throw() throws Exception
  {
    mContinuation.suspend(0);
    mAsyncContext.expire();

    assertDoesNotThrow(() -> mContinuation.resume());

    assertEquals(1, mAsyncContext.dispatchCount);
    assertTrue(mContinuation.isExpired());
  }

  @Test
  public void resume_dispatches_once_when_suspended()
  {
    mContinuation.suspend(0);

    mContinuation.resume();

    assertEquals(1, mAsyncContext.dispatchCount);
    assertFalse(mContinuation.isSuspended());
    assertFalse(mContinuation.isExpired());
  }

  @Test
  public void double_resume_dispatches_only_once()
  {
    mContinuation.suspend(0);

    mContinuation.resume();
    assertDoesNotThrow(() -> mContinuation.resume());

    assertEquals(1, mAsyncContext.dispatchCount);
  }

  @Test
  public void resume_before_suspend_is_a_noop()
  {
    assertDoesNotThrow(() -> mContinuation.resume());

    assertEquals(0, mAsyncContext.dispatchCount);
    assertFalse(mContinuation.isSuspended());
    Mockito.verify(mRequest, Mockito.never()).startAsync();
  }

  @Test
  public void resume_that_loses_the_race_against_the_container_is_swallowed()
  {
    mContinuation.suspend(0);
    // the container expired the request but the timeout listener did not run yet
    mAsyncContext.state = FakeAsyncContext.State.DISPATCHED;

    assertDoesNotThrow(() -> mContinuation.resume());

    assertEquals(0, mAsyncContext.dispatchCount);
    assertFalse(mContinuation.isSuspended());
  }

  @Test
  public void resume_after_complete_does_not_dispatch() throws Exception
  {
    mContinuation.suspend(0);
    mAsyncContext.finish();

    assertDoesNotThrow(() -> mContinuation.resume());

    assertEquals(0, mAsyncContext.dispatchCount);
    assertFalse(mContinuation.isSuspended());
  }

  @Test
  public void error_completes_the_request() throws Exception
  {
    mContinuation.suspend(0);

    mAsyncContext.fail();

    assertEquals(1, mAsyncContext.completeCount);
    assertFalse(mContinuation.isSuspended());
    assertDoesNotThrow(() -> mContinuation.resume());
    assertEquals(0, mAsyncContext.dispatchCount);
  }

  @Test
  public void error_after_dispatch_does_not_throw() throws Exception
  {
    mContinuation.suspend(0);
    mContinuation.resume();

    assertDoesNotThrow(() -> mAsyncContext.fail());
  }

  @Test
  public void can_suspend_again_after_the_request_was_redispatched() throws Exception
  {
    mContinuation.suspend(0);
    mContinuation.resume();
    // ASYNC dispatch: the request is not async any more
    mAsyncStarted = false;

    mContinuation.suspend(500);

    assertTrue(mContinuation.isSuspended());
    assertEquals(500L, mAsyncContext.timeout);
    Mockito.verify(mRequest, Mockito.times(2)).startAsync();
    mContinuation.resume();
    assertEquals(2, mAsyncContext.dispatchCount);
  }

  @Test
  public void listener_is_registered_exactly_once_on_every_async_cycle() throws Exception
  {
    mContinuation.suspend(0);
    assertEquals(1, mAsyncContext.listeners.size());
    // suspending twice in the same cycle must not register twice
    mContinuation.suspend(0);
    assertEquals(1, mAsyncContext.listeners.size());

    mContinuation.resume();
    mAsyncStarted = false;
    mContinuation.suspend(500);

    assertEquals(1, mAsyncContext.listeners.size());
  }

  @Test
  public void timeout_on_the_second_cycle_dispatches_and_marks_expired() throws Exception
  {
    mContinuation.suspend(0);
    mContinuation.resume();
    mAsyncStarted = false;
    assertEquals(1, mAsyncContext.dispatchCount);

    mContinuation.suspend(500);
    mAsyncContext.expire();

    assertEquals(2, mAsyncContext.dispatchCount);
    assertTrue(mContinuation.isExpired());
    assertFalse(mContinuation.isSuspended());
    assertDoesNotThrow(() -> mContinuation.resume());
    assertEquals(2, mAsyncContext.dispatchCount);
  }

  @Test
  public void listener_is_registered_on_a_brand_new_async_context_without_on_start_async() throws Exception
  {
    mContinuation.suspend(0);
    mContinuation.resume();
    mAsyncStarted = false;
    FakeAsyncContext second = new FakeAsyncContext();
    Mockito.doAnswer(i -> {
      mAsyncStarted = true;
      return second;
    }).when(mRequest).startAsync();

    mContinuation.suspend(500);
    second.expire();

    assertEquals(1, second.listeners.size());
    assertEquals(1, second.dispatchCount);
    assertTrue(mContinuation.isExpired());
    Mockito.verify(mRequest, Mockito.never()).getAsyncContext();
  }

  @Test
  public void object_is_stored_on_the_request()
  {
    Object obj = new Object();
    mContinuation.setObject(obj);

    assertSame(obj, mContinuation.getObject());
  }
}
