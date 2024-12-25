package org.openzal.zal.encryption;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.smime.SmimeHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SmimeHandlerFactoryTest {

    @Test
    void test_createSmimeHandler() throws ServiceException {
        OverriddenSmimeHandler overriddenSmimeHandler = Mockito.mock();
        SmimeHandler smimeHandler = SmimeHandlerFactory.createSmimeHandler(overriddenSmimeHandler, true);
        Assertions.assertNotNull(smimeHandler);
        smimeHandler.decryptMessage(Mockito.mock(), Mockito.mock(), 0);
        Mockito.verify(overriddenSmimeHandler, Mockito.times(1))
                .decryptMessage(Mockito.any(), Mockito.any(), Mockito.anyInt());
        Assertions.assertTrue(smimeHandler.signatureEnabled());
    }

    @Test
    void test_registerHandler() {
        OverriddenSmimeHandler overriddenSmimeHandler = Mockito.mock();
        SmimeHandler smimeHandler = SmimeHandlerFactory.createSmimeHandler(overriddenSmimeHandler, true);
        SmimeHandlerFactory.registerHandler(smimeHandler);
        Assertions.assertEquals(smimeHandler, SmimeHandler.getHandler());
        SmimeHandler handler = Mockito.mock();
        SmimeHandlerFactory.registerHandler(handler);
        Assertions.assertEquals(handler, SmimeHandler.getHandler());
    }

    @Test
    void test_registerHandler_default() {
        SmimeHandlerFactory.registerHandler();
        SmimeHandler handler = SmimeHandler.getHandler();
        Assertions.assertNotNull(handler);
        SmimeHandlerFactory.registerHandler();
        Assertions.assertEquals(handler, SmimeHandler.getHandler());
    }

    @Test
    void test_registerHandler_reset_default_one() throws ServiceException {
        SmimeHandler mock = Mockito.mock();
        SmimeHandlerFactory.registerHandler(mock);
        SmimeHandlerFactory.registerHandler();
        Assertions.assertNotEquals(mock, SmimeHandler.getHandler());
    }

    @Test
    void test_registerHandler_with_overriden() throws ServiceException {
        OverriddenSmimeHandler overriddenSmimeHandler = Mockito.mock();
        SmimeHandlerFactory.registerHandler(overriddenSmimeHandler, true);
        SmimeHandler smimeHandler = SmimeHandler.getHandler();
        Assertions.assertNotNull(smimeHandler);
        smimeHandler.decryptMessage(Mockito.mock(), Mockito.mock(), 0);
        Mockito.verify(overriddenSmimeHandler, Mockito.times(1))
                .decryptMessage(Mockito.any(), Mockito.any(), Mockito.anyInt());
        Assertions.assertTrue(smimeHandler.signatureEnabled());
    }

}
