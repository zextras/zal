package org.openzal.zal.mailbox;

import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.file.VolumeBlob;
import java.io.IOException;
import org.openzal.zal.BlobWrap;
import org.openzal.zal.InternalOverrideBlobProxy;
import org.openzal.zal.ZalMailboxBlob;

public class MockVolumeBlob  extends VolumeBlob
{
  private final String mVolumeId;
  private final ZalMockBlob mMockBlob;

  MockVolumeBlob(Blob blob, String volumeId)
  {
    super(blob.getFile(), Short.parseShort(volumeId));
    if (blob instanceof ZalMockBlob)
    {
      mMockBlob = (ZalMockBlob) blob;
    }
    else if (blob instanceof MockVolumeBlob)
    {
      mMockBlob = (ZalMockBlob) blob;
    }
    else
    {
      mMockBlob = (ZalMockBlob) ((BlobWrap)((ZalMailboxBlob)(new InternalOverrideBlobProxy(blob).getWrappedObject())).getLocalBlob(false)).getWrappedObject();
    }
    mVolumeId = volumeId;
  }

  public String getDigest() throws IOException
  {
    return mMockBlob.getDigest();
  }

  public ZalMockBlob getMockBlob()
  {
    return mMockBlob;
  }

  public short getVolumeId()
  {
    return Short.parseShort(mVolumeId);
  }
}
