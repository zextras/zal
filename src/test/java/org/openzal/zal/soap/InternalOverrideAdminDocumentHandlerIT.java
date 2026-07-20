package org.openzal.zal.soap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InternalOverrideAdminDocumentHandlerIT
{
  @Test
  public void reflection_initialization()
  {
    InternalOverrideAdminDocumentHandler handler = new InternalOverrideAdminDocumentHandler(null,null);
  }
}