package org.openzal.zal;

import org.junit.jupiter.api.Test;

public class FlagIT
{

/*
  Cannot mock this class, using a constant still trigger the static{} block
*/
  @Test
  public void reflection_initialization()
  {
    int test = Flag.BITMASK_FROM_ME;
  }
}