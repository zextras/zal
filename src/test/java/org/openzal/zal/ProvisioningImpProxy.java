package org.openzal.zal;

import javax.annotation.Nonnull;
import org.junit.jupiter.api.Disabled;
import org.openzal.zal.exceptions.ZimbraException;

@Disabled
public class ProvisioningImpProxy extends ProvisioningImp
{
  public ProvisioningImpProxy(Object provisioning)
  {
    super(provisioning);
  }

  @Override
  @Nonnull
  public GalSearchResult galSearch(@Nonnull Account account, String query, int skip, int limit)
  {
    return super.galSearch(account, query, skip, limit);
  }

    @Override
  public void visitAllAccounts(@Nonnull SimpleVisitor<Account> visitor)
    throws ZimbraException
  {
    for( Domain domain : getAllDomains() )
    {
      for( Account account : getAllAccounts(domain) )
      {
        visitor.visit(account);
      }
    }
  }
}