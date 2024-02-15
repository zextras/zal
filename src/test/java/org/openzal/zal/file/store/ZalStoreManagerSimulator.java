package org.openzal.zal.file.store;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.BlobBuilder;
import com.zimbra.cs.store.BlobInputStream;
import com.zimbra.cs.store.FileDescriptorCache;
import com.zimbra.cs.store.MailboxBlob;
import com.zimbra.cs.store.StagedBlob;
import com.zimbra.cs.volume.Volume;
import com.zimbra.cs.volume.VolumeManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.function.BiConsumer;
import org.apache.commons.io.IOUtils;
import org.openzal.zal.BlobWrap;
import org.openzal.zal.Utils;
import org.openzal.zal.ZalMailboxBlob;
import org.openzal.zal.mailbox.MockStagedBlob;
import org.openzal.zal.mailbox.MockVolumeBlob;
import org.openzal.zal.mailbox.MockVolumeMailboxBlob;
import org.openzal.zal.mailbox.MockVolumeStagedBlob;
import org.openzal.zal.mailbox.ZalMockBlob;
import org.openzal.zal.mailbox.ZalStoreManager;

public abstract class ZalStoreManagerSimulator extends ZalStoreManager {

  public void startup() throws IOException {
    BlobInputStream.setFileDescriptorCache(new FileDescriptorCache(null));
  }

  public void shutdown() {
    purge();
    BlobInputStream.setFileDescriptorCache(null);
  }

  public boolean supports(StoreFeature feature) {
    return false;
  }

  public boolean supports(StoreFeature storeFeature, String s) {
    return false;
  }

  public abstract void purge();

  public abstract BlobBuilder getBlobBuilder();

  public abstract ZalMockBlob createMockBlob(String id);

  public abstract OutputStream openOutputStream(String id);

  protected abstract void finish(ZalMockBlob mockblob, byte content[]) throws IOException;

