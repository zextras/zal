package org.openzal.zal.extension;

import org.openzal.zal.mailbox.ZalZimbraSimulator;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openzal.zal.Account;
import org.openzal.zal.Mailbox;
import org.openzal.zal.OperationContext;
import org.openzal.zal.exceptions.NoSuchItemException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExceptionWrapperIT
{
  @RegisterExtension
  public ZalZimbraSimulator mZimbraSimulator = new ZalZimbraSimulator();

  @Test
  public void wrap_new_no_suchitem_exception() throws Exception
  {
    Account account = mZimbraSimulator.getProvisioning().createAccount("test", "iddddd", new HashMap<String, Object>());
    Mailbox mbox = mZimbraSimulator.getMailboxManager().getMailboxByAccount(account);
    OperationContext octxt = mbox.newOperationContext();
    assertThrows(NoSuchItemException.class, () -> mbox.getTagById(octxt, 10));
  }
}