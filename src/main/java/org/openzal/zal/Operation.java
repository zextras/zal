package org.openzal.zal;

public enum Operation implements Comparable<Operation>
{
  SKIP(),
  CHECKPOINT(),
  COMMIT_TXN(),
  ABORT_TXN(),
  ROLLOVER(),
  CREATE_MAILBOX(),
  DELETE_MAILBOX(),
  BACKUP_MAILBOX(),
  REINDEX_MAILBOX(),
  PURGE_OLD_MESSAGES(),
  CREATE_SAVED_SEARCH(),
  MODIFY_SAVED_SEARCH(),
  CREATE_TAG(),
  RENAME_TAG(),
  COLOR_ITEM(),
  INDEX_ITEM(),
  ALTER_ITEM_TAG(),
  SET_ITEM_TAGS(),
  MOVE_ITEM(),
  DELETE_ITEM(),
  COPY_ITEM(),
  CREATE_FOLDER_PATH(),
  RENAME_FOLDER_PATH(),
  EMPTY_FOLDER(),
  STORE_INCOMING_BLOB(),
  CREATE_MESSAGE(),
  SAVE_DRAFT(),
  SET_IMAP_UID(),
  CREATE_CONTACT(),
  MODIFY_CONTACT(),
  CREATE_NOTE(),
  EDIT_NOTE(),
  REPOSITION_NOTE(),
  CREATE_MOUNTPOINT(),
  MODIFY_INVITE_FLAG(),
  MODIFY_INVITE_PARTSTAT(),
  CREATE_VOLUME(),
  MODIFY_VOLUME(),
  DELETE_VOLUME(),
  SET_CURRENT_VOLUME(),
  MOVE_BLOBS(),
  CREATE_INVITE(),
  SET_CALENDAR_ITEM(),
  TRACK_SYNC(),
  SET_CONFIG(),
  GRANT_ACCESS(),
  REVOKE_ACCESS(),
  SET_FOLDER_URL(),
  SET_SUBSCRIPTION_DATA(),
  SET_PERMISSIONS(),
  SAVE_WIKI(),
  SAVE_DOCUMENT(),
  ADD_DOCUMENT_REVISION(),
  TRACK_IMAP(),
  IMAP_COPY_ITEM(),
  ICAL_REPLY(),
  CREATE_FOLDER(),
  RENAME_FOLDER(),
  FIX_CALENDAR_ITEM_TIME_ZONE(),
  RENAME_ITEM(),
  RENAME_ITEM_PATH(),
  CREATE_CHAT(),
  SAVE_CHAT(),
  PURGE_IMAP_DELETED(),
  DISMISS_CALENDAR_ITEM_ALARM(),
  FIX_CALENDAR_ITEM_END_TIME(),
  INDEX_DEFERRED_ITEMS(),
  RENAME_MAILBOX(),
  FIX_CALENDAR_ITEM_TZ(),
  DATE_ITEM(),
  SET_FOLDER_DEFAULT_VIEW(),
  SET_CUSTOM_DATA(),
  LOCK_ITEM(),
  UNLOCK_ITEM(),
  PURGE_REVISION(),
  DELETE_ITEM_FROM_DUMPSTER(),
  FIX_CALENDAR_ITEM_PRIORITY(),
  RECOVER_ITEM(),
  ENABLE_SHARED_REMINDER(),
  DOWNLOAD(),
  PREVIEW(),
  SNOOZE_CALENDAR_ITEM_ALARM(),
  CREATE_COMMENT(),
  CREATE_LINK(),
  SET_RETENTION_POLICY(),
  WATCH(),
  UNWATCH(),
  REFRESH_MOUNTPOINT(),
  EXPIRE_ACCESS(),
  SET_DISABLE_ACTIVE_SYNC(),
  SET_WEB_OFFLINE_SYNC_DAYS(),
  DELETE_CONFIG();

  Operation()
  {
  }

