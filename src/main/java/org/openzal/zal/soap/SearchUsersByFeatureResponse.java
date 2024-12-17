package org.openzal.zal.soap;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.service.admin.ToXML;
import org.openzal.zal.Entry;

import java.util.HashSet;
import java.util.List;

import static com.zimbra.soap.DocumentHandler.getZimbraSoapContext;

public class SearchUsersByFeatureResponse {

  public static SoapResponse buildResponse(ZimbraContext zimbraContext, List<Entry> entries, int total, boolean more) {
    var zsc = getZimbraSoapContext(zimbraContext.getContext());
    var response = zsc.createElement(AccountConstants.SEARCH_USERS_BY_FEATURE_RESPONSE);
    var attributes = new HashSet<String>();
    attributes.add(ZAttrProvisioning.A_mail);
    attributes.add(ZAttrProvisioning.A_uid);
    attributes.add(ZAttrProvisioning.A_displayName);
    response.addAttribute(AccountConstants.A_TOTAL, total);
    response.addAttribute(AccountConstants.A_MORE, more);
    entries.forEach(a -> ToXML.encodeAccount(response, (Account) a.toZimbraEntry(), true, attributes, null));
    return new SoapResponseImpl(response, null);
  }

}
