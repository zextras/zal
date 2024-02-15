package org.openzal.zal.mailbox;

public class MailboxManagerUtils {
  public static void reset() {
    com.zimbra.cs.mailbox.MailboxManager.setInstance(null);
  }
}
