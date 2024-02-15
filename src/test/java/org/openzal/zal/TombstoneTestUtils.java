package org.openzal.zal;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.zimbra.cs.db.DbMailItem;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.util.TypedIdList;

public class TombstoneTestUtils {

  public static void addItemToTombstoneWithoutDeletingIt(Item item, Mailbox mbox, String transactionName) throws Exception {
    TypedIdList list = new TypedIdList();
    list.add(item.toZimbra(MailItem.class).getType(), item.getId(), item.getUuid());
    mbox.beginTransaction(transactionName, mbox.newOperationContext());
    com.zimbra.cs.mailbox.Mailbox fakeMailbox = spy(mbox.getMailbox());
    when(fakeMailbox.getLastChangeID()).thenReturn(5000);
    DbMailItem.writeTombstones(fakeMailbox, list);
    mbox.endTransaction(true);
  }

}
