package org.openzal.zal.quota;

import org.openzal.zal.quota.QuotaResult.OverQuota;
import org.openzal.zal.quota.QuotaResult.UnderQuota;

public sealed interface QuotaResult permits OverQuota, UnderQuota {

	public record OverQuota() implements QuotaResult {

	}

	public record UnderQuota() implements QuotaResult {

	}

}
