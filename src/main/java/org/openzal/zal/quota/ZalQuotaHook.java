package org.openzal.zal.quota;

import com.zextras.mailbox.quota.IsOverQuota;
import com.zextras.mailbox.quota.QuotaHook;
import com.zextras.mailbox.quota.QuotaHookSingleton;
import com.zimbra.cs.account.Account;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ZalQuotaHook implements QuotaHook {

	private final Function<org.openzal.zal.Account, Boolean> overQuotaFunction;
	private final BiFunction<org.openzal.zal.Account, Long, Result> addMessageFunction;
	private final BiConsumer<org.openzal.zal.Account, Long> deleteMessageFunction;

	public static synchronized void setInstance(
			Function<org.openzal.zal.Account, Boolean> overQuotaFunction,
			BiFunction<org.openzal.zal.Account, Long, Result> addMessageFunction,
			BiConsumer<org.openzal.zal.Account, Long> deleteMessageFunction
	) {
		var hook = new ZalQuotaHook(overQuotaFunction, addMessageFunction, deleteMessageFunction);
		QuotaHookSingleton.setInstance(hook);
	}

	private ZalQuotaHook(Function<org.openzal.zal.Account, Boolean> overQuotaFunction,
			BiFunction<org.openzal.zal.Account, Long, Result> addMessageFunction,
			BiConsumer<org.openzal.zal.Account, Long> deleteMessageFunction) {
		this.overQuotaFunction = overQuotaFunction;
		this.addMessageFunction = addMessageFunction;
		this.deleteMessageFunction = deleteMessageFunction;
	}


	@Override
	public IsOverQuota getQuota(Account acct) {
		final org.openzal.zal.Account zalAccount = new org.openzal.zal.Account(acct);
		return new IsOverQuota(overQuotaFunction.apply(zalAccount));
	}

	@Override
	public IsOverQuota addMessage(Account acct, long newTotalMailboxUsage) {
		final org.openzal.zal.Account zalAccount = new org.openzal.zal.Account(acct);
		final Result result = addMessageFunction.apply(zalAccount, newTotalMailboxUsage);
		if (!result.isSuccess()) {
			return new IsOverQuota(true);
		}
		return new IsOverQuota(false);
	}

	@Override
	public void deleteMessage(Account acct, long size) {
		final org.openzal.zal.Account zalAccount = new org.openzal.zal.Account(acct);
		deleteMessageFunction.accept(zalAccount, size);
	}

	public record Result(boolean isSuccess) {}
}
