package org.openzal.zal.ldap;

import javax.annotation.Nonnull;

import java.io.Closeable;
import java.io.IOException;

public class LDIFWriter implements Closeable
{
  @Nonnull
  private final com.unboundid.ldif.LDIFWriter mLDIFWriter;

  public LDIFWriter(String path) throws LDAPException
  {
    try
    {
      mLDIFWriter = new com.unboundid.ldif.LDIFWriter(path);
    }
    catch (IOException e)
    {
      throw new LDAPException(e);
    }
  }

  public void writeEntry(Entry entry) throws IOException
  {
    mLDIFWriter.writeEntry(entry.toZimbra(com.unboundid.ldap.sdk.Entry.class));
  }

  public void writeEntry(ZalSearchResultEntry entry) throws IOException
  {
    mLDIFWriter.writeEntry(entry.toZimbra(com.unboundid.ldap.sdk.SearchResultEntry.class));
  }

  @Override
  public void close() throws IOException
  {
    mLDIFWriter.close();
  }

}
