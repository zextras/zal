package org.openzal.zal.mailbox;

import com.zimbra.cs.store.Blob;
import java.io.File;

public abstract class ZalMockBlob extends Blob {

  protected ZalMockBlob(File file) {
    super(file);
  }

  public abstract void remove();
}
