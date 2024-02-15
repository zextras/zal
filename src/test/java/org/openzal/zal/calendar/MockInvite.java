package org.openzal.zal.calendar;

import org.mockito.Mockito;
import org.openzal.zal.Account;

public class MockInvite extends Invite {
    private String subject = "";

    public MockInvite() {
        super(Mockito.mock(com.zimbra.cs.mailbox.calendar.Invite.class));
    }

    @Override
    public String setCancelled(Account account) {
        this.subject = "cancel: " + subject;
        return this.subject;
    }

    public String setReply(Account account, String partStat) {
        return subject = partStat + ": " + subject;
    }

    public String getSubject() {
        return this.subject;
    }

}
