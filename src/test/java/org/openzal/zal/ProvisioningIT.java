package org.openzal.zal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openzal.zal.lib.ZimbraVersion;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProvisioningIT
{
  private Provisioning mProvisioning;

  @BeforeEach
  public void setup()
  {
    mProvisioning = new ProvisioningImp(null);
  }

  @Test
  public void is_valid_uid_with_valid_return_true()
  {
    assertTrue(mProvisioning.isValidUid("e0fafd89-1360-11d9-8661-000a95d98ef2"));
  }

  @Test
  public void not_valid_uid_with_valid_return_false()
  {
    assertFalse(mProvisioning.isValidUid("e0fafd89x1360y11d9-8661-000a95d98ef2"));
    assertFalse(mProvisioning.isValidUid("ciao"));
  }
}
