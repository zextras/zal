package org.openzal.zal.provisioning;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Provisioning.CacheMode;
import java.util.List;
import java.util.Map;
import org.openzal.zal.Account;
import org.openzal.zal.Domain;
import org.openzal.zal.ProvisioningImp;
import org.openzal.zal.Server;
import org.openzal.zal.SimpleVisitor;
import org.openzal.zal.ZimbraListWrapper;
import org.openzal.zal.exceptions.ExceptionWrapper;
import org.openzal.zal.exceptions.ZimbraException;

public class TestProvisioningImp extends ProvisioningImp {

    public TestProvisioningImp() {
        this(Provisioning.getInstance(CacheMode.OFF));
    }

    public TestProvisioningImp(Object provisioning) {
        super(provisioning);
    }

    @Override
    public GalSearchResult galSearch(Account account, String query, int skip, int limit) {
        return super.galSearch(account, query, skip, limit);
    }

    @Override
    public GalSearchResult galSearch(Account account, Domain domain, String query, int skip, int limit) {
        Map<String, Object> attrs = account.getAttrs(true);
        attrs.put("zimbraFeatureGalEnabled", "TRUE");
        attrs.put("zimbraFeatureGalAutoCompleteEnabled", "TRUE");
        account.setAttrs(attrs);
        return super.galSearch(account, domain, query, skip, limit);
    }

    @Override
    public void visitAllAccounts(SimpleVisitor<Account> visitor)
            throws ZimbraException {
        for (Domain domain : getAllDomains()) {
            for (Account account : getAllAccounts(domain)) {
                visitor.visit(account);
            }
        }
    }

    @Override
    public void visitAllDomains(SimpleVisitor<Domain> visitor) throws ZimbraException {
        for (Domain domain : getAllDomains()) {
            visitor.visit(domain);
        }
    }

    @Override
    public void visitDomainsWithAttributes(SimpleVisitor<Domain> visitor, Map<String, Object> attributes) throws ZimbraException {
        this.visitAllDomains(visitor);
    }

    @Override
    public List<Server> getAllServers(String service) throws ZimbraException {
        try {
            return ZimbraListWrapper.wrapServers(this.mProvisioning.getAllServers(service));
        } catch (ServiceException ex) {
            throw ExceptionWrapper.wrap(ex);
        }
    }

    @Override
    public List<Server> getAllServers() throws ZimbraException {
        try {
            return ZimbraListWrapper.wrapServers(this.mProvisioning.getAllServers());
        } catch (ServiceException ex) {
            throw ExceptionWrapper.wrap(ex);
        }
    }
}
