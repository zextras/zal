package org.openzal.zal.soap;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class ZimbraContextImplIT
{
  @Test
  public void reflection_initialization()
  {
    ZimbraContextImpl zimbraContext = new ZimbraContextImpl(new HashMap<String, Object>());
  }
}