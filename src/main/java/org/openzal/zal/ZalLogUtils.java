package org.openzal.zal;

import com.zimbra.common.util.Log;
import com.zimbra.common.util.Log.Level;
import com.zimbra.common.util.ZimbraLog;
import java.util.Arrays;
import java.util.List;

public class ZalLogUtils {

  public synchronized static void disableCarbonioLogs() {
    List<Log> logs = Arrays.asList(
        ZimbraLog.misc, ZimbraLog.net, ZimbraLog.index, ZimbraLog.search, ZimbraLog.searchstat, ZimbraLog.searchstats, ZimbraLog.elasticsearch,
        ZimbraLog.redolog, ZimbraLog.lmtp, ZimbraLog.smtp, ZimbraLog.nio, ZimbraLog.imap_client, ZimbraLog.imap, ZimbraLog.pop_client, ZimbraLog.pop,
        ZimbraLog.milter, ZimbraLog.mailbox, ZimbraLog.calendar, ZimbraLog.im, ZimbraLog.im_intercept, ZimbraLog.account, ZimbraLog.autoprov, ZimbraLog.gal,
        ZimbraLog.galconcurrency, ZimbraLog.ldap, ZimbraLog.acl, ZimbraLog.security, ZimbraLog.soap, ZimbraLog.test, ZimbraLog.sqltrace, ZimbraLog.dbconn,
        ZimbraLog.perf, ZimbraLog.cache, ZimbraLog.filter, ZimbraLog.session, ZimbraLog.backup, ZimbraLog.system, ZimbraLog.sync, ZimbraLog.synctrace,
        ZimbraLog.syncstate, ZimbraLog.wbxml, ZimbraLog.xsync, ZimbraLog.extensions, ZimbraLog.zimlet, ZimbraLog.doc, ZimbraLog.op, ZimbraLog.dav, ZimbraLog.io,
        ZimbraLog.datasource, ZimbraLog.rmgmt, ZimbraLog.webclient, ZimbraLog.scheduler, ZimbraLog.store, ZimbraLog.fb, ZimbraLog.purge, ZimbraLog.mailop,
        ZimbraLog.slogger, ZimbraLog.mbxmgr, ZimbraLog.tnef, ZimbraLog.nginxlookup, ZimbraLog.contact, ZimbraLog.share, ZimbraLog.activity, ZimbraLog.ews,
        ZimbraLog.smime, ZimbraLog.ephemeral, ZimbraLog.contactbackup, ZimbraLog.passwordreset, ZimbraLog.addresslist
    );
    logs.forEach( l -> l.setLevel(Level.error) );
  }

}
