package org.openzal.zal.quota;

import com.zextras.mailbox.quota.QuotaCheck;
import com.zextras.mailbox.quota.QuotaCheckSingleton;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.MailServiceException;
import org.openzal.zal.quota.DeleteResult.DeleteFailure;

public class ZalQuotaHook implements QuotaCheck {

	private final QuotaCheckAdapter quotaCheckAdapter;
	private final QuotaCheckAdapter legacyCheck;

	public static synchronized void setInstance(QuotaCheckAdapter zalQuotaAdapter) {
		var hook = new ZalQuotaHook(zalQuotaAdapter);
		QuotaCheckSingleton.setInstance(hook);
	}

	private ZalQuotaHook(QuotaCheckAdapter quotaCheckAdapter) {
		this.quotaCheckAdapter = quotaCheckAdapter;
		this.legacyCheck = new LegacyQuotaCheckAdapter();
	}


	@Override
	public void onSendMessage(Account acct) throws ServiceException {
		final org.openzal.zal.Account zalAccount = new org.openzal.zal.Account(acct);
		enforce(this.getCurrentCheck().onSendMessage(zalAccount));
	}

	@Override
	public void onAddMessage(Account acct, long newTotalMailboxUsage) throws ServiceException {
		final org.openzal.zal.Account zalAccount = new org.openzal.zal.Account(acct);
		enforce(this.getCurrentCheck().onAddMessage(zalAccount, newTotalMailboxUsage));
	}

	@Override
	public void onDeleteMessage(Account acct, long size) throws ServiceException {
		final org.openzal.zal.Account zalAccount = new org.openzal.zal.Account(acct);
		var result = this.getCurrentCheck().onDeleteMessage(zalAccount, size);
		if (result instanceof DeleteFailure) {
			throw ServiceException.FAILURE("Delete failed");
		}
	}

	/**
	 * Maps a {@link QuotaResult} to the outcome expected by the mailbox.
	 * <ul>
	 *   <li>{@link QuotaResult.OverQuota}: {@code mail.QUOTA_EXCEEDED}, rejected by LMTP as over quota
	 *   (552/452 depending on zimbraLmtpPermanentFailureWhenOverQuota)</li>
	 *   <li>{@link QuotaResult.Unavailable}: {@code service.FAILURE} (receiver's fault), so LMTP
	 *   replies with a temporary failure (451) and the message stays queued</li>
	 * </ul>
	 */
	private static void enforce(QuotaResult result) throws ServiceException {
		if (result instanceof QuotaResult.OverQuota) {
			throw MailServiceException.QUOTA_EXCEEDED(0);
		}
		if (result instanceof QuotaResult.Unavailable unavailable) {
			throw ServiceException.FAILURE(
					"quota service unavailable: " + unavailable.reason(), unavailable.cause());
		}
	}

	private QuotaCheckAdapter getCurrentCheck() {
		if (this.quotaCheckAdapter.doLegacyCheck()) {
			return this.legacyCheck;
		}
		return this.quotaCheckAdapter;
	}

}