  public Blob storeIncoming(InputStream data, boolean storeAsIs) throws IOException {
    String id = UUID.randomUUID().toString();
    ZalMockBlob mockblob = createMockBlob(id);

    OutputStream writer;
    MessageDigest messageDigest;
    try {
      writer = openOutputStream(id);
      messageDigest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    try {
      DigestOutputStream digestOutputStream = new DigestOutputStream(writer, messageDigest);
      int size = IOUtils.copy(data, digestOutputStream);
      mockblob.setDigest(Utils.encodeFSSafeBase64(messageDigest.digest()));
      mockblob.setRawSize(size);
    } finally {
      IOUtils.closeQuietly(writer);
    }

    return mockblob;
  }

  public StagedBlob stage(InputStream data, long actualSize, Mailbox mbox) throws IOException {
    return new MockStagedBlob(mbox, (ZalMockBlob) storeIncoming(data, false));
  }

  public StagedBlob stage(Blob blob, Mailbox mbox) {
    return new MockStagedBlob(mbox, (ZalMockBlob) blob);
  }

  private String getBlobDir(short volumeId, int mboxId, int itemId) {
    Volume vol;
    try {
      vol = VolumeManager.getInstance().getVolume(volumeId);
      return System.getProperty("user.dir") + vol.getBlobDir(mboxId, itemId);
    } catch (ServiceException e) {
      throw new RuntimeException(e);
    }
  }

  public String getBlobPath(long mboxId, int itemId, int revision, short volumeId) {
    String path = getBlobDir(volumeId, (int) mboxId, itemId);

    int buflen = path.length() + 15 + (revision < 0 ? 0 : 11);
    StringBuilder sb = new StringBuilder(buflen);

    sb.append(path).append(File.separator).append(itemId);
    if (revision >= 0) {
      sb.append('-').append(revision);
    }
    sb.append(".msg");

    String finalPath = sb.toString();
    return finalPath.substring(1, finalPath.length());
  }

  public String getBlobPath(MailboxBlob mboxBlob) {
    if (mboxBlob instanceof MockVolumeMailboxBlob) {
      MockVolumeMailboxBlob blob = (MockVolumeMailboxBlob) mboxBlob;
      return getBlobPath(
          blob.getMailbox().getId(),
          blob.getItemId(),
          blob.getRevision(),
          blob.getLocalBlob().getVolumeId()
      );
    }
    MockMailboxBlob blob = (MockMailboxBlob) mboxBlob;
    return getBlobPath(
        blob.getMailbox().getId(),
        blob.getItemId(),
        blob.getRevision(),
        blob.volumeId()
    );
  }

  public short currentVolume() {
    return VolumeManager.getInstance().getCurrentMessageVolume().getId();
  }

  public MailboxBlob copy(
      Blob srcBlob,
      Mailbox destMbox,
      int destItemId,
      int destRevision,
      String locator
  )
      throws IOException {
    String id = copy((ZalMockBlob) srcBlob, destMbox.getId(), destItemId, destRevision, locator);

    ZalMockBlob mockBlob = createMockBlob(id);
    MockStagedBlob mockStagedBlob = new MockStagedBlob(destMbox, mockBlob);

    return new MockMailboxBlob(
        destMbox,
        destItemId,
        destRevision,
        locator,
        mockStagedBlob
    );
  }

  protected abstract String copy(ZalMockBlob srcBlob, int mailboxId, int destItemId, int destRevision, String locator) throws IOException;

  public MailboxBlob copy(MailboxBlob srcOr, Mailbox destMbox, int destItemId, int destRevision) throws IOException {
    return copy(
        ((MockMailboxBlob) srcOr).getMockStagedBlob().getMockBlob(),
        destMbox,
        destItemId,
        destRevision,
        srcOr.getLocator()
    );
  }

  public MailboxBlob link(StagedBlob src, Mailbox destMbox, int destItemId, int destRevision) throws IOException {
    MailboxBlob newBlob = copy(
        ((MockStagedBlob) src).getMockBlob(),
        destMbox,
        destItemId,
        destRevision,
        String.valueOf(currentVolume())
    );
    return newBlob;
  }

  public MailboxBlob link(Blob src, Mailbox destMbox, int destItemId, int destRevision) throws IOException {
    if (src instanceof MockVolumeBlob) {
      src = ((MockVolumeBlob) src).getMockBlob();
    }
    MailboxBlob newBlob = copy(
        src,
        destMbox,
        destItemId,
        destRevision,
        String.valueOf(currentVolume())
    );
    return newBlob;
  }

  public MailboxBlob link(MailboxBlob src, Mailbox destMbox, int destItemId, int destRevision) throws IOException {
    MailboxBlob newBlob = copy(
        src.getLocalBlob(),
        destMbox,
        destItemId,
        destRevision,
        String.valueOf(currentVolume())
    );
    return newBlob;
  }

  public MailboxBlob renameTo(StagedBlob src, Mailbox destMbox, int destItemId, int destRevision) throws IOException {
    MailboxBlob newBlob = copy(
        ((MockVolumeBlob) ((MockVolumeStagedBlob) src).getLocalBlob()).getMockBlob(),
        destMbox,
        destItemId,
        destRevision,
        String.valueOf(currentVolume())
    );

    try {
      ZalMockBlob mb = ((MockVolumeBlob) ((MockVolumeStagedBlob) src).getLocalBlob()).getMockBlob();
      mb.remove();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return newBlob;
  }

  public boolean delete(Blob blob) throws IOException {
    try {
      if (blob instanceof ZalMockBlob) {
        ((ZalMockBlob) blob).remove();
      }
      // TODO: blob can be instanceof InternalOverrideBlobWithMailboxInfo but it's not accessible from here (zimbra 8.0.3)
    } catch (Exception e) {
      throw new IOException(e);
    }
    return true;
  }

  public boolean delete(StagedBlob staged) {
    try {
      ZalMockBlob mb = ((MockVolumeBlob) ((MockVolumeStagedBlob) staged).getLocalBlob()).getMockBlob();
      mb.remove();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return true;
  }

  public boolean delete(MailboxBlob mblob) throws IOException {
    return delete(getBlobPath(mblob));
  }

  protected abstract boolean delete(String id) throws IOException;

  public MailboxBlob getMailboxBlob(Mailbox mbox, int itemId, int revision, String locator) {
    String path = getBlobPath(mbox.getId(), itemId, revision, Short.valueOf(locator));

    ZalMockBlob mockBlob = createMockBlob(path);

    MockStagedBlob mockStagedBlob = new MockStagedBlob(
        mbox,
        mockBlob
    );

    return new MockMailboxBlob(mbox, itemId, revision, locator, mockStagedBlob);
  }

  public MailboxBlob getMailboxBlob(Mailbox mbox, int itemId, int revision, String locator, boolean checkFileExistance) {
    MailboxBlob mailboxBlob = getMailboxBlob(mbox, itemId, revision, locator);

    InputStream inputStream = null;
    try {
      inputStream = mailboxBlob.getLocalBlob().getInputStream();
    } catch (IOException ex) {
      return null;
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException ignore) {
        }
      }
    }
    return mailboxBlob;
  }

  public InputStream getContent(MailboxBlob mblob) throws IOException {
    return mblob.getLocalBlob().getInputStream();
  }

  public InputStream getContent(Blob blob) throws IOException {
    return blob.getInputStream();
  }

  protected abstract boolean deleteStore() throws IOException;

  public boolean deleteStore(Mailbox mbox, Iterable<MailboxBlob.MailboxBlobInfo> mblobs) throws IOException {
    return deleteStore();
  }

  @SuppressWarnings("serial")
  public static class MockMailboxBlob extends MailboxBlob {

    public MockStagedBlob getMockStagedBlob() {
      return mMockStagedBlob;
    }

    private final MockStagedBlob mMockStagedBlob;

    public MockMailboxBlob(Mailbox mbox, int itemId, int revision, String locator, MockStagedBlob mockStagedBlob) {
      super(mbox, itemId, revision, locator);
      mMockStagedBlob = mockStagedBlob;
    }

    public Blob getLocalBlob() throws IOException {
      return new ZalMailboxBlob(
          BlobWrap.wrapZimbraBlob(mMockStagedBlob.getMockBlob()),
          new org.openzal.zal.Mailbox(mMockStagedBlob.getMailbox()),
          getItemId(),
          getRevision()
      ) {
        @Override
        public org.openzal.zal.Blob getLocalBlob() {
          return getLocalBlob(false);
        }

        @Override
        public String getVolumeId() {
          return "1";
        }
      }.toZimbra(Blob.class);
    }

    public short volumeId() {
      return Short.valueOf(getLocator());
    }
  }

  public static class MockBlobBuilder extends BlobBuilder {

    private final BiConsumer<ZalMockBlob, byte[]> finish;
    private byte[] out;

    public MockBlobBuilder(Blob blob, BiConsumer<ZalMockBlob, byte[]> finish) {
      super(blob);
      this.finish = finish;
    }

    protected FileChannel getFileChannel() {
      return null;
    }

    public Blob finish() throws IOException, ServiceException {
      ZalMockBlob mockblob = (ZalMockBlob) super.finish();

      if (out != null) {
        this.finish.accept(mockblob, out);
      }

      return mockblob;
    }
  }
}
