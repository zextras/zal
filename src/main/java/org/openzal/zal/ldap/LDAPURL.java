package org.openzal.zal.ldap;

import javax.annotation.Nonnull;

public class LDAPURL
{
  @Nonnull
  private final com.unboundid.ldap.sdk.LDAPURL mLDAPURL;

  public LDAPURL(@Nonnull Object mLdapUrl)
  {
    mLDAPURL = (com.unboundid.ldap.sdk.LDAPURL)mLdapUrl;
  }

  public String getHost()
  {
    return mLDAPURL.getHost();
  }

  public int getPort()
  {
    return mLDAPURL.getPort();
  }
}
