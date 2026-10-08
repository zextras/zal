package org.openzal.zal.mailbox;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.BlobBuilder;
import com.zimbra.cs.store.MailboxBlob;
import com.zimbra.cs.store.StagedBlob;
import com.zimbra.cs.store.StoreManager;
import com.zimbra.cs.store.file.VolumeMailboxBlob;
import com.zimbra.cs.store.file.VolumeStagedBlob;
import com.zimbra.cs.volume.VolumeManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.openzal.zal.FileBlobStoreWrap;

public class FileBlobStoreSimulatorWrap implements FileBlobStoreWrap
{
  private final ZalStoreManager mStore;

  public FileBlobStoreSimulatorWrap(ZalStoreManager store)
  {
    mStore = store;
  }

  @Override
  public void startup() throws IOException
  {
    try {
      mStore.startup();
    } catch (ServiceException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void shutdown()
  {
    mStore.shutdown();
  }

  @Override
  public boolean supports(Object feature)
  {
    return mStore.supports((StoreManager.StoreFeature) feature);
  }

  @Override
  public BlobBuilder getBlobBuilder() throws IOException
  {
    try {
      return mStore.getBlobBuilder();
    } catch (ServiceException e) {
      throw new IOException(e);
    }
  }

  @Override
  public Blob storeIncoming(InputStream in, boolean storeAsIs) throws IOException
  {
    try {
      return mStore.storeIncoming(in, storeAsIs);
    } catch (ServiceException e) {
      throw new IOException(e);
    }
  }

    @Override
  public VolumeStagedBlob stage(Blob blob, Mailbox mbox) throws IOException
  {
    String volumeId = String.valueOf(VolumeManager.getInstance().getCurrentMessageVolume().getId());
    return new MockVolumeStagedBlob(mbox, (ZalMockBlob) blob, volumeId);
  }


  @Override
  public VolumeMailboxBlob link(Blob src, Mailbox destMbox, int destItemId, int destRevision, String destVolumeId) throws IOException
  {
    return new MockVolumeMailboxBlob(mStore.link(src, destMbox, destItemId, destRevision), destVolumeId);
  }

  @Override
  public VolumeMailboxBlob renameTo(StagedBlob src, Mailbox destMbox, int destItemId, int destRevision) throws IOException
  {
    try {
      String volumeId = String.valueOf(VolumeManager.getInstance().getCurrentMessageVolume().getId());
      return new MockVolumeMailboxBlob(mStore.renameTo(src, destMbox, destItemId, destRevision), volumeId);
    } catch (ServiceException e) {
      throw new IOException(e);
    }
  }

  @Override
  public boolean delete(StagedBlob staged) throws IOException
  {
    return mStore.delete(staged);
  }

  @Override
  public boolean delete(Blob blob) throws IOException
  {
    return mStore.delete(blob);
  }

  @Override
  public MailboxBlob getMailboxBlob(Mailbox mbox, int itemId, int revision, String locator)
  {
    try {
      return mStore.getMailboxBlob(mbox, itemId, revision, locator);
    } catch (ServiceException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public InputStream getContent(MailboxBlob mboxBlob) throws IOException
  {
    return mStore.getContent(mboxBlob);
  }

  @Override
  public InputStream getContent(Blob blob) throws IOException
  {
    return mStore.getContent(blob);
  }

  @Override
  public boolean deleteStore(Mailbox mbox, Iterable blobs) throws IOException
  {
    try {
      return mStore.deleteStore(mbox, new ArrayList<>());
    } catch (ServiceException e) {
      throw new IOException(e);
    }
  }

}
