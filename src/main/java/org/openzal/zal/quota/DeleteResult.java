package org.openzal.zal.quota;

import org.openzal.zal.quota.DeleteResult.DeleteFailure;
import org.openzal.zal.quota.DeleteResult.DeleteSuccess;

public sealed interface DeleteResult permits DeleteSuccess, DeleteFailure {

	public record DeleteSuccess() implements DeleteResult {

	}

	public record DeleteFailure() implements DeleteResult {

	}

}