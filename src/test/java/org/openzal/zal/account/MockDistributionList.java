package org.openzal.zal.account;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.DistributionList;
import com.zimbra.cs.account.Provisioning;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.mockito.Mockito;

public class MockDistributionList extends DistributionList
{
  private Set<String> mMembers;

  public MockDistributionList(String name, String id, Map<String, Object> attrs)
  {
    this(name, id, attrs, Mockito.mock(com.zimbra.cs.account.Provisioning.class));
  }

  public MockDistributionList(String name, String id, Map<String, Object> attrs, Provisioning prov)
  {
    super(name, id, attrs, prov);
    mMembers = new HashSet<String>();
  }

  public void addMembers(String[] members) throws ServiceException {
    for (String member: members)
    {
      mMembers.add(member);
    }
  }

  public void removeMembers(String[] members) throws ServiceException {
    for (String member: members)
    {
      mMembers.remove(member);
    }
  }

  public String[] getAllMembers() throws ServiceException {
    return mMembers.toArray(new String[0]);
  }

  public Set<String> getAllMembersSet() throws ServiceException {
    return mMembers;
  }

}
