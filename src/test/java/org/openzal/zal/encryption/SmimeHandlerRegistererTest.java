package org.openzal.zal.encryption;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.smime.SmimeHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SmimeHandlerRegistererTest {

    @Test
    void test_createSmimeHandler() throws ServiceException {
        OverriddenEncryptionHandler overriddenEncryptionHandler = Mockito.mock();
        SmimeHandler smimeHandler = SmimeHandlerRegisterer.createSmimeHandler(overriddenEncryptionHandler, true);
        Assertions.assertNotNull(smimeHandler);
        smimeHandler.decryptMessage(Mockito.mock(), Mockito.mock(), 0);
        Mockito.verify(overriddenEncryptionHandler, Mockito.times(1))
                .decryptMessage(Mockito.any(), Mockito.any(), Mockito.anyInt());
        Assertions.assertTrue(smimeHandler.signatureEnabled());
    }

    @Test
    void test_registerHandler() {
        OverriddenEncryptionHandler overriddenEncryptionHandler = Mockito.mock();
        SmimeHandler smimeHandler = SmimeHandlerRegisterer.createSmimeHandler(overriddenEncryptionHandler, true);
        SmimeHandlerRegisterer.registerHandler(smimeHandler);
        Assertions.assertEquals(smimeHandler, SmimeHandler.getHandler());
        SmimeHandler handler = Mockito.mock();
        SmimeHandlerRegisterer.registerHandler(handler);
        Assertions.assertEquals(handler, SmimeHandler.getHandler());
    }

    @Test
    void test_registerHandler_default() {
        SmimeHandlerRegisterer.registerHandler();
        SmimeHandler handler = SmimeHandler.getHandler();
        Assertions.assertNotNull(handler);
        SmimeHandlerRegisterer.registerHandler();
        Assertions.assertEquals(handler, SmimeHandler.getHandler());
    }

    @Test
    void test_registerHandler_reset_default_one() throws ServiceException {
        SmimeHandler mock = Mockito.mock();
        SmimeHandlerRegisterer.registerHandler(mock);
        SmimeHandlerRegisterer.registerHandler();
        Assertions.assertNotEquals(mock, SmimeHandler.getHandler());
    }

    @Test
    void test_registerHandler_with_overriden() throws ServiceException {
        OverriddenEncryptionHandler overriddenEncryptionHandler = Mockito.mock();
        SmimeHandlerRegisterer.registerHandler(overriddenEncryptionHandler, true);
        SmimeHandler smimeHandler = SmimeHandler.getHandler();
        Assertions.assertNotNull(smimeHandler);
        smimeHandler.decryptMessage(Mockito.mock(), Mockito.mock(), 0);
        Mockito.verify(overriddenEncryptionHandler, Mockito.times(1))
                .decryptMessage(Mockito.any(), Mockito.any(), Mockito.anyInt());
        Assertions.assertTrue(smimeHandler.signatureEnabled());
    }

}
