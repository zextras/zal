package org.openzal.zal;

import com.zimbra.common.service.ServiceException;

public class MailboxTestUtils {

  private MailboxTestUtils() {
  }

  public static void deleteItemsFromDumpster(Mailbox mailbox, int[] items) throws ServiceException {
    final com.zimbra.cs.mailbox.Mailbox zimbraMailbox = mailbox.toZimbra(com.zimbra.cs.mailbox.Mailbox.class);
    zimbraMailbox.deleteFromDumpster(zimbraMailbox.getOperationContext(), items);
  }

}
