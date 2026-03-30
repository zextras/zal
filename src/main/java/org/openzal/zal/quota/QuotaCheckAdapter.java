package org.openzal.zal.quota;

import org.openzal.zal.Account;

public interface QuotaCheckAdapter {
	QuotaResult onSendMessage(Account acct);
	QuotaResult onAddMessage(Account acct, long newTotalMailboxUsage);
	void onDeleteMessage(Account acct, long size);
}
