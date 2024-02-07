package org.openzal.zal.mailbox;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.MailboxBlob;
import com.zimbra.cs.store.StoreManager;
import java.io.IOException;

public abstract class ZalStoreManager extends StoreManager {

  public abstract MailboxBlob copy(
      Blob src,
      Mailbox destMbox,
      int destItemId,
      int destRevision,
      String locator
  )
      throws IOException;

  public abstract MailboxBlob link(Blob src, Mailbox destMbox, int destItemId, int destRevision) throws IOException;
}
