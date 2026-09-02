package org.openzal.zal;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.SearchDirectoryOptions;
import com.zimbra.cs.ldap.ZLdapFilterFactorySimulator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openzal.zal.exceptions.ZimbraException;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.zimbra.cs.account.SearchDirectoryOptions.MakeObjectOpt.NO_DEFAULTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProvisioningImpCountLicensedAccountsTest
{
  private final String DEFAULT_COS_ID = "e00428a1-0c00-11d9-836a-000d93afea2a";

  @BeforeAll
  static void setUpFilterFactory()
  {
    ZLdapFilterFactorySimulator.setInstance();
  }

  private static final String ZIMBRA_COS_ID_ATTR = "zimbraCosId";
  private static final String ZIMBRA_ID = "zimbraId";

  private static NamedEntry accountWithCosId(String cosId)
  {
    NamedEntry entry = mock(NamedEntry.class);
    when(entry.getAttr(ZIMBRA_COS_ID_ATTR)).thenReturn(cosId);
    when(entry.getAttr(ZIMBRA_ID)).thenReturn(UUID.randomUUID().toString());
    return entry;
  }

  private static NamedEntry accountWithNullCosId()
  {
    return accountWithCosId(null);
  }

  private Map<String, Long> executeCountLicensedAccounts(NamedEntry... entries) throws Exception
  {
    com.zimbra.cs.account.Provisioning zimbraProvisioning =
        mock(com.zimbra.cs.account.Provisioning.class);

    Cos defaultCos = mock();
    when(defaultCos.getId()).thenReturn(DEFAULT_COS_ID);
    when(zimbraProvisioning.getCosByName(com.zimbra.cs.account.Provisioning.DEFAULT_COS_NAME)).thenReturn(defaultCos);

    doAnswer(invocation -> {
      @SuppressWarnings("unchecked")
      NamedEntry.Visitor visitor = invocation.getArgument(1);
      for (NamedEntry entry : entries)
      {
        visitor.visit(entry);
      }
      return null;
    }).when(zimbraProvisioning).searchDirectory(
        any(SearchDirectoryOptions.class),
        any(NamedEntry.Visitor.class));

    ProvisioningImp provisioningImp = new ProvisioningImp(zimbraProvisioning);
    return provisioningImp.countLicensedAccountsGroupByCosId();
  }

  // --- Basic behavior ---

  @Test
  public void empty_search_returns_empty_map() throws Exception
  {
    Map<String, Long> result = executeCountLicensedAccounts();
    assertTrue(result.isEmpty());
  }

  @Test
  public void single_account_single_cos() throws Exception
  {
    Map<String, Long> result = executeCountLicensedAccounts(accountWithCosId("cos-1"));
    assertEquals(1, result.size());
    assertEquals(1L, result.get("cos-1"));
  }

  @Test
  public void multiple_accounts_same_cos() throws Exception
  {
    Map<String, Long> result = executeCountLicensedAccounts(
        accountWithCosId("cos-1"),
        accountWithCosId("cos-1"),
        accountWithCosId("cos-1")
    );
    assertEquals(1, result.size());
    assertEquals(3L, result.get("cos-1"));
  }

  @Test
  public void multiple_accounts_different_cos() throws Exception
  {
    Map<String, Long> result = executeCountLicensedAccounts(
        accountWithCosId("cos-1"),
        accountWithCosId("cos-2"),
        accountWithCosId("cos-3")
    );
    assertEquals(3, result.size());
    assertEquals(1L, result.get("cos-1"));
    assertEquals(1L, result.get("cos-2"));
    assertEquals(1L, result.get("cos-3"));
  }

  @Test
  public void mixed_same_and_different_cos() throws Exception
  {
    Map<String, Long> result = executeCountLicensedAccounts(
        accountWithCosId("cos-1"),
        accountWithCosId("cos-2"),
        accountWithCosId("cos-1"),
        accountWithCosId("cos-3"),
        accountWithCosId("cos-2"),
        accountWithCosId("cos-1")
    );
    assertEquals(3, result.size());
    assertEquals(3L, result.get("cos-1"));
    assertEquals(2L, result.get("cos-2"));
    assertEquals(1L, result.get("cos-3"));
  }

  // --- Null and empty COS ID ---

  @Test
  public void null_cos_id_produces_null_key() throws Exception
  {
    Map<String, Long> result = executeCountLicensedAccounts(accountWithNullCosId());
    assertEquals(1, result.size());
    assertEquals(1L, result.get(DEFAULT_COS_ID));
  }

  @Test
  public void null_cos_id_mixed_with_valid() throws Exception
  {
    Map<String, Long> result = executeCountLicensedAccounts(
        accountWithNullCosId(),
        accountWithCosId("cos-1"),
        accountWithNullCosId()
    );
    assertEquals(2, result.size());
    assertEquals(2L, result.get(DEFAULT_COS_ID));
    assertEquals(1L, result.get("cos-1"));
  }

  @Test
  public void empty_string_cos_id() throws Exception
  {
    Map<String, Long> result = executeCountLicensedAccounts(accountWithCosId(""));
    assertEquals(1, result.size());
    assertEquals(1L, result.get(DEFAULT_COS_ID));
  }

  @Test
  public void single_null_cos_id_account() throws Exception
  {
    Map<String, Long> result = executeCountLicensedAccounts(
        accountWithNullCosId(),
        accountWithCosId("cos-a"),
        accountWithNullCosId()
    );
    assertEquals(2, result.size());
    assertEquals(2L, result.get(DEFAULT_COS_ID));
    assertEquals(1L, result.get("cos-a"));
  }

  @Test
  public void mixed_null_empty_valid_cos_ids() throws Exception
  {
    Map<String, Long> result = executeCountLicensedAccounts(
        accountWithNullCosId(),
        accountWithCosId(""),
        accountWithCosId("cos-valid"),
        accountWithNullCosId(),
        accountWithCosId(""),
        accountWithCosId("cos-valid"),
        accountWithCosId("cos-valid")
    );
    assertEquals(2, result.size());
    assertEquals(4L, result.get(DEFAULT_COS_ID));
    assertEquals(3L, result.get("cos-valid"));
  }

  @Test
  public void special_characters_in_cos_id() throws Exception
  {
    Map<String, Long> result = executeCountLicensedAccounts(
        accountWithCosId("id/with/slashes"),
        accountWithCosId("id:with:colons"),
        accountWithCosId("id with spaces"),
        accountWithCosId("id\nwith\nnewlines"),
        accountWithCosId("cos-unicode-\u00e9\u00e8\u00ea")
    );
    assertEquals(5, result.size());
    assertEquals(1L, result.get("id/with/slashes"));
    assertEquals(1L, result.get("id:with:colons"));
    assertEquals(1L, result.get("id with spaces"));
    assertEquals(1L, result.get("id\nwith\nnewlines"));
    assertEquals(1L, result.get("cos-unicode-\u00e9\u00e8\u00ea"));
  }

  @Test
  public void many_distinct_cos_ids() throws Exception
  {
    NamedEntry[] entries = IntStream.range(0, 20)
        .mapToObj(i -> accountWithCosId("cos-" + i))
        .toArray(NamedEntry[]::new);

    Map<String, Long> result = executeCountLicensedAccounts(entries);
    assertEquals(20, result.size());
    IntStream.range(0, 20).forEach(i -> assertEquals(1L, result.get("cos-" + i)));
  }

  @Test
  public void many_accounts_one_cos() throws Exception
  {
    NamedEntry[] entries = IntStream.range(0, 100)
        .mapToObj(i -> accountWithCosId("cos-big"))
        .toArray(NamedEntry[]::new);

    Map<String, Long> result = executeCountLicensedAccounts(entries);
    assertEquals(1, result.size());
    assertEquals(100L, result.get("cos-big"));
  }

  @Test
  public void long_cos_id_string() throws Exception
  {
    String longCosId = "cos-".repeat(200);
    Map<String, Long> result = executeCountLicensedAccounts(accountWithCosId(longCosId));
    assertEquals(1, result.size());
    assertEquals(1L, result.get(longCosId));
  }

  // --- Search options verification ---

  @Test
  public void verify_search_options_configuration() throws Exception
  {
    com.zimbra.cs.account.Provisioning zimbraProvisioning =
        mock(com.zimbra.cs.account.Provisioning.class);

    @SuppressWarnings("unchecked")
    org.mockito.ArgumentCaptor<SearchDirectoryOptions> optionsCaptor =
        org.mockito.ArgumentCaptor.forClass(SearchDirectoryOptions.class);

    doAnswer(invocation -> null)
        .when(zimbraProvisioning).searchDirectory(
            optionsCaptor.capture(),
            any(NamedEntry.Visitor.class));

    ProvisioningImp provisioningImp = new ProvisioningImp(zimbraProvisioning);
    provisioningImp.countLicensedAccountsGroupByCosId();

    SearchDirectoryOptions options = optionsCaptor.getValue();
    assertEquals(Set.of(SearchDirectoryOptions.ObjectType.accounts), options.getTypes());
    assertEquals(Set.of(com.zimbra.cs.account.Provisioning.A_zimbraCOSId), Set.of(options.getReturnAttrs()));
    assertEquals(NO_DEFAULTS, options.getMakeObjectOpt());
  }

  // --- Exception wrapping ---

  @Test
  public void service_exception_throws_zimbra_exception() throws Exception
  {
    com.zimbra.cs.account.Provisioning zimbraProvisioning =
        mock(com.zimbra.cs.account.Provisioning.class);

    ServiceException serviceException =
        ServiceException.FAILURE("LDAP search failed", null);

    doAnswer(invocation -> { throw serviceException; })
        .when(zimbraProvisioning).searchDirectory(
            any(SearchDirectoryOptions.class),
            any(NamedEntry.Visitor.class));

    ProvisioningImp provisioningImp = new ProvisioningImp(zimbraProvisioning);

    assertThrows(ZimbraException.class, provisioningImp::countLicensedAccountsGroupByCosId);
  }

  // --- Delegation verification ---

  @Test
  public void search_directory_called_once() throws Exception
  {
    com.zimbra.cs.account.Provisioning zimbraProvisioning =
        mock(com.zimbra.cs.account.Provisioning.class);

    executeCountLicensedAccounts(
        zimbraProvisioning,
        accountWithCosId("cos-1"),
        accountWithCosId("cos-2")
    );

    verify(zimbraProvisioning).searchDirectory(
        any(SearchDirectoryOptions.class),
        any(NamedEntry.Visitor.class));
  }

  @Test
  public void search_directory_called_once_on_empty_input() throws Exception
  {
    com.zimbra.cs.account.Provisioning zimbraProvisioning =
        mock(com.zimbra.cs.account.Provisioning.class);

    doAnswer(invocation -> null)
        .when(zimbraProvisioning).searchDirectory(
            any(SearchDirectoryOptions.class),
            any(NamedEntry.Visitor.class));

    ProvisioningImp provisioningImp = new ProvisioningImp(zimbraProvisioning);
    provisioningImp.countLicensedAccountsGroupByCosId();

    verify(zimbraProvisioning).searchDirectory(
        any(SearchDirectoryOptions.class),
        any(NamedEntry.Visitor.class));
  }

  @Test
  public void no_visitor_invocation_on_empty_search() throws Exception
  {
    com.zimbra.cs.account.Provisioning zimbraProvisioning =
        mock(com.zimbra.cs.account.Provisioning.class);

    doAnswer(invocation -> null)
        .when(zimbraProvisioning).searchDirectory(
            any(SearchDirectoryOptions.class),
            any(NamedEntry.Visitor.class));

    ProvisioningImp provisioningImp = new ProvisioningImp(zimbraProvisioning);
    Map<String, Long> result = provisioningImp.countLicensedAccountsGroupByCosId();

    assertTrue(result.isEmpty());
    verify(zimbraProvisioning).searchDirectory(
        any(SearchDirectoryOptions.class),
        any(NamedEntry.Visitor.class));
  }

  // --- Helper overload for direct provisioning injection ---

  private Map<String, Long> executeCountLicensedAccounts(
      com.zimbra.cs.account.Provisioning zimbraProvisioning,
      NamedEntry... entries
  ) throws Exception
  {
    doAnswer(invocation -> {
      @SuppressWarnings("unchecked")
      NamedEntry.Visitor visitor = invocation.getArgument(1);
      for (NamedEntry entry : entries)
      {
        visitor.visit(entry);
      }
      return null;
    }).when(zimbraProvisioning).searchDirectory(
        any(SearchDirectoryOptions.class),
        any(NamedEntry.Visitor.class));

    ProvisioningImp provisioningImp = new ProvisioningImp(zimbraProvisioning);
    return provisioningImp.countLicensedAccountsGroupByCosId();
  }
}
