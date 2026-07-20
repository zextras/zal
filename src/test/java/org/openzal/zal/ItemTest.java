package org.openzal.zal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class ItemTest
{
  @Test
  public void reflection_initialization()
  {
    Item item = new Item(mock(com.zimbra.cs.mailbox.MailItem.class));
  }
}