package org.openzal.zal.quota;

import org.openzal.zal.quota.QuotaResult.OverQuota;
import org.openzal.zal.quota.QuotaResult.Unavailable;
import org.openzal.zal.quota.QuotaResult.UnderQuota;

public sealed interface QuotaResult permits OverQuota, UnderQuota, Unavailable {

	public record OverQuota() implements QuotaResult {

	}

	public record UnderQuota() implements QuotaResult {

	}

	/**
	 * The quota could not be checked (e.g. the quota service is unreachable): the outcome is
	 * unknown, so the operation must be retried later instead of being rejected as over quota.
	 *
	 * @param reason human readable description of why the check could not be performed
	 * @param cause the underlying error, may be null
	 */
	public record Unavailable(String reason, Throwable cause) implements QuotaResult {

	}

}
