package org.openzal.zal.quota;

import com.zextras.mailbox.quota.IsOverQuota;
import com.zextras.mailbox.quota.QuotaHook;
import com.zimbra.cs.account.Account;
import io.vavr.Function2;
import io.vavr.control.Try;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ZalQuotaHook implements QuotaHook {

	private final Function<org.openzal.zal.Account, Boolean> overQuotaFunction;
	private final Function2<org.openzal.zal.Account, Long, Try<Void>> addMessageFunction;
	private final BiConsumer<org.openzal.zal.Account, Long> deleteMessageFunction;

	public ZalQuotaHook(Function<org.openzal.zal.Account, Boolean> overQuotaFunction,
			Function2<org.openzal.zal.Account, Long, Try<Void>> addMessageFunction,
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
		final Try<Void> apply = addMessageFunction.apply(zalAccount).apply(newTotalMailboxUsage);
		if (apply.isFailure()) {
			return new IsOverQuota(true);
		}
		return new IsOverQuota(false);
	}

	@Override
	public void deleteMessage(Account acct, long size) {
		final org.openzal.zal.Account zalAccount = new org.openzal.zal.Account(acct);
		deleteMessageFunction.accept(zalAccount, size);
	}
}
