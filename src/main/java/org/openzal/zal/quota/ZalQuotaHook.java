package org.openzal.zal.quota;

import com.zextras.mailbox.quota.QuotaHook;
import com.zextras.mailbox.quota.QuotaHookSingleton;
import com.zimbra.cs.account.Account;
import java.util.function.Function;

public class ZalQuotaHook implements QuotaHook {

	private final Function<org.openzal.zal.Account, Boolean> overQuotaFunction;

	public ZalQuotaHook(Function<org.openzal.zal.Account, Boolean> overQuotaFunction) {
		this.overQuotaFunction = overQuotaFunction;
		QuotaHookSingleton.setInstance(this);
	}

	@Override
	public boolean isOverQuota(Account account) {
		final org.openzal.zal.Account zalAccount = new org.openzal.zal.Account(account);
		return overQuotaFunction.apply(zalAccount);
	}
}
