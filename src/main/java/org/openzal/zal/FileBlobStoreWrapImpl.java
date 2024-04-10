/*
 * ZAL - Zextras Abstraction Layer.
 * Copyright (C) 2023 ZeXtras S.r.l.
 *
 * This file is part of ZAL.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZAL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.openzal.zal;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.BlobBuilder;
import com.zimbra.cs.store.MailboxBlob;
import com.zimbra.cs.store.StagedBlob;
import com.zimbra.cs.store.StoreManager;
import com.zimbra.cs.store.file.FileBlobStore;
import com.zimbra.cs.store.file.VolumeMailboxBlob;
import com.zimbra.cs.store.file.VolumeStagedBlob;

import java.io.IOException;
import java.io.InputStream;

public class FileBlobStoreWrapImpl implements FileBlobStoreWrap
{
  private final FileBlobStore mStore;

  public static String getFilename(int itemId, int revision)
  {
    return FileBlobStore.getFilename(itemId, revision);
  }

  @Override
  public void startup() throws IOException, ServiceException
  {
    mStore.startup();
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
  public BlobBuilder getBlobBuilder() throws IOException, ServiceException
  {
    return mStore.getBlobBuilder();
  }

  @Override
  public Blob storeIncoming(InputStream in, boolean storeAsIs) throws IOException, ServiceException
  {
    return mStore.storeIncoming(in, storeAsIs);
  }

  @Override
  public VolumeStagedBlob stage(InputStream in, long actualSize, Mailbox mbox) throws IOException, ServiceException
  {
    return mStore.stage(in, actualSize, mbox);
  }

  @Override
  public VolumeStagedBlob stage(Blob blob, Mailbox mbox) throws IOException
  {
    return mStore.stage(blob, mbox);
  }

  @Override
  public VolumeMailboxBlob copy(MailboxBlob src, Mailbox destMbox, int destItemId, int destRevision) throws IOException, ServiceException
  {
    return mStore.copy(src, destMbox, destItemId, destRevision);
  }

  @Override
  public VolumeMailboxBlob copy(Blob src, Mailbox destMbox, int destItemId, int destRevision, String destVolumeId) throws IOException, ServiceException
  {
    return mStore.copy(src, destMbox, destItemId, destRevision, Short.valueOf(destVolumeId));
  }

  @Override
  public VolumeMailboxBlob link(StagedBlob src, Mailbox destMbox, int destItemId, int destRevision) throws IOException, ServiceException
  {
    return mStore.link(src, destMbox, destItemId, destRevision);
  }

  @Override
  public VolumeMailboxBlob link(Blob src, Mailbox destMbox, int destItemId, int destRevision, String destVolumeId) throws IOException, ServiceException
  {
    return mStore.link(src, destMbox, destItemId, destRevision, Short.valueOf(destVolumeId));
  }

  @Override
  public VolumeMailboxBlob renameTo(StagedBlob src, Mailbox destMbox, int destItemId, int destRevision) throws IOException, ServiceException
  {
    return mStore.renameTo(src, destMbox, destItemId, destRevision);
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
  public MailboxBlob getMailboxBlob(Mailbox mbox, int itemId, int revision, String locator) throws ServiceException
  {
    return mStore.getMailboxBlob(mbox, itemId, revision, locator);
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
  public boolean deleteStore(Mailbox mbox, Iterable blobs) throws IOException, ServiceException
  {
    return mStore.deleteStore(mbox, blobs);
  }

  @Override
  public Object getWrappedObject()
  {
    return mStore;
  }

  public static String getBlobPath(Mailbox mbox, int itemId, int revision, short volumeId) throws ServiceException
  {
    return FileBlobStore.getBlobPath(mbox, itemId, revision, volumeId);
  }

  public FileBlobStoreWrapImpl(FileBlobStore store)
  {
    mStore = store;
  }

  public FileBlobStoreWrapImpl()
  {
    this(new FileBlobStore());
  }
}
