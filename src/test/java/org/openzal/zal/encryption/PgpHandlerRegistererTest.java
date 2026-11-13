package org.openzal.zal.encryption;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.pgp.PgpHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PgpHandlerRegistererTest {

    @Test
    void test_createPgpHandler() throws ServiceException {
        OverriddenEncryptionHandler overriddenEncryptionHandler = Mockito.mock();
        Provisioning provisioning = Mockito.mock();
        PgpHandler pgpHandler = PgpHandlerRegisterer.createPgpHandler(overriddenEncryptionHandler, true, provisioning);
        Assertions.assertNotNull(pgpHandler);
        Mailbox mailbox = Mockito.mock();
        Mockito.when(provisioning.getAccountByMailbox(mailbox)).thenReturn(Mockito.mock());
        pgpHandler.decryptMessage(mailbox, Mockito.mock(), 0);
        Mockito.verify(overriddenEncryptionHandler, Mockito.times(1))
                .decryptMessage(Mockito.any(), Mockito.any(), Mockito.anyInt());
        Assertions.assertTrue(pgpHandler.signatureEnabled());
    }

    @Test
    void test_registerHandler() {
        OverriddenEncryptionHandler overriddenEncryptionHandler = Mockito.mock();
        Provisioning provisioning = Mockito.mock();
        PgpHandler pgpHandler = PgpHandlerRegisterer.createPgpHandler(overriddenEncryptionHandler, true, provisioning);
        PgpHandlerRegisterer.registerHandler(pgpHandler);
        Assertions.assertEquals(pgpHandler, PgpHandler.getHandler());
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
        Provisioning provisioning = Mockito.mock();
        PgpHandlerRegisterer.registerHandler(overriddenEncryptionHandler, true, provisioning);
        PgpHandler pgpHandler = PgpHandler.getHandler();
        Assertions.assertNotNull(pgpHandler);
        Mailbox mailbox = Mockito.mock();
        Mockito.when(provisioning.getAccountByMailbox(mailbox)).thenReturn(Mockito.mock());
        pgpHandler.decryptMessage(mailbox, Mockito.mock(), 0);
        Mockito.verify(overriddenEncryptionHandler, Mockito.times(1))
                .decryptMessage(Mockito.any(), Mockito.any(), Mockito.anyInt());
        Assertions.assertTrue(pgpHandler.signatureEnabled());
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
