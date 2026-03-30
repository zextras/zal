package org.openzal.zal.quota;

import org.openzal.zal.Account;

public interface ZalQuotaAdapter {
	Boolean onSendMessage(Account acct);
	Result onAddMessage(Account acct, long newTotalMailboxUsage);
	void onDeleteMessage(Account acct, long size);
}
