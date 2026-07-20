package org.openzal.zal.soap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InternalOverrideDocumentHandlerIT
{
  @Test
  public void reflection_initialization()
  {
    InternalOverrideDocumentHandler handler = new InternalOverrideDocumentHandler(null,null);
  }
}