package org.openzal.zal;

import org.openzal.zal.Mailbox;
import com.zimbra.cs.db.DbMailItem;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openzal.zal.lib.ZimbraDatabase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class ZimbraDatabaseIT
{
  @Test
  public void mail_item_table_name_doesnt_change()
  {
    final String zimbraTableName;
    zimbraTableName = DbMailItem.getMailItemTableName(
      32,
      false
    );

    Mailbox mbox = Mockito.mock(Mailbox.class);
    when( mbox.getSchemaGroupId() ).thenReturn(32);
    String zextrasTableName = ZimbraDatabase.getItemTableName(mbox);

    assertEquals(zimbraTableName,zextrasTableName);
  }

  @Test
  public void tombstone_table_name_doesnt_change()
  {
    final String zimbraTableName;

    zimbraTableName = DbMailItem.getTombstoneTableName(
      32,
      32
    );


    Mailbox mbox = Mockito.mock(Mailbox.class);
    when( mbox.getSchemaGroupId() ).thenReturn(32);
    String zextrasTableName = ZimbraDatabase.getTombstoneTable(mbox);

    assertEquals(zimbraTableName,zextrasTableName);
  }

  @Test
  public void revision_table_name_doesnt_change()
  {
    final String zimbraTableName;

    zimbraTableName = DbMailItem.getRevisionTableName(
      32,
      false
    );

    Mailbox mbox = Mockito.mock(Mailbox.class);
    when( mbox.getSchemaGroupId() ).thenReturn(32);
    String zextrasTableName = ZimbraDatabase.getRevisionTableName(mbox);

    assertEquals(zimbraTableName,zextrasTableName);
  }

}
