package org.openzal.zal;

import java.util.*;

import javax.annotation.Nonnull;
import org.openzal.zal.exceptions.ZimbraException;
import com.zimbra.common.service.ServiceException;

/* $if ZimbraVersion >= 8.0.6 $*/
/* $else$
 import com.zimbra.common.account.Key.GranteeBy;
/* $endif$ */

import org.mockito.Mockito;

public class ProvisioningSimulator extends ProvisioningImp
{
  static class ServerSimulator extends Server{
    private String mName;

    public ServerSimulator(
      String name,
      String id,
      Map<String, Object> attrs,
      Map<String, Object> defaults,
      @Nonnull Provisioning prov
    )
    {
      super(
        new com.zimbra.cs.account.Server(
          name,
          id,
          attrs,
          defaults,
          prov.toZimbra(com.zimbra.cs.account.Provisioning.class)
        )
      );
      mName = name;
    }

    @Override
    public String getServerHostname()
    {
      return mName;
    }
  }

  private Map<String, Account> mAccountIdMap;
  private Map<String, Domain> mDomainMap;
  private Map<String, Account> mAccountMap;
  private Map<String, DistributionList> mDistributionListMap;
    private Map<String,Server> mServers;

  /*************** Simulator methods ********************/
  public ProvisioningSimulator()
  {
    super(null);
    mDomainMap = new HashMap<String, Domain>();
    mAccountMap = new HashMap<String, Account>();
    mAccountIdMap = new HashMap<String, Account>();
    mDistributionListMap = new HashMap<String, DistributionList>();
    mServers = new HashMap<String, Server>();
    addServer("localhost");
  }

  public Server addServer(final String name)
  {
    com.zimbra.cs.account.Provisioning provisioning = Mockito.mock(com.zimbra.cs.account.Provisioning.class);
    Server server = new ServerSimulator(
      name,"666",
      new HashMap<String, Object>(){{put(ProvisioningImp.A_zimbraServiceHostname, name);}},
      Collections.EMPTY_MAP,
      new ProvisioningImp(provisioning));
    mServers.put(name,server);
    return server;
  }

  public void addDomain(String domain)
  {
    if (mDomainMap.containsKey(domain))
    {
      return;
    }
    mDomainMap.put(domain, createFakeDomain(domain));
  }

  public List<DistributionList> getDistributionLists(Account userAccount) throws ServiceException
  {
    List<DistributionList> distributionListsWithInTargetUser = new ArrayList<DistributionList>();
    Collection<DistributionList>
                                                distributionLists =
      mDistributionListMap
    .values();
    for (DistributionList distributionList : distributionLists){
      Collection<String> distributionListMembers = distributionList
        .getAllMembersSet();
      Collection<String> userAccountAllAddresses = userAccount
        .getAllAddresses();
      for( String userAddress : userAccountAllAddresses){
        if(distributionListMembers.contains(userAddress)){
          distributionListsWithInTargetUser.add(distributionList);
          break;
        }
      }
    }
    return distributionListsWithInTargetUser;
  }

  public Account addUser(String address, String displayName)
  {
    return addUser(address, displayName, new HashMap<String, Object>());
  }

  public Account addUser(String address, String displayName, Map<String, Object> attrs)
  {
    if (mAccountMap.containsKey(address))
    {
      return mAccountMap.get(address);
    }

    int domainIdx = address.indexOf('@');
    if (domainIdx == -1)
    {
      throw new RuntimeException("Invalid address " + address);
    }

    String domain = address.substring(domainIdx + 1);
    addDomain(domain);

    Account account = createFakeAccount(address, displayName, domain, attrs);
    mAccountMap.put(address, account);
    mAccountIdMap.put(account.getId(), account);

    return account;
  }

  public Domain createFakeDomain(String domain)
  {
    Map<String,Object> attrs = new HashMap<String, Object>();
    Map<String, Object> defaults = new HashMap<String, Object>();
    attrs.put("zimbraPublicServiceHostname",domain);

    return new Domain(domain,domain,attrs,defaults,this);
  }

  public Account createFakeAccount(String accountStr)
  {
    return createFakeAccount(accountStr, accountStr);
  }

