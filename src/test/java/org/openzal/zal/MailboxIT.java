package org.openzal.zal;

import org.openzal.zal.mailbox.ZalZimbraSimulator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MailboxIT
{
  private ZalZimbraSimulator mZimbraSimulator;

  @BeforeEach
  public void setup() throws Exception {
    mZimbraSimulator = new ZalZimbraSimulator();
  }

  @AfterEach
  public void cleanup() throws Exception {
    mZimbraSimulator.cleanup();
  }

  @Test
  public void reflection_initialization()
  {
    com.zimbra.cs.mailbox.Mailbox.MailboxData data = new com.zimbra.cs.mailbox.Mailbox.MailboxData();
    com.zimbra.cs.mailbox.Mailbox zimbraMbox = new com.zimbra.cs.mailbox.Mailbox(data){};
    Mailbox mbox = new Mailbox(zimbraMbox);
  }
}