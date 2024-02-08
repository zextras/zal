package org.openzal.zal;

import org.mockito.Mockito;

public class MailboxMockFactory {

  public static Mailbox createMailbox() {
    Mailbox mailbox = Mockito.mock(Mailbox.class);
    com.zimbra.cs.mailbox.Mailbox realMailbox = Mockito.mock(com.zimbra.cs.mailbox.Mailbox.class);
    Mockito.when(mailbox.getMailbox()).thenReturn(realMailbox);
    return mailbox;
  }

}