  public Account createFakeAccount(String address, String displayName)
  {
    return addUser(address,displayName,Collections.<String,Object>emptyMap());
  }

  public Account createFakeAccount(String address, String displayName,String domain,Map<String,Object> extraAttr)
  {
    if( address == null ) {
      address = "mockito@example.com";
    }

    if( displayName == null ) {
      displayName = "mockito";
    }

    Map<String,Object> attrs = new HashMap<String, Object>();

    String id = UUID.randomUUID().toString();
    attrs.put(com.zimbra.cs.account.Provisioning.A_mail, displayName);

    Map<String, Object> defaults = new HashMap<String, Object>();
    defaults.put(com.zimbra.cs.account.Provisioning.A_zimbraAccountStatus,
                 com.zimbra.cs.account.Provisioning.ACCOUNT_STATUS_ACTIVE);
    defaults.put(com.zimbra.cs.account.Provisioning.A_displayName, displayName );
    defaults.put(com.zimbra.cs.account.Provisioning.A_zimbraPrefTimeZoneId, "UTC");
    defaults.put(com.zimbra.cs.account.Provisioning.A_zimbraId, id);
    defaults.put(com.zimbra.cs.account.Provisioning.A_zimbraDomainId, domain);
    attrs.putAll(extraAttr);
    attrs.putAll(defaults);

    defaults.put(ProvisioningImp.A_zimbraMailHost, "localhost");

    return new AccountSimulator(
      address,
      id,
      attrs,
      defaults,
      this
    );
  }

  public Account getAccountByName(String accountStr)
  {
    return mAccountMap.get(accountStr);
  }

  public Account getAccountById(String zimbraId)
  {
    return mAccountIdMap.get(zimbraId);
  }

  public Domain getDomainByName( String domain )
  {
    return mDomainMap.get(domain);
  }

  public boolean onLocalServer(Account userAccount)
  {
    return userAccount.getMailHost().equals("localhost");
  }

  @Override
  public <T> T toZimbra(@Nonnull Class<T> cls)
  {
    return cls.cast(Mockito.mock(com.zimbra.cs.account.Provisioning.class));
  }

  /******************************************************/

  public Account getZimbraUser()
    throws ZimbraException {
    throw new RuntimeException("Provisioning method not implemented.");
  }

  public OperationContext createZContext()
    throws ZimbraException {
    throw new RuntimeException("Provisioning method not implemented");
  }

  public DistributionList getDistributionListById(String id)
  {
    return mDistributionListMap.get(id);
  }

  public DistributionList getDistributionListByName(String name)
  {
    return mDistributionListMap.get(name);
  }

  public Server getLocalServer()
      throws ZimbraException
  {
    HashMap<String, Object> attrs = new HashMap<String, Object>();
    attrs.put(ProvisioningImp.A_zimbraServiceHostname, "localhost");

    Server server = mServers.get("localhost");
    if (server == null)
    {
      server = addServer("localhost");
    }
    server.modify(attrs);
    return server;
  }

  public List<Domain> getAllDomains()
      throws ZimbraException
  {
    throw new RuntimeException("Provisioning method not implemented");
  }

  public Domain getDomainById(String domainId)
      throws ZimbraException
  {
    return mDomainMap.get(domainId);
  }

  public List<DistributionList> getAllDistributionLists(Domain domain)
    throws ZimbraException
  {
    return Collections.emptyList();
  }

  public Cos getCosById(String cosId)
    throws ZimbraException
  {
    throw new RuntimeException("Provisioning method not implemented");
  }

  public List<Cos> getAllCos()
      throws ZimbraException
  {
    return Collections.emptyList();
  }


  public Cos getCosByName(String cosStr)
      throws ZimbraException
  {
    throw new RuntimeException("Provisioning method not implemented");
  }


  public List<Account> getAllAccounts(Domain domain)
      throws ZimbraException
  {
    throw new RuntimeException("Provisioning method not implemented");
  }

  public List<Server> getAllServers()
      throws ZimbraException
  {
    return new ArrayList<>(mServers.values());
  }

