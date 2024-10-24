package org.openzal.zal.ldap;

import com.zimbra.common.service.ServiceException;
import java.util.List;
import java.util.stream.Collectors;
import org.openzal.zal.Domain;

public class SearchDirectoryOptions extends com.zimbra.cs.account.SearchDirectoryOptions {

  public void setSearchBases(List<Domain> multipleBases) {
    setMultipleBases(multipleBases.stream().map(domain -> getDomain()).collect(Collectors.toList()));
  }

  public void setType(ObjectType objectType) {
    try {
      setTypes(objectType);
    } catch (ServiceException exception) {
      exception.printStackTrace();
    }
  }

  public ObjectType setFlag(String flag) {
    try {
      return ObjectType.fromString(flag);
    } catch (ServiceException exception) {
      exception.printStackTrace();
    }
    return ObjectType.accounts;
  }
}
