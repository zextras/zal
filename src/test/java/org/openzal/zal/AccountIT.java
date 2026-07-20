package org.openzal.zal;

import org.openzal.zal.mailbox.ZalZimbraSimulator;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openzal.zal.extension.ConfigZimletStatus;
import org.openzal.zal.soap.SoapTransport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@SuppressWarnings("ConstantConditions")
public class AccountIT
{
  private ZalZimbraSimulator mZimbraSimulator;
  private Provisioning    mProvisioning;
  private Account         mAccount;
  private Domain          mMainDomain;

  @BeforeEach
  public void setup() throws Exception
  {
    mZimbraSimulator = new ZalZimbraSimulator();
    mProvisioning = mZimbraSimulator.getProvisioning();
    mMainDomain = mProvisioning.createDomain("example.com",new HashMap<String, Object>());
    mAccount = mProvisioning.createAccount("test@example.com","",new HashMap<String, Object>());
  }

  @AfterEach
  public void cleanup() throws Exception
  {
    mZimbraSimulator.cleanup();
  }

  @Test
  public void no_aliases_only_one_address_returned() throws Exception
  {
    List<String> aliases;

    aliases = new LinkedList<String>(
      mAccount.getAllAddressesIncludeDomainAliases(mProvisioning)
    );

    assertEquals(
      1L,
      (long) aliases.size()
    );
    assertEquals(
      "test@example.com",
      aliases.get(0)
    );
  }

  @Test
  public void two_alias_three_addresses_returned() throws Exception
  {
    mAccount.addAlias("alias_1@example.com");
    mAccount.addAlias("alias_2@example.com");

    List<String> aliases;

    aliases = new LinkedList<String>(
      mAccount.getAllAddressesIncludeDomainAliases(mProvisioning)
    );
    Collections.sort(aliases);

    assertEquals(
      3L,
      (long) aliases.size()
    );
    assertEquals(
      "alias_1@example.com",
      aliases.get(0)
    );
    assertEquals(
      "alias_2@example.com",
      aliases.get(1)
    );
    assertEquals(
      "test@example.com",
      aliases.get(2)
    );
  }

  @Test
  public void one_alias_plus_domain_alias_four_addresses_returned() throws Exception
  {
    List<String> aliases;

    mAccount.addAlias("alias@example.com");
    Domain aliasDomain = mProvisioning.createDomain("aliasdomain.com", new HashMap<String, Object>());

    HashMap<String, Object> attrs = new HashMap<String, Object>();
    attrs.put("zimbraDomainAliasTargetId", mMainDomain.getId());
    mProvisioning.modifyAttrs(aliasDomain,attrs);

    aliases = new LinkedList<String>(
      mAccount.getAllAddressesIncludeDomainAliases(mProvisioning)
    );
    Collections.sort(aliases);

    assertEquals(
      4L,
      (long) aliases.size()
    );
    assertEquals(
      "alias@aliasdomain.com",
      aliases.get(0)
    );
    assertEquals(
      "alias@example.com",
      aliases.get(1)
    );
    assertEquals(
      "test@aliasdomain.com",
      aliases.get(2)
    );
    assertEquals(
      "test@example.com",
      aliases.get(3)
    );
  }

  @Test
  public void alias_on_other_domain_returned() throws Exception
  {
    List<String> aliases;

    mAccount.addAlias("alias@otherdomain.com");
    mProvisioning.createDomain("otherdomain.com", new HashMap<String, Object>());
    Domain aliasDomain = mProvisioning.createDomain("example-alias.com", new HashMap<String, Object>());
    HashMap<String, Object> attrs = new HashMap<String, Object>();
    attrs.put("zimbraDomainAliasTargetId", mMainDomain.getId());
    mProvisioning.modifyAttrs(aliasDomain,attrs);

    aliases = new LinkedList<String>(
      mAccount.getAllAddressesIncludeDomainAliases(mProvisioning)
    );
    Collections.sort(aliases);

    assertEquals(
      3L,
      (long) aliases.size()
    );
    assertEquals(
      "alias@otherdomain.com",
      aliases.get(0)
    );
    assertEquals(
      "test@example-alias.com",
      aliases.get(1)
    );
    assertEquals(
      "test@example.com",
      aliases.get(2)
    );
  }

  @Test
  public void test_user_mandatory_zimlets()
  {
    String[] installedZimlets = {"!zimlet1", "+zimlet2", "-zimlet3"};
    HashMap<String, Object> attrsMap = new HashMap<>();
    attrsMap.put("zimbraZimletAvailableZimlets", installedZimlets);
    mAccount.setAttrs(attrsMap);
    Map<String, ConfigZimletStatus> userZimlets = mAccount.getUserAvailableZimlets();
    assertEquals(3, userZimlets.entrySet().size());
    assertEquals(userZimlets.get("zimlet1"), ConfigZimletStatus.Mandatory);
    assertEquals(userZimlets.get("zimlet2"), ConfigZimletStatus.Enabled);
    assertEquals(userZimlets.get("zimlet3"), ConfigZimletStatus.Disabled);
  }

  @Test
  public void include_allow_from_addresses() throws Exception
  {
    HashMap<String, Object> attrs = new HashMap<String, Object>();
    attrs.put("zimbraAllowFromAddress", "other@domain123.com");
    mProvisioning.modifyAttrs(mAccount,attrs);

    LinkedList<String> aliases = new LinkedList<String>(
      mAccount.getAllAddressesAllowedInFrom(mProvisioning)
    );
    Collections.sort(aliases);

    assertEquals(
      2L,
      (long) aliases.size()
    );
    assertEquals(
      "other@domain123.com",
      aliases.get(0)
    );
    assertEquals(
      "test@example.com",
      aliases.get(1)
    );
  }
}