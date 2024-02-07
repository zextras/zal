package org.openzal.zal.account;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.GalContact;
import com.zimbra.cs.gal.GalSearchParams;
import com.zimbra.cs.gal.GalSearchResultCallback;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.openzal.zal.ProvisioningImp;
import org.openzal.zal.gal.SearchGalProperty;

public class CarbonioMockProvisioning extends MockProvisioning {

  @Override
  public void searchGal(GalSearchParams params) throws ServiceException {
    String query = params.getQuery().toLowerCase();
    SearchGalResult result = params.getResult();
    GalSearchResultCallback callback = params.createResultCallback();

    String regex = sqlPatternToRegex(query);
    for (String name:name2account.keySet())
    {
      {
        Account account = name2account.get(name);
        GalContact zimbraContact = new GalContact(account.getName(), account.getAttrs());
        ProvisioningImp.GalSearchResult.GalContact contact = new ProvisioningImp.GalSearchResult.GalContact(zimbraContact);
        boolean found = name.toLowerCase().matches(regex);

        if (!found)
        {
          List<SearchGalProperty> properties = getGalProperties(contact);

          for (SearchGalProperty property : properties)
          {
            if (property.getValue().toLowerCase().matches(regex))
            {
              found = true;
              break;
            }
          }
        }

        if (found)
        {
          callback.visit(zimbraContact);
          result.addMatch(zimbraContact);
        }
      }
    }

    callback.setHasMoreResult(false);
  }

  private List<SearchGalProperty> getGalProperties(
    ProvisioningImp.GalSearchResult.GalContact contact
  ) {
    List<SearchGalProperty> properties = new ArrayList<>(0);

    for (String key : SearchGalProperty.GAL_CONTACT_TAGS.keySet()) {
      if (StringUtils.isNotBlank(contact.getSingleAttr(key))) {
        SearchGalProperty.GalContactTags tagName = SearchGalProperty.GAL_CONTACT_TAGS.get(key);
        properties.add(
          new SearchGalProperty(
            tagName,
            contact.getSingleAttr(key)
          )
        );
      }
    }

    for (String key : SearchGalProperty.GAL_REGEX_CONTACT_TAGS.keySet()) {
      for (String value : contact.match(key)) {
        if (StringUtils.isNotBlank(value)) {
          SearchGalProperty.GalContactTags tagName = SearchGalProperty.GAL_REGEX_CONTACT_TAGS.get(key);
          properties.add(
            new SearchGalProperty(
              tagName,
              value
            )
          );
        }
      }
    }
    return properties;
  }
}
