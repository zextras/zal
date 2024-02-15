package org.openzal.zal.mailbox;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.file.VolumeStagedBlob;
import java.io.IOException;

public class MockVolumeStagedBlob extends VolumeStagedBlob
{
  public MockVolumeStagedBlob(Mailbox mbox, ZalMockBlob blob, String volumeId) throws IOException
  {
    super(mbox, new MockVolumeBlob(blob, volumeId));
  }

  public String getDigest()
  {
    try
    {
      return getLocalBlob().getDigest();
    }
    catch (Exception e)
    {
      return "";
    }
  }
}