package org.openzal.zal;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Soner
 */
public class MailboxGroupIdUtilTest {

  @Test
  public void test_when_groupId_does_not_exists_in_mail_box_then_return_localConfigEvaluated_value() {
    com.zimbra.cs.mailbox.Mailbox carbonioMailbox = Mockito.mock(com.zimbra.cs.mailbox.Mailbox.class);
    Mockito.when(carbonioMailbox.getId()).thenReturn(117);
    Mailbox mailbox = new Mailbox(carbonioMailbox);
    Assert.assertEquals(17, MailboxGroupIdUtil.getGroupId(mailbox));
  }

  @Test
  public void test_when_groupId__exists_then_return_mailbox_group_id() {
    com.zimbra.cs.mailbox.Mailbox carbonioMailbox = Mockito.mock(com.zimbra.cs.mailbox.Mailbox.class);
    Mockito.when(carbonioMailbox.getId()).thenReturn(117);
    Mockito.when(carbonioMailbox.getSchemaGroupId()).thenReturn(35);
    Mailbox mailbox = new Mailbox(carbonioMailbox);
    Assert.assertEquals(35, MailboxGroupIdUtil.getGroupId(mailbox));
  }

  @Test
  public void test_default_value_of_zimbra_mailbox_groups() {
    Assert.assertEquals(100, LocalConfig.getInt(LocalConfig.zimbra_mailbox_groups));
  }
}
