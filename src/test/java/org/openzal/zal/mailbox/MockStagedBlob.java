package org.openzal.zal.mailbox;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.StagedBlob;

public class MockStagedBlob extends StagedBlob
{
  public ZalMockBlob getMockBlob()
  {
    return mMockBlob;
  }

  private final ZalMockBlob mMockBlob;

  public MockStagedBlob(Mailbox mbox, ZalMockBlob mockBlob)
  {
    super(mbox, "xxx", 123);
    mMockBlob = mockBlob;
  }

  public String getStagedLocator()
  {
    return getLocator();
  }

  public String getLocator()
  {
    return "1";
  }
}
