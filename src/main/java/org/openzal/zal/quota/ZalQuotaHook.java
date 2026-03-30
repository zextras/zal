package org.openzal.zal.quota;

import com.zextras.mailbox.quota.QuotaHook;
import com.zextras.mailbox.quota.QuotaHookSingleton;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.MailServiceException;

public class ZalQuotaHook implements QuotaHook {

	private final ZalQuotaAdapter zalQuotaAdapter;

	public static synchronized void setInstance(ZalQuotaAdapter zalQuotaAdapter) {
		var hook = new ZalQuotaHook(zalQuotaAdapter);
		QuotaHookSingleton.setInstance(hook);
	}

	private ZalQuotaHook(ZalQuotaAdapter zalQuotaAdapter) {
		this.zalQuotaAdapter = zalQuotaAdapter;
	}


	@Override
	public void onSendMessage(Account acct) throws ServiceException {
		final org.openzal.zal.Account zalAccount = new org.openzal.zal.Account(acct);
		final Boolean isOverQuota = this.zalQuotaAdapter.onSendMessage(zalAccount);
		if (isOverQuota) {
			throw MailServiceException.QUOTA_EXCEEDED(0);
		}
	}

	@Override
	public void onAddMessage(Account acct, long newTotalMailboxUsage) throws ServiceException {
		final org.openzal.zal.Account zalAccount = new org.openzal.zal.Account(acct);
		final Result result = this.zalQuotaAdapter.onAddMessage(zalAccount, newTotalMailboxUsage);
		if (!result.isSuccess()) {
			// TODO: retrieve limit from storages?
			throw MailServiceException.QUOTA_EXCEEDED(0);
		}
	}

	@Override
	public void onDeleteMessage(Account acct, long size) {
		final org.openzal.zal.Account zalAccount = new org.openzal.zal.Account(acct);
		this.zalQuotaAdapter.onDeleteMessage(zalAccount, size);
	}

}
