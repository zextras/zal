package org.openzal.zal;

import org.openzal.zal.lib.ZimbraDatabase;

public final class MailboxGroupIdUtil {
    private MailboxGroupIdUtil() {
    }

    public static int getGroupId(Mailbox mailbox) {

        if (mailbox.getSchemaGroupId() != 0) {
            return mailbox.getSchemaGroupId();
        }

        return ZimbraDatabase.getMailboxGroupFromMailboxId(mailbox.getId());
    }
}
