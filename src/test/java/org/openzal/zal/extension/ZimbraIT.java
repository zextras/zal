package org.openzal.zal.extension;

import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.Test;
import org.openzal.zal.mailbox.ZalZimbraSimulator;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class ZimbraIT
{
  @RegisterExtension
  public ZalZimbraSimulator mZimbraSimulator = new ZalZimbraSimulator();

  @Test
  public void reflection_initialization()
  {
    Zimbra zimbra = mZimbraSimulator.getZimbra();
  }

  @Test
  public void remove_extension()
  {
    Zimbra zimbra = mZimbraSimulator.getZimbra();
    assertFalse(zimbra.removeExtension("not_existing_extension"));
  }
}