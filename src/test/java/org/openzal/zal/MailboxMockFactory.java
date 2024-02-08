package org.openzal.zal;

import org.mockito.Mockito;

public class MailboxMockFactory {

  public static Mailbox createMailbox() {
    com.zimbra.cs.mailbox.Mailbox realMailbox = Mockito.mock(com.zimbra.cs.mailbox.Mailbox.class);
    return new Mailbox(realMailbox);
  }

}
