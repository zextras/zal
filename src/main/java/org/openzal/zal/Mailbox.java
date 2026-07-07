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
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.db.DbMailbox;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.fb.FreeBusyQuery;
import com.zimbra.cs.index.SearchParams;
import com.zimbra.cs.index.SortBy;
import com.zimbra.cs.index.ZimbraQueryResults;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.CalendarItem.ReplyInfo;
import com.zimbra.cs.mailbox.DeliveryOptions;
import com.zimbra.cs.mailbox.Folder.FolderOptions;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.Mailbox.DeleteBlobs;
import com.zimbra.cs.mailbox.calendar.RecurId;
import com.zimbra.cs.mailbox.util.TypedIdList;
import com.zimbra.cs.service.FileUploadServlet.Upload;
import com.zimbra.cs.service.mail.ItemActionHelper;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.session.Session;
import org.apache.commons.dbutils.DbUtils;
import org.openzal.zal.calendar.CalendarItemData;
import org.openzal.zal.calendar.Invite;
import org.openzal.zal.calendar.RecurrenceId;
import org.openzal.zal.exceptions.ExceptionWrapper;
import org.openzal.zal.exceptions.InternalServerException;
import org.openzal.zal.exceptions.NoSuchAccountException;
import org.openzal.zal.exceptions.NoSuchCalendarException;
import org.openzal.zal.exceptions.NoSuchConversationException;
import org.openzal.zal.exceptions.NoSuchFolderException;
import org.openzal.zal.exceptions.NoSuchFreeBusyException;
import org.openzal.zal.exceptions.NoSuchItemException;
import org.openzal.zal.exceptions.NoSuchMessageException;
import org.openzal.zal.exceptions.PermissionDeniedException;
import org.openzal.zal.exceptions.ZimbraException;
import org.openzal.zal.lib.ZimbraConnectionWrapper;
import org.openzal.zal.lib.ZimbraDatabase;
import org.openzal.zal.log.ZimbraLog;
import org.openzal.zal.redolog.RedoLogProvider;
import org.openzal.zal.redolog.op.RawSetConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Mailbox
{
  @Nonnull final private com.zimbra.cs.mailbox.Mailbox mMbox;
  @Nonnull private final MailboxIndex mIndex;

  public static final int ID_FOLDER_USER_ROOT     = com.zimbra.cs.mailbox.Mailbox.ID_FOLDER_USER_ROOT;
  public static final int ID_FOLDER_INBOX         = com.zimbra.cs.mailbox.Mailbox.ID_FOLDER_INBOX;
  public static final int ID_FOLDER_TRASH         = com.zimbra.cs.mailbox.Mailbox.ID_FOLDER_TRASH;
  public static final int ID_FOLDER_SPAM          = com.zimbra.cs.mailbox.Mailbox.ID_FOLDER_SPAM;
  public static final int ID_FOLDER_SENT          = com.zimbra.cs.mailbox.Mailbox.ID_FOLDER_SENT;
  public static final int ID_FOLDER_DRAFTS        = com.zimbra.cs.mailbox.Mailbox.ID_FOLDER_DRAFTS;
  public static final int ID_FOLDER_CONTACTS      = com.zimbra.cs.mailbox.Mailbox.ID_FOLDER_CONTACTS;
  public static final int ID_FOLDER_TAGS          = com.zimbra.cs.mailbox.Mailbox.ID_FOLDER_TAGS;
  public static final int ID_FOLDER_CONVERSATIONS = com.zimbra.cs.mailbox.Mailbox.ID_FOLDER_CONVERSATIONS;
  public static final int ID_FOLDER_CALENDAR      = com.zimbra.cs.mailbox.Mailbox.ID_FOLDER_CALENDAR;
  public static final int ID_FOLDER_ROOT          = com.zimbra.cs.mailbox.Mailbox.ID_FOLDER_ROOT;
  @Deprecated
  public static final int ID_FOLDER_AUTO_CONTACTS = com.zimbra.cs.mailbox.Mailbox.ID_FOLDER_AUTO_CONTACTS;
  public static final int ID_FOLDER_IM_LOGS       = com.zimbra.cs.mailbox.Mailbox.ID_FOLDER_IM_LOGS;
  public static final int ID_FOLDER_COMMENTS      = 17;

  private static final int HIGHEST_SYSTEM_ID = com.zimbra.cs.mailbox.Mailbox.HIGHEST_SYSTEM_ID;
  public static final  int FIRST_USER_ID     = com.zimbra.cs.mailbox.Mailbox.FIRST_USER_ID;

  private static final Set<String> CREATE_CALENDAR_ITEM_ALLOWED_METHODS = new HashSet<>(Arrays.asList("PUBLISH", "REQUEST"));

  public long getSize()
  {
    return mMbox.getSize();
  }

  public static int getHighestSystemId()
  {
    return HIGHEST_SYSTEM_ID;
  }

  public void emptyFolder(@Nonnull OperationContext zContext, int folderId, boolean withDeleteSubFolders)
  {
    try
    {
      mMbox.emptyFolder(zContext.getOperationContext(), folderId, withDeleteSubFolders);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public MailboxData getMailboxData()
  {
    return new MailboxData(
            getId(),
            getSchemaGroupId(),
            getAccountId(),
            getIndexVolume()
    );
  }

  public void deleteFromDumpster(OperationContext newOperationContext, int[] ids) {
    try {
      mMbox.deleteFromDumpster(newOperationContext.getOperationContext(), ids);
    } catch( ServiceException e ) {
      throw ExceptionWrapper.wrap(e);
    }
  }

  static class FakeMailbox extends com.zimbra.cs.mailbox.Mailbox
  {
    public FakeMailbox(@Nonnull com.zimbra.cs.account.Account account)
    {
      super(createMailboxMetadata(account));
    }

    public FakeMailbox(long id, String accountId, int schemaGroupId)
    {
      super(createMailboxMetadata((int)id, accountId, schemaGroupId));
    }

    @Nonnull
    private static MailboxData createMailboxMetadata(
            @Nonnull
            com.zimbra.cs.account.Account account
    )
    {
      MailboxData data = new MailboxData();
      data.id = -1;
      data.schemaGroupId = -1;
      data.accountId = account.getId();
      data.size = 0L;
      data.contacts = 0;
      data.indexVolumeId = 0;
      data.lastBackupDate = 0;
      data.lastItemId = 0;
      data.lastChangeId = 0;
      data.lastChangeDate = 0;
      data.lastWriteDate = 0;
      data.recentMessages = -1;
      data.trackSync = -1;
      data.trackImap = false;
      data.configKeys = new HashSet<>();

      return data;
    }

    @Nonnull
    private static MailboxData createMailboxMetadata(int id, String accountId, int schemaGroupId)
    {
      MailboxData data = new MailboxData();
      data.id = id;
      data.schemaGroupId = schemaGroupId;
      data.accountId = accountId;
      data.size = 0L;
      data.contacts = 0;
      data.indexVolumeId = 0;
      data.lastBackupDate = 0;
      data.lastItemId = 0;
      data.lastChangeId = 0;
      data.lastChangeDate = 0;
      data.lastWriteDate = 0;
      data.recentMessages = -1;
      data.trackSync = -1;
      data.trackImap = false;
      data.configKeys = new HashSet<>();

      return data;
    }
  }

  public void migrateContactGroup()
  {
    try
    {
      com.zimbra.cs.mailbox.ContactGroup.MigrateContactGroup contactGroup =
              new com.zimbra.cs.mailbox.ContactGroup.MigrateContactGroup(mMbox);
      contactGroup.handle();
    }
    catch (ServiceException ex)
    {
      throw ExceptionWrapper.wrap(ex);
    }
  }

  public Mailbox(@Nullable Object mbox)
  {
    if (mbox == null)
    {
      throw new IllegalArgumentException("mMbox is null");
    }
    this.mMbox = (com.zimbra.cs.mailbox.Mailbox) mbox;

    mIndex = new MailboxIndex(this, this.mMbox.index);
  }

  @Nonnull
  public static Mailbox createFakeMailbox(@Nonnull Account realAccount)
  {
    return new Mailbox(
            new FakeMailbox(realAccount.toZimbra(com.zimbra.cs.account.Account.class))
    );
  }

  @Nonnull
  public static Mailbox createFakeMailbox(long id, String accountId, int schemaGroupId)
  {
    return new Mailbox(
            new FakeMailbox(id, accountId, schemaGroupId)
    );
  }

  public <T> T toZimbra(@Nonnull Class<T> cls)
  {
    return cls.cast(getMailbox());
  }

  @Nonnull
  public OperationContext newZimbraAdminContext()
  {
    return new OperationContext(
            new com.zimbra.cs.mailbox.OperationContext(
                    new ProvisioningImp(
                            com.zimbra.cs.account.Provisioning.getInstance()
                    ).getZimbraUser().toZimbra(com.zimbra.cs.account.Account.class)
                    ,true)
    );
  }

  @Nonnull
  public com.zimbra.cs.mailbox.Mailbox getMailbox()
  {
    return mMbox;
  }

  @Nonnull
  public Account getAccount()
          throws NoSuchAccountException
  {
    try
    {
      return new Account(mMbox.getAccount());
    }
    catch (ServiceException serviceException)
    {
      throw ExceptionWrapper.wrap(serviceException);
    }
  }

  public String getAccountId()
  {
    return mMbox.getAccountId();
  }

  public int getId()
  {
    return mMbox.getId();
  }

  public boolean hasListener(String listenerName)
  {
    return getListener(listenerName) != null;
  }

  public Object getListener(String listenerName)
  {
    return mMbox.getListener(listenerName);
  }

  public void registerListener(@Nonnull Listener listener)
  {
    try
    {
      mMbox.addListener(listener.getStoreContext().toZimbra(Session.class));
    }
    catch (ServiceException e)
    {
      ZimbraLog.mailbox.warn("Error adding listener to mailbox " +
              mMbox.getId() + ": " +
              e.getMessage());
    }
  }

  public void unregisterListener(@Nonnull Listener listener)
  {
    mMbox.removeListener(listener.getStoreContext().toZimbra(Session.class));
  }

  private static int getMailboxSyncCutoff(@Nonnull com.zimbra.cs.mailbox.Mailbox mMbox)
  {
    return mMbox.getSyncCutoff();
  }

  public boolean isTombstoneValid(int sequence)
  {
    int mboxSequence = getMailboxSyncCutoff(mMbox);
    return mboxSequence > 0 && sequence >= mboxSequence;
  }

  @Nonnull
  public Item getItemById(@Nonnull OperationContext zContext, int id, byte type)
          throws NoSuchItemException
  {
    MailItem item;
    try
    {
      item = mMbox.getItemById(zContext.getOperationContext(), id, Item.convertType(type));
    }
    catch (ServiceException serviceException)
    {
      throw ExceptionWrapper.wrap(serviceException);
    }
    return new Item(item);
  }

  @Nonnull
  public Item getItemByIdFromDumpster(@Nonnull OperationContext zContext, int id, byte type)
          throws NoSuchItemException
  {
    MailItem item;
    try
    {
      item = mMbox.getItemById(zContext.getOperationContext(), id, Item.convertType(type), true);
    }
    catch (ServiceException serviceException)
    {
      throw ExceptionWrapper.wrap(serviceException);
    }
    return new Item(item);
  }

  @Nonnull
  public Item getItemRevisionById(@Nonnull OperationContext zContext, int id, byte type, int revision)
          throws NoSuchItemException
  {
    MailItem item;
    try
    {
      item = mMbox.getItemRevision(zContext.getOperationContext(), id, Item.convertType(type), revision);
    }
    catch (ServiceException serviceException)
    {
      throw ExceptionWrapper.wrap(serviceException);
    }
    if (item == null)
    {
      throw new NoSuchItemException(id+"-"+revision);
    }
    return new Item(item);
  }

  public List<Item> getAllRevisionsIncludeDumpster(@Nonnull OperationContext zContext, int id, byte type)
  {
    List<Item> revisions = new ArrayList<>();
    try
    {
      revisions.addAll(getAllRevisions(zContext, id, type, false));
    }
    catch( Exception ignored ) {}
    try
    {
      revisions.addAll(getAllRevisions(zContext, id, type, true));
    }
    catch( Exception ignored ) {}
    return revisions;
  }

  @Nonnull
  public List<Item> getAllRevisions(@Nonnull OperationContext zContext, int id, byte type)
  {
    return getAllRevisions(zContext, id, type, false);
  }

  @Nonnull
  public List<Item> getAllRevisions(@Nonnull OperationContext zContext, int id, byte type, boolean inDumpster)
  {
    try
    {
      if (inDumpster)
      {
        beginTransaction("getAllRevisions", zContext);
        try
        {
          Item item = getItemByIdFromDumpster(zContext, id, type);

          MailItem mailItem = item.toZimbra(MailItem.class);
          List<Item> revisions;
          List<MailItem> revisionsObject = mailItem.loadRevisions();
          if (revisionsObject == null)
          {
            revisions = Collections.singletonList(item);
          }
          else
          {
            List<MailItem> zimbraRevisions = revisionsObject;
            revisions = new ArrayList<>(zimbraRevisions.size());
            for (MailItem zimbraItem : zimbraRevisions)
            {
              revisions.add(new Item(zimbraItem));
            }
            revisions.add(item);
          }

          return revisions;
        }
        finally
        {
          endTransaction(true);
        }
      }
      else
      {
        List<MailItem> mailItems = mMbox.getAllRevisions(zContext.getOperationContext(), id, Item.convertType(type));

        List<Item> items = new ArrayList<>(mailItems.size());
        for (MailItem mailItem : mailItems)
        {
          items.add(new Item(mailItem));
        }

        return items;
      }
    }
    catch (ServiceException serviceException)
    {
      throw ExceptionWrapper.wrap(serviceException);
    }
  }

  @Nonnull
  public Message getMessageById(@Nonnull OperationContext zContext, int id)
          throws NoSuchMessageException
  {
    try
    {
      com.zimbra.cs.mailbox.Message message = mMbox.getMessageById(zContext.getOperationContext(), id);
      return new Message(message);
    }
    catch (Exception exception)
    {
      throw ExceptionWrapper.wrap(exception);
    }
  }

  @Nonnull
  public List<Message> getMessagesByConversation(@Nonnull OperationContext zContext, int id)
          throws NoSuchConversationException
  {
    List<com.zimbra.cs.mailbox.Message> list;
    try
    {
      list = mMbox.getMessagesByConversation(zContext.getOperationContext(), id);
    }
    catch (Exception exception)
    {
      throw ExceptionWrapper.wrap(exception);
    }
    List<Message> newList = new ArrayList<>(list.size());

    for (com.zimbra.cs.mailbox.Message item : list)
    {
      newList.add(new Message(item));
    }

    return newList;
  }

  public List<Integer> listTombstones(int sequence)
  {
    try
    {
      return mMbox.getTombstones(sequence).getAllIds();
    }
    catch (ServiceException serviceException)
    {
      throw ExceptionWrapper.wrap(serviceException);
    }
  }

  @Nonnull
  public Folder getFolderByName(@Nonnull OperationContext zContext, String name, int parentId)
          throws NoSuchFolderException
  {
    MailItem folder;
    try
    {
      folder = mMbox.getFolderByName(zContext.getOperationContext(), parentId, name);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }

    return new Folder(folder);
  }

  @Nonnull
  public Folder getFolderByPath(@Nonnull OperationContext zContext, String path)
          throws NoSuchFolderException
  {
    MailItem folder;
    try
    {
      folder = mMbox.getFolderByPath(zContext.getOperationContext(), path);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }

    return new Folder(folder);
  }

  @Nonnull
  public List<Folder> getFolderList(@Nonnull OperationContext zContext)
          throws NoSuchFolderException
  {
    List<Folder> folderList = new ArrayList<>(0);
    try
    {
      for (com.zimbra.cs.mailbox.Folder folder : mMbox
              .getFolderList(zContext.getOperationContext(), SortBy.NONE)) {
        folderList.add(new Folder(folder));
      }

    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }

    return folderList;
  }

  @Nonnull
  public Folder getFolderById(@Nonnull OperationContext zContext, int id)
          throws NoSuchFolderException
  {
    MailItem folder;
    try
    {
      folder = mMbox.getFolderById(zContext.getOperationContext(), id);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }

    return new Folder(folder);
  }

  @Nonnull
  public Mountpoint getMountpointById(@Nonnull OperationContext octxt, int id)
  {
    MailItem mountpoint;
    try
    {
      mountpoint = mMbox.getMountpointById(octxt.getOperationContext(), id);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }

    return new Mountpoint(mountpoint);
  }

  @Nonnull
  public CalendarItem getCalendarItemById(@Nonnull OperationContext octxt, int id)
          throws NoSuchCalendarException
  {
    try
    {
      return new CalendarItem(mMbox.getCalendarItemById(octxt.getOperationContext(), id));
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  @Nonnull
  public CalendarItem getCalendarItemByUid(@Nonnull OperationContext octxt, String uid)
          throws NoSuchCalendarException
  {
    MailItem mailItem;
    try
    {
      mailItem = mMbox.getCalendarItemByUid(octxt.getOperationContext(), uid);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }

    if (mailItem == null)
    {
      throw new NoSuchCalendarException(uid);
    }

    return new CalendarItem(mailItem);
  }

  @Nonnull
  public FreeBusy getFreeBusy(@Nonnull OperationContext octxt, long start, long end)
          throws NoSuchItemException
  {
    com.zimbra.cs.fb.FreeBusy freeBusy;
    try
    {
      freeBusy = mMbox.getFreeBusy(octxt.getOperationContext(),start,end,FreeBusyQuery.CALENDAR_FOLDER_ALL);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }

    if (freeBusy == null)
    {
      throw new NoSuchFreeBusyException(start, end);
    }

    return new FreeBusy(freeBusy);
  }

  public void copyCalendarReplyInfo(
          @Nonnull CalendarItem fromCalendarItem,
          CalendarItem toCalendarItem,
          @Nonnull OperationContext zContext
  )
  {
    synchronized (mMbox)
    {
      beginTransaction("ZxCalendarRepliesRestore", zContext);
      try
      {
        fromCalendarItem.copyReplyInfoTo(toCalendarItem);
      }
      finally
      {
        endTransaction(true);
      }
    }
  }

  public void rename(@Nonnull OperationContext zContext, int id, byte type, String name, int folderId)
          throws ZimbraException
  {
    try
    {
      mMbox.rename(zContext.getOperationContext(), id, Item.convertType(type), name, folderId);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public void delete(@Nonnull OperationContext octxt, int itemId, byte type)
          throws ZimbraException
  {
    try
    {
      mMbox.delete(octxt.getOperationContext(), itemId, Item.convertType(type));
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public void delete(@Nonnull OperationContext octxt, int[] itemIds, byte type)
          throws ZimbraException
  {
    try
    {
      mMbox.delete(octxt.getOperationContext(), itemIds, Item.convertType(type), null);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public void setPermissions(@Nonnull OperationContext zContext, int folderId, @Nonnull Acl acl)
          throws ZimbraException
  {
    try
    {
      mMbox.setPermissions(zContext.getOperationContext(), folderId, acl.toZimbra(ACL.class));
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public void setColor(@Nonnull OperationContext octxt, int[] itemIds, byte type, @Nonnull Item.Color color)
          throws ZimbraException
  {
    try
    {
      mMbox.setColor(
              octxt.getOperationContext(),
              itemIds,
              Item.convertType(type),
              color.toZimbra(com.zimbra.common.mailbox.Color.class)
      );
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  @Nonnull
  public CalendarItem setCalendarItem(
          @Nonnull OperationContext octxt, int folderId, int flags, String tags[],
          @Nonnull CalendarItemData defaultInv,
          @Nonnull List<CalendarItemData> exceptions,
          List<ReplyInfo> replies, long nextAlarm
  )
          throws ZimbraException
  {
    com.zimbra.cs.mailbox.Mailbox.SetCalendarItemData[] zimbraExceptions = null;
    if( exceptions.size() > 0 )
    {
      zimbraExceptions = new com.zimbra.cs.mailbox.Mailbox.SetCalendarItemData[exceptions.size()];
      for (int i = 0; i < exceptions.size(); i++)
      {
        zimbraExceptions[i] = exceptions.get(i).toZimbra(com.zimbra.cs.mailbox.Mailbox.SetCalendarItemData.class);
      }
    }

    try
    {
      com.zimbra.cs.mailbox.Mailbox.SetCalendarItemData calendarItemData = defaultInv.toZimbra(com.zimbra.cs.mailbox.Mailbox.SetCalendarItemData.class);
      boolean patchCalendarItemMethod = !CREATE_CALENDAR_ITEM_ALLOWED_METHODS.contains(calendarItemData.invite.getMethod());
      String oldMethod = calendarItemData.invite.getMethod();
      if (patchCalendarItemMethod) {
        calendarItemData.invite.setMethod("PUBLISH");
        com.zimbra.cs.mailbox.CalendarItem calendarItem = calendarItemData.invite.getCalendarItem();
        String cid = String.format("Message Id: %s from account id %s",
                calendarItem.getId(),
                calendarItem.getAccount().getId()
        );
        ZimbraLog.extensions.warn(String.format("Setting metadata method to 'PUBLISH', '%s' is not supported for calendar item %s", oldMethod, cid));
      }

      com.zimbra.cs.mailbox.CalendarItem inviteCalendarItem = calendarItemData.invite.getCalendarItem();
      List<ReplyInfo> newReplies = replies;
      if (calendarItemData.invite != null && calendarItemData.invite.getCalendarItem() != null) {
        newReplies = replies == null ? calendarItemData.invite.getCalendarItem().getAllReplies() : null;
      }

      CalendarItem result = new CalendarItem(
              mMbox.setCalendarItem(
                      octxt.getOperationContext(),
                      folderId,
                      flags,
                      tags,
                      calendarItemData,
                      zimbraExceptions,
                      newReplies,
                      nextAlarm
              )
      );
      return result;
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public
  @Nullable
  Metadata getConfig(@Nonnull OperationContext octxt, String section)
          throws ZimbraException
  {
    try
    {
      com.zimbra.cs.mailbox.Metadata metadata = mMbox.getConfig(octxt.getOperationContext(), section);
      if (metadata == null)
      {
        return null;
      }
      return new Metadata(metadata);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public void setConfig(@Nonnull OperationContext octxt, String section, @Nonnull Metadata config)
          throws ZimbraException
  {
    try
    {
      mMbox.setConfig(
              octxt.getOperationContext(),
              section,
              config.toZimbra(com.zimbra.cs.mailbox.Metadata.class));
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public void alterTag(@Nonnull OperationContext octxt, int itemId, byte type, int tagId, boolean addTag)
          throws ZimbraException
  {
    try
    {
      mMbox.alterTag(octxt.getOperationContext(), itemId, Item.convertType(type), Flag.of(tagId), addTag, null);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public void setTags(@Nonnull OperationContext octxt, int itemId, byte type, @Nullable Collection<String> tags)
          throws ZimbraException
  {
    String[] tagsArray;
    if (tags == null)
    {
      tagsArray = null;
    }
    else
    {
      tagsArray = tags.toArray(new String[tags.size()]);
    }

    MailItem item;
    try
    {
      item = mMbox.getItemById(octxt.getOperationContext(), itemId, Item.convertType(Item.TYPE_UNKNOWN));
      mMbox.setTags(
              octxt.getOperationContext(),
              itemId,
              Item.convertType(type),
              item.getFlagBitmask(),
              tagsArray
      );
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public void setFlags(@Nonnull OperationContext octxt, int itemId, byte type, int flags)
          throws ZimbraException
  {
    try
    {
      MailItem item = mMbox.getItemById(octxt.getOperationContext(), itemId, Item.convertType(Item.TYPE_UNKNOWN));
      mMbox.setTags(octxt.getOperationContext(), itemId, Item.convertType(type), flags,
              item.getTags()
      );
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public void modifyContact(@Nonnull OperationContext octxt, int contactId, @Nonnull ParsedContact pc)
          throws ZimbraException
  {
    try
    {
      mMbox.modifyContact(
              octxt.getOperationContext(),
              contactId,
              pc.toZimbra(com.zimbra.cs.mime.ParsedContact.class)
      );
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  @Nullable
  public ZimbraItemId sendMimeMessage(
          @Nonnull OperationContext octxt, Boolean saveToSent, MimeMessage mm,
          List<Upload> uploads,
          @Nullable ZimbraItemId origMsgId, String replyType,
          boolean replyToSender
  )
          throws ZimbraException
  {

    ItemId itemId = null;
    ItemId newItemId;

    if (origMsgId != null)
    {
      itemId = new ItemId(origMsgId.getAccountId().toString(), origMsgId.getItemId());
    }

    try
    {
      newItemId = mMbox.getMailSender().sendMimeMessage(
              octxt.getOperationContext(), mMbox, saveToSent, mm,
              uploads, itemId, replyType,
              null, replyToSender
      );

      if( newItemId == null ) {
        return null;
      }
      return new ZimbraItemId(newItemId.getAccountId(), newItemId.getId());
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public boolean attachmentsIndexingEnabled()
          throws ZimbraException
  {
    try
    {
      return mMbox.attachmentsIndexingEnabled();
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public void move(@Nonnull OperationContext octxt, int itemId, byte type, int targetId)
          throws ZimbraException
  {
    try
    {
      if (!canWrite(octxt,itemId) || !canWrite(octxt,targetId))
      {
        throw new PermissionDeniedException("Missing write permissions for " + octxt.getAccount().getName() + " on " + mMbox.getAccount().getMail() + " mailbox");
      }
      mMbox.move(octxt.getOperationContext(), itemId, Item.convertType(type), targetId);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public int move(@Nonnull Account dstAccount,@Nonnull OperationContext octxt, int itemId, byte type, int targetId)
          throws ZimbraException
  {
    try
    {
      ItemId zimbraItemId = new ItemId(
              dstAccount.getId(),
              targetId
      );
      ItemActionHelper op = ItemActionHelper.MOVE(octxt.getOperationContext(),
              mMbox,
              SoapProtocol.Soap12,
              Arrays.asList(itemId),
              Item.convertType(type),
              null,
              zimbraItemId);
      List<String> createdIds;
      createdIds = op.getResult().getSuccessIds();
      if (createdIds == null)
      {
        return itemId;
      }
      if (createdIds.size() != 1)
      {
        throw new NoSuchItemException(Integer.toString(itemId));
      }
      ItemId newZimbraItemId = new ItemId(createdIds.get(0),(String)null);
      return newZimbraItemId.getId();
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public List<CalendarItem> getCalendarItemsForRange(
          @Nonnull OperationContext octxt, byte type, long start,
          long end, int folderId, int[] excludeFolders
  )
          throws ZimbraException
  {
    try
    {
      List<com.zimbra.cs.mailbox.CalendarItem> zimbraCalendarItems = mMbox.getCalendarItemsForRange(
              octxt.getOperationContext(),
              Item.convertType(type),
              start,
              end,
              folderId,
              excludeFolders
      );

      return ZimbraListWrapper.wrapCalendarItems(zimbraCalendarItems);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public List<Integer> listItemIds(@Nonnull OperationContext octxt, byte type, int folderId)
          throws NoSuchItemException
  {
    try
    {
      return mMbox.listItemIds(octxt.getOperationContext(), Item.convertType(type), folderId);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  @Nonnull
  public Iterator<Map.Entry<Byte, List<Integer>>> getItemIds(@Nonnull OperationContext octxt, int folderId)
          throws ZimbraException
  {
    try
    {
      Map<Byte, List<Integer>> map = new HashMap<>();
      Iterator<Map.Entry<Type, List<TypedIdList.ItemInfo>>> iterator
              = mMbox.getItemIds(octxt.getOperationContext(), folderId).iterator();
      while (iterator.hasNext())
      {
        Map.Entry<Type, List<TypedIdList.ItemInfo>> entry = iterator.next();
        List<Integer> list = new ArrayList<>();
        for (TypedIdList.ItemInfo item : entry.getValue())
        {
          list.add(item.getId());
        }
        map.put(entry.getKey().toByte(), list);
      }
      return map.entrySet().iterator();
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public boolean canRead(OperationContext octxt, int itemId)
  {
    short rights;

    try
    {
      rights = mMbox.getEffectivePermissions(octxt.getOperationContext(), itemId, Type.UNKNOWN);
    }
    catch (ServiceException e)
    {
      return false;
    }

    return (rights & Acl.RIGHT_ADMIN) != 0 || (rights & Acl.RIGHT_READ) != 0;
  }

  public boolean canWrite(OperationContext octxt, int itemId)
  {
    short rights;

    try
    {
      rights = mMbox.getEffectivePermissions(octxt.getOperationContext(), itemId, Type.UNKNOWN);
    }
    catch (ServiceException e)
    {
      return false;
    }

    return (rights & Acl.RIGHT_ADMIN) != 0 || (rights & Acl.RIGHT_WRITE) != 0;
  }

  public void modifyPartStat(
          @Nonnull OperationContext octxt, int calItemId,
          @Nullable RecurrenceId recurId, String cnStr,
          String addressStr, String cutypeStr,
          String roleStr, String partStatStr,
          Boolean rsvp, int seqNo, long dtStamp
  )
    throws ZimbraException
  {
    try
    {
      if (! canWrite(octxt, calItemId))
      {
        throw new PermissionDeniedException("Missing write permissions for " + octxt.getAccount().getName() + " on " + mMbox.getAccount().getMail() + " mailbox");
      }


      mMbox.modifyPartStat(
        octxt.getOperationContext(),
        calItemId,
        recurId == null ? null : recurId.toZimbra(RecurId.class),
        cnStr,
        addressStr,
        cutypeStr,
        roleStr,
        partStatStr,
        rsvp,
        seqNo,
        dtStamp);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  @Nonnull
  public Tag getTagById(@Nonnull OperationContext octxt, int itemId)
          throws NoSuchItemException
  {
    MailItem tag;
    try
    {
      tag = mMbox.getTagById(octxt.getOperationContext(), itemId);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
    return new Tag(tag);
  }

  @Nullable
  public Tag getTagByName(@Nonnull OperationContext octxt, String name)
          throws NoSuchItemException
  {
    try
    {
      com.zimbra.cs.mailbox.Tag tagByName = mMbox.getTagByName(octxt.getOperationContext(), name);
      if( Objects.isNull(tagByName) )
      {
        return null;
      }
      return new Tag(tagByName);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public void setCustomData(@Nonnull OperationContext octxt, int itemId, byte type, @Nonnull Item.CustomMetadata custom)
          throws ZimbraException
  {
    try
    {
      mMbox.setCustomData(
              octxt.getOperationContext(),
              itemId,
              Item.convertType(type),
              custom.toZimbra(MailItem.CustomMetadata.class)
      );
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  @Nonnull
  public OperationContext newOperationContext()
          throws ZimbraException
  {
    try
    {
      return new OperationContext(new com.zimbra.cs.mailbox.OperationContext(mMbox));
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public void beginTrackingSync()
          throws ZimbraException
  {
    try
    {
      mMbox.beginTrackingSync();
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public int getLastChangeID()
  {
    return mMbox.getLastChangeID();
  }

  public void clearItemCache() throws ZimbraException
  {
    mMbox.purge(Item.convertType(Item.TYPE_UNKNOWN));
  }

  public void purgeCache()
  {
    mMbox.purgeCache();
  }

  public void clearCache(byte type) throws ZimbraException
  {
    mMbox.purge(Item.convertType(type));
  }


  @Nonnull
  public QueryResults search(
          @Nonnull OperationContext octxt,
          String queryString,
          @Nonnull byte[] types,
          @Nonnull SortedBy sortBy,
          int chunkSize
  )
          throws ZimbraException
  {
    return search(octxt, queryString, types, sortBy, chunkSize, 0, false, false);
  }

  @Nonnull
  public QueryResults search(
          @Nonnull OperationContext octxt,
          String queryString,
          @Nonnull byte[] types,
          @Nonnull SortedBy sortBy,
          int chunkSize,
          int offset,
          boolean onlyIds
  )
          throws ZimbraException
  {
    return search(octxt, queryString, types, sortBy, chunkSize, offset, onlyIds, false);
  }

  public QueryResults search(
          OperationContext operationContext,
          org.openzal.zal.SearchParams  searchParams
  ) {
    ZimbraQueryResults result;
    try {
      result = mMbox.index.search(
              SoapProtocol.Soap12,
              operationContext.getOperationContext(),
              searchParams.toZimbra(SearchParams.class)
      );
    } catch (ServiceException e) {
      throw ExceptionWrapper.wrap(e);
    }

    return new QueryResults(
            result
    );
  }

  @Nonnull
  public QueryResults search(
          @Nonnull OperationContext octxt,
          String queryString,
          @Nonnull byte[] types,
          @Nonnull SortedBy sortBy,
          int chunkSize,
          int offset,
          boolean onlyIds,
          boolean inDumpster
  )
          throws ZimbraException
  {
    try
    {
      Set<Type> typeList = new HashSet(types.length);
      for (byte type : types)
      {
        typeList.add(Item.convertType(type));
      }

      SearchParams.Fetch fetchMode = onlyIds ? SearchParams.Fetch.IDS : SearchParams.Fetch.NORMAL;

      SearchParams params = new SearchParams();
      params.setQueryString(queryString);
      params.setTimeZone(null);
      params.setLocale(null);
      params.setTypes(typeList);
      params.setSortBy(sortBy.toZimbra(SortBy.class));
      params.setPrefetch(true);
      params.setFetchMode(fetchMode);
      params.setInDumpster(inDumpster);
      params.setLimit(chunkSize + offset);
      params.setOffset(offset);

      ZimbraQueryResults result = mMbox.index.search(
              SoapProtocol.Soap12,
              octxt.getOperationContext(),
              params
      );

      if( offset >= 1 )
        result.skipToHit(offset-1);

      return new QueryResults(
              result
      );
    }
    catch (Exception e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  @Nonnull
  public List<Item> getItemList(byte type, @Nonnull OperationContext zContext)
          throws ZimbraException
  {
    List<Item> result = new LinkedList<>();

    beginTransaction("ZxGetItemList", zContext.getOperationContext());
    try
    {
      if (type == Item.TYPE_FOLDER || type == Item.TYPE_SEARCHFOLDER || type == Item.TYPE_MOUNTPOINT)
      {
        final Collection<Folder> folderCache = getFolderCache();
        for (Folder subfolder : folderCache)
        {
          if (subfolder.getType() == type || type == Item.TYPE_FOLDER)
          {
            result.add(subfolder);
          }
        }
      }
      else if (type == Item.TYPE_TAG)
      {
        final Map<Object, com.zimbra.cs.mailbox.Tag> tagCache = getTagCache();
        for (Map.Entry<Object, com.zimbra.cs.mailbox.Tag> entry : tagCache.entrySet())
        {
          if (entry.getKey() instanceof String)
          {
            result.add(new Item(entry.getValue()));
          }
        }
      }
      else if (type == Item.TYPE_FLAG)
      {
        for (MailItem item : getAllFlags())
        {
          result.add(new Item(item));
        }
        return result;
      }
      else
      {
        List<Item.UnderlyingData> dataList = ZimbraDatabase.getByType(this, type, SortBy.NONE);

        if (dataList == null)
        {
          return Collections.emptyList();
        }

        for (Item.UnderlyingData data : dataList)
        {
          if (data != null)
          {
            try
            {
              Item item = rawGetItem(data);
              if(item != null)
              {
                result.add(item);
              }
            }
            catch (Throwable ex)
            {
              ZimbraLog.extensions.debug("getItemList(): skipping item: " + Utils.exceptionToString(ex));
            }
          }
        }
      }
    }
    finally
    {
      endTransaction(true);
    }

    return result;
  }

  @Nonnull
  public Folder createFolder(
          @Nonnull OperationContext octxt, String name, int parentId,
          byte attrs, byte defaultView, int flags,
          @Nonnull Item.Color color, String url
  )
          throws ZimbraException
  {
    MailItem folder;
    try
    {
      folder = mMbox.createFolder(
              octxt.getOperationContext(),
              name,
              parentId,
              attrs,
              Item.convertType(defaultView),
              flags,
              color.toZimbra(com.zimbra.common.mailbox.Color.class),
              url
      );
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }

    return new Folder(folder);
  }

  @Nonnull
  public Folder createFolder(
          OperationContext operationContext,
          String path
  ) {
    MailItem folder;
    try {
      FolderOptions fopts = new FolderOptions();
      folder = mMbox.createFolder(operationContext.getOperationContext(), path, fopts);
    } catch (ServiceException e) {
      throw ExceptionWrapper.wrap(e);
    }
    return new Folder(folder);
  }

  public void setFolderRetentionPolicy(@Nonnull OperationContext octxt, int folderId, RetentionPolicy retentionPolicy)
          throws ZimbraException {
    try {
      mMbox.setRetentionPolicy(
              octxt.getOperationContext(),
              folderId,
              Type.FOLDER,
              retentionPolicy.toZimbra(com.zimbra.soap.mail.type.RetentionPolicy.class)
      );
    } catch (ServiceException e) {
      throw ExceptionWrapper.wrap(e);
    }
  }

  @Nonnull
  public SearchFolder createSearchFolder(
          @Nonnull OperationContext octxt, int folderId, String name,
          String query, String types, String sort,
          int flags, @Nonnull Item.Color color
  )
          throws ZimbraException
  {
    MailItem item;
    try
    {
      item = mMbox.createSearchFolder(
              octxt.getOperationContext(),
              folderId,
              name,
              query,
              types,
              sort,
              flags,
              color.toZimbra(com.zimbra.common.mailbox.Color.class)
      );
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }

    return new SearchFolder(item);
  }

  @Nonnull
  public Tag createTag(@Nonnull OperationContext octxt, String name, @Nonnull Item.Color color)
          throws ZimbraException
  {
    MailItem tag;
    try
    {
      tag = mMbox.createTag(
              octxt.getOperationContext(),
              name,
              color.toZimbra(com.zimbra.common.mailbox.Color.class)
      );
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }

    return new Tag(tag);
  }

  @Nonnull
  public Message addMessage(
          @Nonnull OperationContext octxt, InputStream in, int sizeHint, Long receivedDate,
          int folderId, boolean noIcal,
          int flags, Collection<String> tags, int conversationId, String rcptEmail,
          @Nullable Item.CustomMetadata customData
  )
          throws IOException, ZimbraException
  {
    DeliveryOptions opts = new DeliveryOptions();
    opts.setFolderId(folderId);
    opts.setNoICal(noIcal);
    opts.setFlags(flags);
    opts.setTags(tags);
    opts.setConversationId(conversationId);
    opts.setRecipientEmail(rcptEmail);
    opts.setDraftInfo(null);
    if (customData != null)
    {
      opts.setCustomMetadata(customData.toZimbra(MailItem.CustomMetadata.class));
    }
    MailItem message;
    try
    {
      message = mMbox.addMessage(octxt.getOperationContext(),
              in,
              sizeHint,
              receivedDate,
              opts,
              null
      );
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }

    return new Message(message);
  }

  @Nonnull
  public Message simpleAddMessage(
          @Nonnull OperationContext octxt,
          InputStream in,
          int folderId
  )
          throws IOException, ZimbraException
  {
    MailItem message;
    try
    {
      DeliveryOptions opts = new DeliveryOptions();
      opts.setFolderId(folderId);
      opts.setDraftInfo(null);
      message = mMbox.addMessage(
              octxt.getOperationContext(),
              in,
              0L,
              null,
              opts,
              null
      );
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }

    return new Message(message);
  }

  @Nonnull
  public Message saveDraft(@Nonnull OperationContext octxt,@Nonnull ParsedMessage parsedMessage, int id)
          throws IOException, ZimbraException
  {
    try
    {
      return new Message(mMbox.saveDraft(octxt.getOperationContext(), parsedMessage.toZimbra(com.zimbra.cs.mime.ParsedMessage.class), id));
    }
    catch( ServiceException e )
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  @Nonnull
  public Contact getContactById(OperationContext octxt,int id)
  {
    try
    {
      return new Contact(mMbox.getContactById(octxt.getOperationContext(),id));
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  @Nonnull
  public List<Contact> getContacts(OperationContext octxt, int folderId)
  {
    List<Contact> contacts = new ArrayList<>();
    try
    {
      List<com.zimbra.cs.mailbox.Contact> contactList = mMbox.getContactList(octxt.getOperationContext(), folderId);
      for( com.zimbra.cs.mailbox.Contact contact : contactList )
      {
        contacts.add(new Contact(contact));
      }
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }

    return contacts;
  }

  @Nonnull
  public Contact createContact(OperationContext octxt, ParsedContact pc, int folderId)
  {
    return createContact(octxt, pc, folderId, Collections.emptyList());
  }

  @Nonnull
  public Contact createContact(@Nonnull OperationContext octxt, @Nonnull ParsedContact pc, int folderId, @Nonnull Collection<String> tags)
          throws ZimbraException
  {
    MailItem contact;
    try
    {
      contact = mMbox.createContact(
              octxt.getOperationContext(),
              pc.toZimbra(com.zimbra.cs.mime.ParsedContact.class),
              folderId,
              tags.toArray(new String[tags.size()])
      );
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }

    return new Contact(contact);
  }
  public int addInvite(
          @Nonnull OperationContext octxt, @Nonnull Invite inv,
          int folderId, @Nullable ParsedMessage pm,
          boolean preserveExistingAlarms,
          boolean discardExistingInvites,
          boolean addRevision
  )
          throws ZimbraException
  {
    try
    {
      com.zimbra.cs.mime.ParsedMessage parsedMessage = null;
      if (pm != null)
      {
        parsedMessage = pm.toZimbra(com.zimbra.cs.mime.ParsedMessage.class);
      }
      return mMbox.addInvite(
              octxt.getOperationContext(),
              inv.toZimbra(com.zimbra.cs.mailbox.calendar.Invite.class),
              folderId, parsedMessage,
              preserveExistingAlarms,
              discardExistingInvites,
              addRevision
      ).calItemId;
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public void addInvite(@Nonnull OperationContext octxt, @Nonnull Invite inv, int folderId)
          throws ZimbraException
  {
    try
    {
      com.zimbra.cs.mime.ParsedMessage parsedMessage = null;
      mMbox.addInvite(
        octxt.getOperationContext(),
        inv.toZimbra(com.zimbra.cs.mailbox.calendar.Invite.class),
        folderId,
        null,
        true,
        false,
        true
      );
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public void addInvite(@Nonnull OperationContext octxt, @Nonnull Invite inv, int folderId, @Nullable MimeMessage mimeMessage)
    throws ZimbraException
  {
    try
    {
      com.zimbra.cs.mime.ParsedMessage parsedMessage = null;
      mMbox.addInvite(
        octxt.getOperationContext(),
        inv.toZimbra(com.zimbra.cs.mailbox.calendar.Invite.class),
        folderId,
        mimeMessage != null ? new com.zimbra.cs.mime.ParsedMessage(mimeMessage, false) : null,
        true,
        true,
        true
      );
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  @Nonnull
  public Mountpoint createMountpoint(
          @Nonnull OperationContext octxt, int folderId,
          String name, String ownerId,
          int remoteId, String remoteUuid,
          byte view, int flags,
          @Nonnull Item.Color color
  )
          throws ZimbraException
  {
    MailItem mountPoint;
    try
    {
      mountPoint = mMbox.createMountpoint(
              octxt.getOperationContext(), folderId,
              name, ownerId,
              remoteId,
              remoteUuid,
              Item.convertType(view),
              flags,
              color.toZimbra(com.zimbra.common.mailbox.Color.class),
              false
      );
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }

    return new Mountpoint(mountPoint);
  }

  public void beginTransaction(String name, @Nonnull OperationContext context)
  {
    beginTransaction(name, context.getOperationContext());
  }

  private void beginTransaction(String name, com.zimbra.cs.mailbox.OperationContext zContext)
          throws ZimbraException
  {
    try
    {
      mMbox.beginTransaction(name, zContext);
    }
    catch (Exception ex)
    {
      throw ExceptionWrapper.wrap(ex);
    }
  }

  public final void endTransaction(boolean success)
          throws ZimbraException
  {
    try
    {
      mMbox.endTransaction(success);
    }
    catch (Exception ex)
    {
      throw ExceptionWrapper.wrap(ex);
    }
  }

  /*
   * Warning: unsynchronized private access to mailbox
   */
  @Nullable
  private Item rawGetItem(@Nonnull Item.UnderlyingData data)
          throws InternalServerException
  {
    try
    {
      MailItem item = mMbox.getItem(data.toZimbra(MailItem.UnderlyingData.class));
      if(item != null)
      {
        return new Item(item);
      }
      else
      {
        return null;
      }
    }
    catch (Throwable ex)
    {
      throw ExceptionWrapper.wrap(new RuntimeException(ex));
    }
  }

  /*
   * Warning: unsynchronized private access to mailbox
   */
  @Nullable
  private List<com.zimbra.cs.mailbox.Flag> getAllFlags()
          throws ZimbraException
  {
    try {
      return com.zimbra.cs.mailbox.Flag.allOf(mMbox);
    } catch (ServiceException | RuntimeException e) {
      return null;
    }
  }

  public static boolean ACLIsEmpty(@Nullable Acl acl)
  {
    if (acl == null)
    {
      return true;
    }
    return acl.isEmpty();
  }

  /*
   * Warning: unsynchronized private access to mailbox
   */
  @Nullable
  private Map<Object, com.zimbra.cs.mailbox.Tag> getTagCache()
  {
    return mMbox.getTagCache();
  }

  /*
   * Warning: unsynchronized private access to mailbox
   */
  @Nullable
  private Collection<Folder> getFolderCache()
  {
    try
    {
      Collection<com.zimbra.cs.mailbox.Folder> folders = mMbox.getCacheFolders();
      ArrayList<Folder> newList = new ArrayList<>(folders.size());

      for (com.zimbra.cs.mailbox.Folder folder : folders)
      {
        if ( folder != null)
        {
          newList.add(new Folder(folder));
        }
      }

      return newList;
    }
    catch (Throwable ex)
    {
      ZimbraLog.mailbox.error("Exception: " + Utils.exceptionToString(ex));
      return null;
    }
  }

  @Nullable
  public static Mailbox getByAccount(@Nonnull Account account)
          throws ZimbraException
  {
    return getByAccount(account, true);
  }

  @Nullable
  @Deprecated
  public static Mailbox getByAccount(@Nonnull Account account, boolean autocreate)
          throws ZimbraException
  {
    com.zimbra.cs.mailbox.Mailbox mbox;
    try
    {
      mbox = com.zimbra.cs.mailbox.MailboxManager.getInstance().getMailboxByAccount(
              account.toZimbra(com.zimbra.cs.account.Account.class)
      );
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
    if (mbox != null)
    {
      return new Mailbox(mbox);
    }
    return null;
  }

  @Nullable
  @Deprecated
  public static Mailbox getById(long mboxId)
          throws ZimbraException
  {
    return getById((int) mboxId);
  }

  @Nullable
  @Deprecated
  public static Mailbox getById(int mboxId)
          throws ZimbraException
  {
    com.zimbra.cs.mailbox.Mailbox mbox;
    try
    {
      mbox = com.zimbra.cs.mailbox.MailboxManager.getInstance().getMailboxById(mboxId);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
    if (mbox != null)
    {
      return new Mailbox(mbox);
    }
    return null;
  }

  @Nonnull
  @Deprecated
  public static Mailbox getByItem(@Nonnull Item item)
  {
    return item.getMailbox();
  }

  @Deprecated
  public static Map<String, Integer> getMapAccountsAndMailboxes(@Nonnull Connection conn)
          throws ZimbraException
  {
    Map<String, Integer> accountsAndMailboxes;
    try
    {
      accountsAndMailboxes = DbMailbox.listMailboxes(conn.toZimbra(DbPool.DbConnection.class));
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }

    return accountsAndMailboxes;
  }

  @Nonnull
  public Connection getOperationConnection()
          throws ZimbraException
  {
    DbPool.DbConnection connection;
    try
    {
      connection = mMbox.getOperationConnection();
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
    return new ZimbraConnectionWrapper(connection);
  }

  public int getSchemaGroupId()
  {
    //leave the cast
    return mMbox.getSchemaGroupId();
  }

  public String rawGetConfig(
          @Nonnull String key
  )
          throws SQLException, ZimbraException
  {
    String query = "SELECT metadata FROM zimbra.mailbox_metadata WHERE mailbox_id=? AND section=? LIMIT 1";
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try
    {
      connection = ZimbraDatabase.legacyGetConnection();
      statement = connection.prepareStatement(query);
      statement.setInt(1, getId());
      statement.setString(2, key);

      resultSet = statement.executeQuery();
      if (resultSet.next())
      {
        return resultSet.getString(1);
      }
      else
      {
        return "";
      }
    }
    finally
    {
      DbUtils.closeQuietly(resultSet);
      DbUtils.closeQuietly(statement);
      if (connection != null)
      {
        connection.close();
      }
    }
  }

  public static final long MAX_METADATA_SIZE = 16777215;

  public void rawSetConfig(
          @Nonnull String section,
          @Nullable String metadata
  )
          throws SQLException, ZimbraException
  {
    if (metadata != null && metadata.length() > MAX_METADATA_SIZE)
    {
      throw new SQLException("metadata is too big to be saved");
    }

    Connection connection = null;
    PreparedStatement replaceStatement = null;
    try
    {
      connection = ZimbraDatabase.legacyGetConnection();
      if (metadata != null) {
        String query = "REPLACE INTO zimbra.mailbox_metadata (mailbox_id,section,metadata) VALUES(?,?,?)";
        replaceStatement = connection.prepareStatement(query);
        replaceStatement.setInt(1, getId());
        replaceStatement.setString(2, section);
        replaceStatement.setString(3, metadata);
      } else {
        String query = "DELETE FROM zimbra.mailbox_metadata WHERE mailbox_id = ? AND section = ?";
        replaceStatement = connection.prepareStatement(query);
        replaceStatement.setInt(1, getId());
        replaceStatement.setString(2, section);
      }

      replaceStatement.executeUpdate();
      connection.commit();
      RedoLogProvider.getRedoLogProvider().getRedoLogManager().commit(new RawSetConfig(getId(), section, metadata));
    }
    finally
    {
      DbUtils.closeQuietly(replaceStatement);
      if (connection != null)
      {
        connection.close();
      }
    }
  }

  public void deleteMailbox()
  {
    try
    {
      mMbox.deleteMailbox();
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public void deleteMailboxButStore()
  {
    try
    {
      mMbox.deleteMailbox(DeleteBlobs.NEVER);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public void recalculateFolderAndTagCounts()
  {
    try
    {
      mMbox.recalculateFolderAndTagCounts();
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  @Nonnull
  public MailboxIndex getIndex()
  {
    return mIndex;
  }

  public void startReIndex()
  {
    try
    {
      mMbox.index.startReIndex();
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public void suspendIndexing()
  {
    mMbox.suspendIndexing();
  }

  public void resumeIndexing()
  {
    mMbox.resumeIndexing();
  }

  public boolean isReIndexInProgress()
  {
    return mMbox.index.isReIndexInProgress() || mMbox.index.isCompactIndexInProgress();
  }

  public short getIndexVolume() {
    return mMbox.getIndexVolume();
  }

  public void purgeImapDeleted(OperationContext operationContext) {
    try{
      mMbox.purgeImapDeleted(operationContext.getOperationContext());
    } catch( ServiceException e ) {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public void setFolderUrl(OperationContext operationContext, int folderId, String url) {
    try {
      mMbox.setFolderUrl(operationContext.getOperationContext(), folderId, url);
    } catch (ServiceException e) {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public void setActiveSyncDisabled(OperationContext octxt, int folderId, boolean disableActiveSync) {
    try {
      mMbox.setActiveSyncDisabled(octxt.getOperationContext(), folderId, disableActiveSync);
    } catch (ServiceException e) {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public void markMailboxDeleted() throws org.openzal.zal.exceptions.ServiceException {
    try {
      getMailbox().markMailboxDeleted();
    } catch (ServiceException e) {
      throw new org.openzal.zal.exceptions.ServiceException(e);
    }
  }
}