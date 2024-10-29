package org.openzal.zal.ldap;

import java.util.List;
import java.util.stream.Collectors;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.ldap.ZLdapFilterFactory;
import org.openzal.zal.Domain;

public class SearchDirectoryOptions {

  private com.zimbra.cs.account.SearchDirectoryOptions options;

  public SearchDirectoryOptions() {
    this.options = new com.zimbra.cs.account.SearchDirectoryOptions();
  }

  public void setSearchBases(List<Domain> multipleBases) {
    options.setMultipleBases(multipleBases.stream()
            .map((Domain domain) -> domain.toZimbra(com.zimbra.cs.account.Domain.class))
            .collect(Collectors.toList()));
  }

  public Object toZimbra() {
    return options;
  }

  public void setTypeFlagAccounts() throws org.openzal.zal.exceptions.ServiceException {
    try {
      options.setTypes(com.zimbra.cs.account.SearchDirectoryOptions.ObjectType.accounts);
    } catch (ServiceException e) {
      throw new org.openzal.zal.exceptions.ServiceException(e);
    }
  }

  public void setFilterString(String filterId, String filterString) {
    options.setFilterString(ZLdapFilterFactory.FilterId.valueOf(filterId), filterString);
  }

  public void setSortAttr(String displayName) {
    options.setSortAttr(displayName);
  }

  public void setSortOpt(String displayName) {
    options.setSortOpt(com.zimbra.cs.account.SearchDirectoryOptions.SortOpt.SORT_ASCENDING.valueOf(displayName));
  }
}
