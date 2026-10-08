/*
 * ZAL - Zextras Abstraction Layer.
 * Copyright (C) 2023 ZeXtras S.r.l.
 *
 * This file is part of ZAL.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZAL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.openzal.zal;

import com.zimbra.common.calendar.ICalTimeZone;
import com.zimbra.common.util.Constants;
import com.zimbra.cs.datasource.DataSourceManager;
import com.zimbra.cs.mailbox.calendar.Util;

import org.openzal.zal.calendar.ICalendarTimezone;
import org.openzal.zal.exceptions.*;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.util.AccountUtil;
import com.zimbra.cs.account.accesscontrol.ACLAccessManager;
import com.zimbra.cs.account.accesscontrol.Right;
import com.zimbra.cs.account.accesscontrol.generated.UserRights;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.openzal.zal.extension.ConfigZimletStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Account extends Entry
{

    @Nonnull private final com.zimbra.cs.account.Account mAccount;

  public Account(@Nonnull Object account)
  {
    super(account);
    mAccount = (com.zimbra.cs.account.Account) account;
  }

  public Account(
    String accountName,
    String accountId,
    Map<String, Object> accountAttrs,
    Map emptyMap,
    @Nonnull Provisioning provisioning
  )
  {
    this(
      new com.zimbra.cs.account.Account(
        accountName,
        accountId,
        accountAttrs,
        emptyMap,
        provisioning.toZimbra(com.zimbra.cs.account.Provisioning.class)
      )
    );
  }


  public String getLastLogonTime()
  {
    try
    {
      return mAccount.getLastLogonTimestampAsString();
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public Boolean isMailAllowReceiveButNotSendWhenOverQuota() {
    return mAccount.isMailAllowReceiveButNotSendWhenOverQuota();
  }

  public void setLastLogonTime(String time)
  {
    try
    {
      mAccount.setLastLogonTimestampAsString(time);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public boolean isFeatureMobilePolicyEnabled()
  {
    return mAccount.isFeatureMobilePolicyEnabled();
  }

  public String getPrefOutOfOfficeReply()
  {
    return mAccount.getPrefOutOfOfficeReply();
  }

  public String getCOSId()
  {
    return mAccount.getCOSId();
  }

  public void addAlias(String alias)
    throws AlreadyExistsException
  {
    try
    {
      mAccount.addAlias(alias);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public String getSignatureId()
  {
    return mAccount.getSignatureId();
  }

  public boolean isIsDelegatedAdminAccount()
  {
    return mAccount.isIsDelegatedAdminAccount();
  }

  public String getMailHost()
  {
    return mAccount.getMailHost();
  }

  public List<DataSource> getAllDataSources()
    throws NoSuchAccountException
  {
    try
    {
      return ZimbraListWrapper.wrapDataSources(mAccount.getAllDataSources());
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  /**
   * Returns every zimlet available for the user.
   * Attribute "zimbraZimletAvailableZimlets" returns a list of the available zimlets with an extra character
   * that indicates whether the zimlet is mandatory, enabled or disabled for the user (based on the config):
   * +: this zimlet is enabled for the user, therefore he/she is able to hide it through Preferences
   * -: this zimlet is disabled for the user
   * !: this zimlet is mandatory for the user, therefore he/she won't be able to hide it
   * {@link ConfigZimletStatus} represents these three states.
   * N.B. "zimbraZimletAvailableZimlets" attribute does not provide information on whether the user hid a specific
   * zimlet from his/her Preferences. To assert this, use {@link #getUserPrefHiddenZimlets()} or
   * {@link #getUserPrefZimlets()} based on what you need to achieve
   * @return a {@link Map} with zimlet names as keys and {@link ConfigZimletStatus} as values
   */
  public Map<String, ConfigZimletStatus> getUserAvailableZimlets()
  {
    String[] zimlets = mAccount.getMultiAttr("zimbraZimletAvailableZimlets");
    Map<String, ConfigZimletStatus> toReturn = new HashMap<>();
    for (String zimletName : zimlets)
    {
      switch (zimletName.charAt(0))
      {
        case '!':
          toReturn.put(zimletName.substring(1), ConfigZimletStatus.Mandatory);
          break;

        case '+':
          toReturn.put(zimletName.substring(1), ConfigZimletStatus.Enabled);
          break;

        case '-':
          toReturn.put(zimletName.substring(1), ConfigZimletStatus.Disabled);
          break;
      }
    }
    return toReturn;
  }

    public boolean isIsExternalVirtualAccount()
  {
    return mAccount.isIsExternalVirtualAccount();
  }

  public boolean isMobileSmartForwardRFC822Enabled()
  {
    return mAccount.isMobileSmartForwardRFC822Enabled();
  }

  public void setPrefAllowAddressForDelegatedSender(@Nonnull Collection<String> addresses)
    throws ZimbraException
  {
    try
    {
      mAccount.setPrefAllowAddressForDelegatedSender(addresses.toArray(new String[addresses.size()]));
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public List<Identity> getAllIdentities() throws NoSuchAccountException
  {
    try
    {
       return ZimbraListWrapper.wrapIdentities(mAccount.getAllIdentities());
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  @Nonnull
  public Collection<String> getMultiAttr(String name)
  {
    return Arrays.asList(mAccount.getMultiAttr(name));
  }

  @Nonnull
  public Collection<String> getPrefAllowAddressForDelegatedSender()
  {
    return Arrays.asList(mAccount.getPrefAllowAddressForDelegatedSender());
  }

  public boolean isIsSystemResource()
  {
    return mAccount.isIsSystemResource();
  }

  public String getAttr(String name, String defaultValue)
  {
    return mAccount.getAttr(name, defaultValue);
  }

  public void modifyDataSource(String dataSourceId, Map<String, Object> attrs)
    throws NoSuchAccountException
  {
    try
    {
      mAccount.modifyDataSource(dataSourceId, attrs);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

    @Nullable
  public Identity getIdentityByName(String name) throws NoSuchAccountException
  {
    com.zimbra.cs.account.Identity identity;
    try
    {
      identity = mAccount.getIdentityByName(name);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }

    if ( identity == null )
    {
      return null;
    }

    return new Identity(identity);
  }

  @Nonnull
  public String getId()
  {
    return mAccount.getId();
  }

  public Boolean getBooleanAttr(String name, Boolean defaultValue)
  {
    return mAccount.getBooleanAttr(name, defaultValue);
  }

  public void setPrefOutOfOfficeReply(String zimbraPrefOutOfOfficeReply)
  {
    try
    {
      mAccount.setPrefOutOfOfficeReply(zimbraPrefOutOfOfficeReply);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public void setIsSystemAccount(boolean zimbraIsSystemAccount)
  {
    try
    {
      mAccount.setIsSystemAccount(zimbraIsSystemAccount);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public String getDomainId()
  {
    return mAccount.getDomainId();
  }

  public Date getPrefOutOfOfficeUntilDate()
  {
    return mAccount.getPrefOutOfOfficeUntilDate();
  }

  public String getDisplayName()
  {
    String displayName = mAccount.getDisplayName();
    if( displayName == null || displayName.isEmpty() )
    {
      return getName();
    }
    else
    {
      return displayName;
    }
  }

  public void unsetSignatureId()
  {
    try
    {
      mAccount.unsetSignatureId();
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

/**
 * @param via If not null, in it will set a Map<distributionList1,
 *            distributionList2> of distribution list which the user is a member
 *            (indirectly), where distributionList2 is in distributionList1,
 */
  @Nonnull
  public List<DistributionList> getDistributionLists(boolean directOnly, Map<String, String> via)
  {
    try
    {
      return ZimbraListWrapper.wrapDistributionLists(mAccount.getDistributionLists(directOnly, via));
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public boolean isPrefDeleteInviteOnReply()
  {
    return mAccount.isPrefDeleteInviteOnReply();
  }

  @Nonnull
  public Collection<String> getAliases()
  {
    return Arrays.asList(mAccount.getMailAlias());
  }

  /**
   * @param  provisioning Provisioning
   * @return Collection of all addresses of an Account obtained from account.getName() and account.getMailAlias()
   * properly combined with their relative domainAliases
   */
  @Nonnull
  public Collection<String> getAllAddressesIncludeDomainAliases(Provisioning provisioning)
  {
    Set<String> addresses = new HashSet<String>();
    Map<String,Collection<Domain>> domainCache = new HashMap<>();
    for (String address : getAllAddresses())
    {
      addresses.add(address);
      //will be fixed in devel(compatibility check)
      addresses.addAll(
        ((ProvisioningImp)provisioning).getWithDomainAliasesExpansion(address, domainCache)
      );
    }

    return addresses;
  }

  @Nonnull
  public Collection<String> getAllAddressesAllowedInFrom(Provisioning provisioning)
  {
    Set<String> addresses = new HashSet<String>();

    //add main address plus mail aliases combined with relative domain aliases
    addresses.addAll(getAllAddressesIncludeDomainAliases(provisioning));

    //add addresses obtained from account multi attribute "zimbraAllowFromAddress"
    addresses.addAll(getAllowFromAddress());

    Map<Right, Set<com.zimbra.cs.account.Entry>>  rights;
    try
    {
      rights = new ACLAccessManager().discoverUserRights(mAccount, new HashSet<Right>(){{add(UserRights.R_sendAs);add(UserRights.R_sendAsDistList);}}, false);
    }
    catch (Exception e)
    {
      return addresses;
    }
    if (rights.containsKey(UserRights.R_sendAs))
    {
      Set<com.zimbra.cs.account.Entry> allowed = rights.get(UserRights.R_sendAs);
      for (com.zimbra.cs.account.Entry entry : allowed)
      {
        if (entry instanceof com.zimbra.cs.account.Account)
        {
          addresses.add(((com.zimbra.cs.account.Account) entry).getName());
          addresses.addAll(Arrays.asList(((com.zimbra.cs.account.Account) entry).getPrefAllowAddressForDelegatedSender()));
        }
      }
    }
    if (rights.containsKey(UserRights.R_sendAsDistList))
    {
      Set<com.zimbra.cs.account.Entry> allowed = rights.get(UserRights.R_sendAsDistList);
      for (com.zimbra.cs.account.Entry entry : allowed)
      {
        if (entry instanceof com.zimbra.cs.account.DistributionList)
        {
          addresses.add(((com.zimbra.cs.account.DistributionList) entry).getName());
          addresses.addAll(Arrays.asList((((com.zimbra.cs.account.DistributionList)entry).getPrefAllowAddressForDelegatedSender())));
        }
      }
    }

    return addresses;
  }

  /**
   * @return the address of account's alias obtained from MultiAttribute "zimbraMailAlias" with the addition
   * of account.getName().
   */
  @Nonnull
  public List<String> getAllAddresses()
  {
    String[] alises = mAccount.getMailAlias();

    ArrayList<String> list = new ArrayList<String>(alises.length + 1);

    list.add(getName());
    list.addAll(Arrays.asList(alises));

    return list;
  }

  public void setPrefOutOfOfficeUntilDate(Date zimbraPrefOutOfOfficeUntilDate)
  {
    try
    {
      mAccount.setPrefOutOfOfficeUntilDate(zimbraPrefOutOfOfficeUntilDate);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

    @Nullable
  public Cos getCOS() throws NoSuchDomainException
  {
    com.zimbra.cs.account.Cos cos;
    try
    {
      cos = mAccount.getCOS();
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }

    if ( cos == null )
    {
      return null;
    }

    return new Cos(cos);
  }

  public String getAccountStatusAsString()
  {
    return mAccount.getAccountStatusAsString();
  }

  @Nonnull
  public List<Signature> getAllSignatures() throws NoSuchAccountException
  {
    try
    {
      return ZimbraListWrapper.wrapSignatures(mAccount.getAllSignatures());
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public boolean isMobilePolicyAllowPartialProvisioning()
  {
    return mAccount.isMobilePolicyAllowPartialProvisioning();
  }

    public boolean isIsAdminAccount()
  {
    return mAccount.isIsAdminAccount();
  }

  @Override
  public boolean equals(@Nullable Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }

   return getId().equals(
     ((Account) o).getId()
   );
  }

  @Override
  public int hashCode()
  {
    return mAccount.getId().hashCode();
  }

  public String getPrefOutOfOfficeExternalReply()
  {
    return mAccount.getPrefOutOfOfficeExternalReply();
  }

  public Date getPrefOutOfOfficeFromDate()
  {
    return mAccount.getPrefOutOfOfficeFromDate();
  }

  public boolean isIsSystemAccount()
  {
    return mAccount.isIsSystemAccount();
  }

  public String getUid()
  {
    return mAccount.getUid();
  }

  @Nullable
  public AccountStatus getAccountStatus()
  {
    ZAttrProvisioning.AccountStatus accountStatus = mAccount.getAccountStatus();

    if ( accountStatus == null )
    {
      return null;
    }

    return AccountStatus.of(accountStatus);
  }

  public void setAccountStatus(AccountStatus status)
  {
    try
    {
      mAccount.setAccountStatus(status.toZimbra(ZAttrProvisioning.AccountStatus.class));
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

    @Nonnull
  public Collection<String> getMailAlias()
  {
    return Arrays.asList(mAccount.getMailAlias());
  }

  @Nonnull
  public Signature createSignature(String signatureName, Map<String, Object> attrs)
    throws NoSuchAccountException
  {
    try
    {
      return new Signature(mAccount.createSignature(signatureName, attrs));
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public void setPrefOutOfOfficeExternalReply(String zimbraPrefOutOfOfficeExternalReply)
    throws ServiceException
  {
    mAccount.setPrefOutOfOfficeExternalReply(zimbraPrefOutOfOfficeExternalReply);
    mAccount.setPrefExternalSendersType(ZAttrProvisioning.PrefExternalSendersType.ALL);
  }

  public void setPrefOutOfOfficeExternalUnknownReply(String zimbraPrefOutOfOfficeExternalReply)
    throws ServiceException
  {
    mAccount.setPrefOutOfOfficeExternalReply(zimbraPrefOutOfOfficeExternalReply);
    mAccount.setPrefExternalSendersType(ZAttrProvisioning.PrefExternalSendersType.ALLNOTINAB);
  }

  @Nonnull
  public Collection<String> getAllowFromAddress()
  {
    return Arrays.asList(mAccount.getAllowFromAddress());
  }

  public String getPrefFromDisplay()
  {
    return mAccount.getPrefFromDisplay();
  }

    @Nonnull
  public Collection<String> getMailDeliveryAddress()
  {
    return Arrays.asList(mAccount.getMailDeliveryAddress());
  }

  public boolean isMobilePolicyAllowNonProvisionableDevices()
  {
    return mAccount.isMobilePolicyAllowNonProvisionableDevices();
  }

  public boolean isFeatureMobileSyncEnabled()
  {
    return mAccount.isFeatureMobileSyncEnabled();
  }

  public boolean isCarbonioFeatureOTPMgmtEnabled()
  {
    return mAccount.isCarbonioFeatureOTPMgmtEnabled();
  }

    public void deleteAccount() throws NoSuchAccountException
  {
    try
    {
      mAccount.deleteAccount();
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public boolean isPrefOutOfOfficeReplyEnabled()
  {
    return mAccount.isPrefOutOfOfficeReplyEnabled();
  }

  public String getAccountStatus(@Nonnull Provisioning prov)
  {
    return mAccount.getAccountStatus(
      prov.toZimbra(com.zimbra.cs.account.Provisioning.class)
    );
  }

  @Nullable
  public Signature getSignatureByName(String key)
  {
    com.zimbra.cs.account.Signature signature;
    try
    {
      signature = mAccount.getSignatureByName(key);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }

    if ( signature == null )
    {
      return null;
    }

    return new Signature(signature);
  }

  public String getName()
  {
    return mAccount.getName();
  }

  public void setPrefOutOfOfficeReplyEnabled(boolean zimbraPrefOutOfOfficeReplyEnabled)
  {
    try
    {
      mAccount.setPrefOutOfOfficeReplyEnabled(zimbraPrefOutOfOfficeReplyEnabled);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public void modifySignature(String signatureId, Map<String, Object> attrs)
    throws NoSuchAccountException, NoSuchSignatureException
  {
    try
    {
      mAccount.modifySignature(signatureId, attrs);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  @Nonnull
  public Map<String, Object> getAttrs()
  {
    return new HashMap<String, Object>(mAccount.getAttrs());
  }

  public String getPrefFromAddress()
  {
    return mAccount.getPrefFromAddress();
  }

    public void setPrefOutOfOfficeFromDate(Date zimbraPrefOutOfOfficeFromDate)
  {
    try
    {
      mAccount.setPrefOutOfOfficeFromDate(zimbraPrefOutOfOfficeFromDate);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  @Nonnull
  public Map<String, Object> getAttrs(boolean applyDefaults)
  {
    return new HashMap<String, Object>(mAccount.getAttrs(applyDefaults));
  }

  @Nullable
  public DataSource getDataSourceByName(String name) throws NoSuchAccountException
  {
    com.zimbra.cs.account.DataSource dataSource;
    try
    {
      dataSource = mAccount.getDataSourceByName(name);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }

    if ( dataSource == null )
    {
      return null;
    }

    return new DataSource(dataSource);
  }

  public String getAttr(String name)
  {
    return mAccount.getAttr(name);
  }

  public String getDomainName()
  {
    return mAccount.getDomainName();
  }

    @Nonnull
  public DataSource createDataSource(
    @Nonnull DataSourceType sourceType,
    String sourceName,
    Map<String, Object> attrs,
    boolean passwdAlreadyEncrypted
  )
    throws NoSuchAccountException, TooManyDataSourcesException, DataSourceExistsException
  {
    try
    {
      return new DataSource(
      mAccount.createDataSource(
        sourceType.toZimbra(com.zimbra.soap.admin.type.DataSourceType.class),
        sourceName,
        attrs,
        passwdAlreadyEncrypted)
      );
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public String getAttr(String name, boolean applyDefaults)
  {
    return mAccount.getAttr(name, applyDefaults);
  }

  public void setSignatureId(String signatureId)
  {
    try
    {
      mAccount.setSignatureId(signatureId);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public void setAllowFromAddress(@Nonnull Collection<String> zimbraAllowFromAddress)
  {
    try
    {
      mAccount.setAllowFromAddress(zimbraAllowFromAddress.toArray(new String[zimbraAllowFromAddress.size()]));
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public void unsetPrefOutOfOfficeDate()
    throws ServiceException
  {
    mAccount.unsetPrefOutOfOfficeUntilDate();
    mAccount.unsetPrefOutOfOfficeFromDate();
  }

  public void setPrefOutOfOfficeExternalReplyEnabled(boolean prefOutOfOfficeExternalReplyEnabled)
  {
    try
    {
      mAccount.setPrefOutOfOfficeExternalReplyEnabled(prefOutOfOfficeExternalReplyEnabled);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public boolean isPrefOutOfOfficeExternalKnownReplyEnabled()
  {
    return mAccount.isPrefOutOfOfficeExternalReplyEnabled();
  }

  public boolean isPrefOutOfOfficeExternalUnknownReplyEnabled()
  {
    return mAccount.isPrefOutOfOfficeExternalReplyEnabled() && mAccount.getPrefExternalSendersType() == ZAttrProvisioning.PrefExternalSendersType.ALLNOTINAB;
  }

    @Nonnull
  public Identity createIdentity(String identityName, Map<String, Object> attrs)
    throws NoSuchAccountException, TooManyIdentitiesException, IdentityExistsException
  {
    try
    {
      return new Identity(mAccount.createIdentity(identityName, attrs));
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public boolean isAllowAnyFromAddress()
  {
    return mAccount.isAllowAnyFromAddress();
  }

  public void setAllowAnyFromAddress(boolean zimbraAllowAnyFromAddress)
  {
    try
    {
      mAccount.setAllowAnyFromAddress(zimbraAllowAnyFromAddress);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public String getGivenName()
  {
    return mAccount.getGivenName();
  }

  public boolean isCalendarResource()
  {
    return mAccount.isCalendarResource();
  }

  @Nullable
  public String getMail()
  {
    return mAccount.getMail();
  }

  public String getCn()
  {
    return mAccount.getCn();
  }

    public <T> T toZimbra(@Nonnull Class<T> cls)
  {
    return cls.cast(mAccount);
  }

  public boolean addressMatchesAccount(String address)
  {
    try
    {
      return AccountUtil.addressMatchesAccount(mAccount, address);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public boolean hasAddress(String address)
  {
    try
    {
      return AccountUtil.addressMatchesAccount(mAccount, address);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public void setAttrs(Map<String, Object> attrs)
  {
    mAccount.setAttrs(attrs);
  }

    public String getPrefMailDefaultCharset()
  {
    return mAccount.getPrefMailDefaultCharset();
  }

  public boolean isLocalAccount()
  {
    try
    {
      String target = mAccount.getAttr("zimbraMailHost");
      String localhost = mAccount.getProvisioning().getLocalServer().getAttr("zimbraServiceHostname");
      boolean isLocal = target != null && target.equalsIgnoreCase(localhost);
      return isLocal;
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  @Deprecated
  public String getServerHostname()
  {
    return mAccount.getAttr(ProvisioningImp.A_zimbraMailHost,"localhost");
  }

    @Nonnull
  public ICalendarTimezone getAccountTimeZone()
  {
    ICalTimeZone accountTimeZone = Util.getAccountTimeZone(
      toZimbra(com.zimbra.cs.account.Account.class)
    );
    return new ICalendarTimezone(accountTimeZone);
  }

    public Identity getDefaultIdentity() {
    try
    {
      return new Identity(mAccount.getDefaultIdentity());
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public String getMailSieveScript() {
    return mAccount.getMailSieveScript();
  }

  public boolean mustChangePassword() {
    return mAccount.isPasswordMustChange();
  }

  static Account wrap(com.zimbra.cs.account.Account zAccount) {
    if (zAccount == null) {
      return null;
    }
    return new Account(zAccount);
  }

  public boolean isPasswordExpired() {
    int maxAge = mAccount.getIntAttr(com.zimbra.cs.account.Provisioning.A_zimbraPasswordMaxAge, 0);

    if (maxAge > 0) {
      Date lastChange =
          mAccount.getGeneralizedTimeAttr(
              com.zimbra.cs.account.Provisioning.A_zimbraPasswordModifiedTime, null);
      if (lastChange != null) {
        long last = lastChange.getTime();
        long curr = System.currentTimeMillis();
        return (last + (Constants.MILLIS_PER_DAY * maxAge)) < curr;
      }
    }
    return false;
  }

  public boolean isPasswordNoMoreValid() {
    return mustChangePassword() || isPasswordExpired();
  }

    public List<String> getAuthTokenEncoded() {
    Object encodedTokens = mAccount.getAttrs(false).get(ProvisioningImp.A_zimbraAuthTokens);
    if (encodedTokens == null) {
      return new ArrayList<>();
    } else if (encodedTokens instanceof String) {
      return Collections.singletonList((String) encodedTokens);
    } else if (encodedTokens instanceof String[]) {
      return Arrays.asList((String[]) encodedTokens);
    }
    throw new UnsupportedOperationException();
  }

  public boolean invalidateToken(String id, String version) {
    try {
      mAccount.removeAuthTokens(id, version);
      return true;
    } catch (ServiceException e) {
      throw ExceptionWrapper.wrap(e);
    }
  }

    public void cancelAllDataSources() {
    try {
      List<com.zimbra.cs.account.DataSource> dataSources = com.zimbra.cs.account.Provisioning.getInstance().getAllDataSources(mAccount);
      for (com.zimbra.cs.account.DataSource ds : dataSources) {
        DataSourceManager.cancelSchedule(mAccount, ds.getId());
      }
    } catch (ServiceException e) {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public void updateAllDataSources() {
    try {
      List<com.zimbra.cs.account.DataSource> dataSources = com.zimbra.cs.account.Provisioning.getInstance().getAllDataSources(mAccount);
      for (com.zimbra.cs.account.DataSource ds : dataSources) {
        DataSourceManager.updateSchedule(mAccount, ds);
      }
    } catch (ServiceException e) {
      throw ExceptionWrapper.wrap(e);
    }
  }
}

