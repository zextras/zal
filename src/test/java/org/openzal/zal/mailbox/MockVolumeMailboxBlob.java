package org.openzal.zal.mailbox;

import com.zimbra.cs.store.MailboxBlob;
import com.zimbra.cs.store.file.VolumeMailboxBlob;
import java.io.IOException;

public class MockVolumeMailboxBlob extends VolumeMailboxBlob
{
  public MockVolumeMailboxBlob(MailboxBlob blob, String volumeId) throws IOException
  {
    super(blob.getMailbox(), blob.getItemId(), blob.getRevision(), blob.getLocator(), new MockVolumeBlob(blob.getLocalBlob(), volumeId));
  }
}