package org.openzal.zal.mailbox;

import com.zimbra.cs.store.Blob;
import java.io.File;

public abstract class ZalMockBlob extends Blob {

  protected ZalMockBlob(File file) {
    super(file);
  }

  protected ZalMockBlob(File file, long rawSize, String digest) {
    super(file, rawSize, digest);
  }
}
