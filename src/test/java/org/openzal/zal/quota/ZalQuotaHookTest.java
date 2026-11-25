package org.openzal.zal.quota;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zextras.mailbox.quota.QuotaCheck;
import com.zextras.mailbox.quota.QuotaCheckSingleton;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.MailServiceException;
import java.net.ConnectException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ZalQuotaHookTest {

	private QuotaCheck previousInstance;
	private QuotaCheckAdapter adapter;
	private Account account;

	@BeforeEach
	void setUp() {
		previousInstance = QuotaCheckSingleton.getInstance();
		adapter = mock(QuotaCheckAdapter.class);
		when(adapter.doLegacyCheck()).thenReturn(false);
		account = mock(Account.class);
		ZalQuotaHook.setInstance(adapter);
	}

	@AfterEach
	void tearDown() {
		QuotaCheckSingleton.setInstance(previousInstance);
	}

	private static QuotaCheck hook() {
		return QuotaCheckSingleton.getInstance();
	}

	@Test
	void onAddMessageUnderQuotaDoesNotThrow() {
		when(adapter.onAddMessage(any(), anyLong())).thenReturn(new QuotaResult.UnderQuota());

		assertDoesNotThrow(() -> hook().onAddMessage(account, 10L));
	}

	@Test
	void onAddMessageOverQuotaThrowsQuotaExceeded() {
		when(adapter.onAddMessage(any(), anyLong())).thenReturn(new QuotaResult.OverQuota());

		final ServiceException e =
				assertThrows(ServiceException.class, () -> hook().onAddMessage(account, 10L));

		assertEquals(MailServiceException.QUOTA_EXCEEDED, e.getCode());
		assertFalse(e.isReceiversFault());
	}

	@Test
	void onAddMessageUnavailableThrowsReceiversFaultFailureWithCause() {
		final Throwable cause = new ConnectException("connection refused");
		when(adapter.onAddMessage(any(), anyLong()))
				.thenReturn(new QuotaResult.Unavailable("storages not reachable", cause));

		final ServiceException e =
				assertThrows(ServiceException.class, () -> hook().onAddMessage(account, 10L));

		assertEquals(ServiceException.FAILURE, e.getCode());
		assertNotEquals(MailServiceException.QUOTA_EXCEEDED, e.getCode());
		assertTrue(e.isReceiversFault());
		assertSame(cause, e.getCause());
		assertTrue(e.getMessage().contains("storages not reachable"));
	}

	@Test
	void onAddMessageUnavailableWithoutCauseThrowsReceiversFaultFailure() {
		when(adapter.onAddMessage(any(), anyLong()))
				.thenReturn(new QuotaResult.Unavailable("unknown", null));

		final ServiceException e =
				assertThrows(ServiceException.class, () -> hook().onAddMessage(account, 10L));

		assertEquals(ServiceException.FAILURE, e.getCode());
		assertTrue(e.isReceiversFault());
	}

	@Test
	void onSendMessageUnderQuotaDoesNotThrow() {
		when(adapter.onSendMessage(any())).thenReturn(new QuotaResult.UnderQuota());

		assertDoesNotThrow(() -> hook().onSendMessage(account));
	}

	@Test
	void onSendMessageOverQuotaThrowsQuotaExceeded() {
		when(adapter.onSendMessage(any())).thenReturn(new QuotaResult.OverQuota());

		final ServiceException e =
				assertThrows(ServiceException.class, () -> hook().onSendMessage(account));

		assertEquals(MailServiceException.QUOTA_EXCEEDED, e.getCode());
	}

	@Test
	void onSendMessageUnavailableThrowsReceiversFaultFailureWithCause() {
		final Throwable cause = new ConnectException("connection refused");
		when(adapter.onSendMessage(any()))
				.thenReturn(new QuotaResult.Unavailable("storages not reachable", cause));

		final ServiceException e =
				assertThrows(ServiceException.class, () -> hook().onSendMessage(account));

		assertEquals(ServiceException.FAILURE, e.getCode());
		assertTrue(e.isReceiversFault());
		assertSame(cause, e.getCause());
	}
}
