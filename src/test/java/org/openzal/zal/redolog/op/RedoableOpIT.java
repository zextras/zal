package org.openzal.zal.redolog.op;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class RedoableOpIT
{
  @Test
  public void reflection_initialization()
  {
    RedoableOp redoableOp = new RedoableOp(mock(com.zimbra.cs.redolog.op.RedoableOp.class));
  }
}