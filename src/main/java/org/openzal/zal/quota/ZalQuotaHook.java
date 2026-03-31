package org.openzal.zal.quota;

import com.zextras.mailbox.quota.QuotaCheck;
import com.zextras.mailbox.quota.QuotaCheckSingleton;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.MailServiceException;
import org.openzal.zal.quota.DeleteResult.DeleteFailure;

public class ZalQuotaHook implements QuotaCheck {

	private final QuotaCheckAdapter quotaCheckAdapter;

	public static synchronized void setInstance(QuotaCheckAdapter zalQuotaAdapter) {
		var hook = new ZalQuotaHook(zalQuotaAdapter);
		QuotaCheckSingleton.setInstance(hook);
	}

	private ZalQuotaHook(QuotaCheckAdapter quotaCheckAdapter) {
		this.quotaCheckAdapter = quotaCheckAdapter;
	}


	@Override
	public void onSendMessage(Account acct) throws ServiceException {
		final org.openzal.zal.Account zalAccount = new org.openzal.zal.Account(acct);
		final QuotaResult result = this.quotaCheckAdapter.onSendMessage(zalAccount);
		if (result instanceof QuotaResult.OverQuota) {
			throw MailServiceException.QUOTA_EXCEEDED(0);
		}
	}

	@Override
	public void onAddMessage(Account acct, long newTotalMailboxUsage) throws ServiceException {
		final org.openzal.zal.Account zalAccount = new org.openzal.zal.Account(acct);
		final QuotaResult result = this.quotaCheckAdapter.onAddMessage(zalAccount, newTotalMailboxUsage);
		if (result instanceof QuotaResult.OverQuota) {
			throw MailServiceException.QUOTA_EXCEEDED(0);
		}
	}

	@Override
	public void onDeleteMessage(Account acct, long size) throws ServiceException {
		final org.openzal.zal.Account zalAccount = new org.openzal.zal.Account(acct);
		var result = this.quotaCheckAdapter.onDeleteMessage(zalAccount, size);
		if (result instanceof DeleteFailure) {
			throw ServiceException.FAILURE("Delete failed");
		}
	}

}