  public static Operation fromOperationCode(int code) {
    switch (code) {
      case 1:
        return CHECKPOINT;
      case 3:
        return COMMIT_TXN;
      case 4:
        return ABORT_TXN;
      case 6:
        return ROLLOVER;
      case 7:
        return CREATE_MAILBOX;
      case 8:
        return DELETE_MAILBOX;
      case 9:
        return BACKUP_MAILBOX;
      case 10:
        return REINDEX_MAILBOX;
      case 11:
        return PURGE_OLD_MESSAGES;
      case 12:
        return CREATE_SAVED_SEARCH;
      case 13:
        return MODIFY_SAVED_SEARCH;
      case 14:
        return CREATE_TAG;
      case 15:
        return RENAME_TAG;
      case 16:
        return COLOR_ITEM;
      case 17:
        return INDEX_ITEM;
      case 18:
        return ALTER_ITEM_TAG;
      case 19:
        return SET_ITEM_TAGS;
      case 20:
        return MOVE_ITEM;
      case 21:
        return DELETE_ITEM;
      case 22:
        return COPY_ITEM;
      case 23:
        return CREATE_FOLDER_PATH;
      case 24:
        return RENAME_FOLDER_PATH;
      case 25:
        return EMPTY_FOLDER;
      case 26:
        return STORE_INCOMING_BLOB;
      case 27:
        return CREATE_MESSAGE;
      case 28:
        return SAVE_DRAFT;
      case 29:
        return SET_IMAP_UID;
      case 30:
        return CREATE_CONTACT;
      case 31:
        return MODIFY_CONTACT;
      case 32:
        return CREATE_NOTE;
      case 33:
        return EDIT_NOTE;
      case 34:
        return REPOSITION_NOTE;
      case 35:
        return CREATE_MOUNTPOINT;
      case 36:
        return MODIFY_INVITE_FLAG;
      case 37:
        return MODIFY_INVITE_PARTSTAT;
      case 38:
        return CREATE_VOLUME;
      case 39:
        return MODIFY_VOLUME;
      case 40:
        return DELETE_VOLUME;
      case 41:
        return SET_CURRENT_VOLUME;
      case 42:
        return MOVE_BLOBS;
      case 43:
        return CREATE_INVITE;
      case 44:
        return SET_CALENDAR_ITEM;
      case 45:
        return TRACK_SYNC;
      case 46:
        return SET_CONFIG;
      case 47:
        return GRANT_ACCESS;
      case 48:
        return REVOKE_ACCESS;
      case 49:
        return SET_FOLDER_URL;
      case 50:
        return SET_SUBSCRIPTION_DATA;
      case 51:
        return SET_PERMISSIONS;
      case 52:
        return SAVE_WIKI;
      case 53:
        return SAVE_DOCUMENT;
      case 54:
        return ADD_DOCUMENT_REVISION;
      case 55:
        return TRACK_IMAP;
      case 56:
        return IMAP_COPY_ITEM;
      case 57:
        return ICAL_REPLY;
      case 58:
        return CREATE_FOLDER;
      case 59:
        return RENAME_FOLDER;
      case 60:
        return FIX_CALENDAR_ITEM_TIME_ZONE;
      case 61:
        return RENAME_ITEM;
      case 62:
        return RENAME_ITEM_PATH;
      case 63:
        return CREATE_CHAT;
      case 64:
        return SAVE_CHAT;
      case 65:
        return PURGE_IMAP_DELETED;
      case 66:
        return DISMISS_CALENDAR_ITEM_ALARM;
      case 67:
        return FIX_CALENDAR_ITEM_END_TIME;
      case 68:
        return INDEX_DEFERRED_ITEMS;
      case 69:
        return RENAME_MAILBOX;
      case 70:
        return FIX_CALENDAR_ITEM_TZ;
      case 71:
        return DATE_ITEM;
      case 72:
        return SET_FOLDER_DEFAULT_VIEW;
      case 73:
        return SET_CUSTOM_DATA;
      case 74:
        return LOCK_ITEM;
      case 75:
        return UNLOCK_ITEM;
      case 76:
        return PURGE_REVISION;
      case 77:
        return DELETE_ITEM_FROM_DUMPSTER;
      case 78:
        return FIX_CALENDAR_ITEM_PRIORITY;
      case 79:
        return RECOVER_ITEM;
      case 80:
        return ENABLE_SHARED_REMINDER;
      case 81:
        return DOWNLOAD;
      case 82:
        return PREVIEW;
      case 83:
        return SNOOZE_CALENDAR_ITEM_ALARM;
      case 84:
        return CREATE_COMMENT;
      case 85:
        return CREATE_LINK;
      case 86:
        return SET_RETENTION_POLICY;
      case 87:
        return WATCH;
      case 88:
        return UNWATCH;
      case 89:
        return REFRESH_MOUNTPOINT;
      case 90:
        return EXPIRE_ACCESS;
      case 91:
        return SET_DISABLE_ACTIVE_SYNC;
      case 92:
        return SET_WEB_OFFLINE_SYNC_DAYS;
      case 93:
        return DELETE_CONFIG;
      case 0:
      default:
        return SKIP;
    }
  }
}
