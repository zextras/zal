package org.openzal.zal.encryption;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.pgp.PgpHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PgpHandlerRegistererTest {

    @Test
    void test_createSmimeHandler() throws ServiceException {
        OverriddenEncryptionHandler overriddenEncryptionHandler = Mockito.mock();
        PgpHandler smimeHandler = PgpHandlerRegisterer.createPgpHandler(overriddenEncryptionHandler, true);
        Assertions.assertNotNull(smimeHandler);
        smimeHandler.decryptMessage(Mockito.mock(), Mockito.mock(), 0);
        Mockito.verify(overriddenEncryptionHandler, Mockito.times(1))
                .decryptMessage(Mockito.any(), Mockito.any(), Mockito.anyInt());
        Assertions.assertTrue(smimeHandler.signatureEnabled());
    }

    @Test
    void test_registerHandler() {
        OverriddenEncryptionHandler overriddenEncryptionHandler = Mockito.mock();
        PgpHandler smimeHandler = PgpHandlerRegisterer.createPgpHandler(overriddenEncryptionHandler, true);
        PgpHandlerRegisterer.registerHandler(smimeHandler);
        Assertions.assertEquals(smimeHandler, PgpHandler.getHandler());
        PgpHandler handler = Mockito.mock();
        PgpHandlerRegisterer.registerHandler(handler);
        Assertions.assertEquals(handler, PgpHandler.getHandler());
    }

    @Test
    void test_registerHandler_default() {
        PgpHandlerRegisterer.registerHandler();
        PgpHandler handler = PgpHandler.getHandler();
        Assertions.assertNull(handler);
        PgpHandlerRegisterer.registerHandler();
        Assertions.assertEquals(handler, PgpHandler.getHandler());
    }

    @Test
    void test_registerHandler_reset_default_one() {
        PgpHandler mock = Mockito.mock();
        PgpHandlerRegisterer.registerHandler(mock);
        PgpHandlerRegisterer.registerHandler();
        Assertions.assertNotEquals(mock, PgpHandler.getHandler());
    }

    @Test
    void test_registerHandler_with_overriden() throws ServiceException {
        OverriddenEncryptionHandler overriddenEncryptionHandler = Mockito.mock();
        PgpHandlerRegisterer.registerHandler(overriddenEncryptionHandler, true);
        PgpHandler smimeHandler = PgpHandler.getHandler();
        Assertions.assertNotNull(smimeHandler);
        smimeHandler.decryptMessage(Mockito.mock(), Mockito.mock(), 0);
        Mockito.verify(overriddenEncryptionHandler, Mockito.times(1))
                .decryptMessage(Mockito.any(), Mockito.any(), Mockito.anyInt());
        Assertions.assertTrue(smimeHandler.signatureEnabled());
    }

    @Test
    void test_getRegisteredClass_when_not_registered_then_return_null() {
        PgpHandlerRegisterer.registerHandler(null);
        Assertions.assertNull(PgpHandlerRegisterer.getRegisteredClass());
    }

    @Test
    void test_getRegisteredClass_when_registered_then_return_registered() {
        PgpHandler mock = Mockito.mock();
        PgpHandlerRegisterer.registerHandler(mock);
        Assertions.assertEquals(mock.getClass(), PgpHandlerRegisterer.getRegisteredClass());
    }

}
