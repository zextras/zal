/*
 * ZAL - The abstraction layer for Zimbra.
 * Copyright (C) 2016 ZeXtras S.r.l.
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
import com.zimbra.cs.store.file.VolumeMailboxBlob;
import com.zimbra.cs.store.file.VolumeStagedBlob;

import java.io.IOException;
import java.io.InputStream;

public interface FileBlobStoreWrap
{
    void startup() throws IOException, ServiceException;

    void shutdown();

    boolean supports(Object feature);

    BlobBuilder getBlobBuilder() throws IOException, ServiceException;

    Blob storeIncoming(InputStream in, boolean storeAsIs) throws IOException, ServiceException;

    VolumeStagedBlob stage(InputStream in, long actualSize, Mailbox mbox) throws IOException, ServiceException;

    VolumeStagedBlob stage(Blob blob, Mailbox mbox) throws IOException;

    VolumeMailboxBlob copy(MailboxBlob src, Mailbox destMbox, int destItemId, int destRevision)
    throws IOException, ServiceException;

    VolumeMailboxBlob copy(Blob src, Mailbox destMbox, int destItemId, int destRevision, String destVolumeId)
    throws IOException, ServiceException;

    VolumeMailboxBlob link(StagedBlob src, Mailbox destMbox, int destItemId, int destRevision)
    throws IOException, ServiceException;

    VolumeMailboxBlob link(Blob src, Mailbox destMbox, int destItemId, int destRevision, String destVolumeId)
    throws IOException, ServiceException;

    VolumeMailboxBlob renameTo(StagedBlob src, Mailbox destMbox, int destItemId, int destRevision)
    throws IOException, ServiceException;

    boolean delete(StagedBlob staged) throws IOException;

    boolean delete(Blob blob) throws IOException;

    MailboxBlob getMailboxBlob(Mailbox mbox, int itemId, int revision, String locator) throws ServiceException;

    InputStream getContent(MailboxBlob mboxBlob) throws IOException;

    InputStream getContent(Blob blob) throws IOException;

    boolean deleteStore(Mailbox mbox, Iterable blobs) throws IOException, ServiceException;

    Object getWrappedObject();
}