  public List<Server> getAllServers(String service)
    throws ZimbraException
  {
    return new ArrayList<>(mServers.values());
  }

  public List<CalendarResource> getAllCalendarResources(Domain domain)
      throws ZimbraException
  {
    throw new RuntimeException("Provisioning method not implemented");
  }

  public Config getConfig()
      throws ZimbraException
  {
    return new Config(
      new HashMap<String, Object>(){{
        put("key", "value");
      }},
      new ProvisioningImp(mProvisioning)
    );
  }

  public CalendarResource getCalendarResourceByName(String resourceName)
      throws ZimbraException
  {
    throw new RuntimeException("Provisioning method not implemented");
  }

  public CalendarResource getCalendarResourceById(String resourceId)
      throws ZimbraException
  {
    throw new RuntimeException("Provisioning method not implemented");
  }

  public Domain createDomain(String currentDomainName, Map<String, Object> stringObjectMap)
      throws ZimbraException
  {
    throw new RuntimeException("Provisioning method not implemented");
  }

  public Cos createCos(String cosname, Map<String, Object> stringObjectMap)
      throws ZimbraException
  {
    throw new RuntimeException("Provisioning method not implemented");
  }

  public DistributionList createDistributionList(String dlistName, Map<String, Object> stringObjectMap)
      throws ZimbraException
  {
    throw new RuntimeException("Provisioning method not implemented");
  }

  public CalendarResource createCalendarResource(String dstAccount, String newPassword, Map<String, Object> attrs)
      throws ZimbraException
  {
    throw new RuntimeException("Provisioning method not implemented");
  }

  public Account createAccount(String dstAccount, String newPassword, Map<String, Object> attrs)
      throws ZimbraException
  {
    throw new RuntimeException("Provisioning method not implemented");
  }

  public Account createFakeAccount(Map<String, Object> attrs)
    throws ZimbraException
  {
    throw new RuntimeException("Provisioning method not implemented");
  }

  public Server createServer(String name, Map<String, Object> attrs)
          throws ZimbraException
  {
    throw new RuntimeException("Provisioning method not implemented");
  }

  public void modifyIdentity(Account newAccount, String identityName, Map<String, Object> newAttrs)
      throws ZimbraException
  {
    throw new RuntimeException("Provisioning method not implemented");
  }

  public Domain getDomain(Account account)
    throws ZimbraException
  {
    return mDomainMap.get(account.getDomainName());
  }

  public CountAccountResult countAccount(Domain domain)
    throws ZimbraException
  {
    throw new RuntimeException("Provisioning method not implemented");
  }


  public Server getServer(Account acct)
    throws ZimbraException
  {
    throw new RuntimeException("Provisioning method not implemented");
  }

  public Server getServerById(String id)
    throws ZimbraException
  {
    throw new RuntimeException("Provisioning method not implemented");
  }

  public Server getServerByName(String name)
    throws ZimbraException
  {
    return mServers.get(name);
  }

  @Override
  public Locale getLocale(Entry entry){
    return Locale.getDefault();
  }

  /******************************************************/
  public static class AccountSimulator extends Account
  {
    private static Provisioning mProvisioning;

    public AccountSimulator(String address,
                            String id,
                            Map<String, Object> attrs,
                            Map<String, Object> defaults,
                            Provisioning prov)
    {
      super(address, id, attrs, defaults, prov);
      mProvisioning = prov;
    }

    @Override
    public boolean isLocalAccount()
    {
      return "localhost".equals(getAttr(ProvisioningImp.A_zimbraMailHost));
    }

   @Nonnull
   public List<DistributionList> getDistributionLists(boolean directOnly, Map<String, String> via)
   {
     try
     {
       return ((ProvisioningSimulator) mProvisioning).getDistributionLists //TODO
         (this
         );
     }
     catch (ServiceException e)
     {
       e.printStackTrace();
     }
     return  null;
   }

    @Override
    public String getDisplayName()
    {
      String displayName = getAttr("displayName");
      if (displayName == null || displayName.isEmpty())
      {
        return getName();
      }

      return displayName;
    }

    @Override
    public String getAccountStatus(Provisioning prov)
    {
      return "test status";
    }
  }

}
