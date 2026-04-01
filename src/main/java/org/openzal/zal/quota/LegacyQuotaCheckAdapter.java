package org.openzal.zal.quota;

import com.zextras.mailbox.quota.QuotaCheck;
import com.zextras.mailbox.quota.QuotaCheckSingleton.DefaultQuotaCheck;
import com.zimbra.common.service.ServiceException;
import org.openzal.zal.Account;
import org.openzal.zal.quota.DeleteResult.DeleteFailure;
import org.openzal.zal.quota.DeleteResult.DeleteSuccess;

public class LegacyQuotaCheckAdapter implements QuotaCheckAdapter {

	private final QuotaCheck defaultImpl;

	public LegacyQuotaCheckAdapter() {
		this.defaultImpl = new DefaultQuotaCheck();
	}

	@Override
	public QuotaResult onSendMessage(Account acct) {
		try {
			defaultImpl.onSendMessage(acct.toZimbra(com.zimbra.cs.account.Account.class));
			return new QuotaResult.UnderQuota();
		} catch (ServiceException e) {
			return new QuotaResult.OverQuota();
		}
	}

	@Override
	public QuotaResult onAddMessage(Account acct, long newTotalMailboxUsage) {
		try {
			defaultImpl.onAddMessage(acct.toZimbra(com.zimbra.cs.account.Account.class),
					newTotalMailboxUsage);
			return new QuotaResult.UnderQuota();
		} catch (ServiceException e) {
			return new QuotaResult.OverQuota();
		}
	}

	@Override
	public DeleteResult onDeleteMessage(Account acct, long size) {
		try {
			defaultImpl.onAddMessage(acct.toZimbra(com.zimbra.cs.account.Account.class), size);
			return new DeleteSuccess();
		} catch (ServiceException e) {
			return new DeleteFailure();
		}
	}
}
