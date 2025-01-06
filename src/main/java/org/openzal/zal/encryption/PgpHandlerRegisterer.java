/*
 * ZAL - Zextras Abstraction Layer.
 * Copyright (C) 2025 ZeXtras S.r.l.
 *
 * This file is part of ZAL.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZAL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.openzal.zal.encryption;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.pgp.PgpHandler;
import org.openzal.zal.Account;

import javax.mail.internet.MimeMessage;

public final class PgpHandlerRegisterer {

    private PgpHandlerRegisterer() {
    }

    public static PgpHandler createPgpHandler(OverriddenEncryptionHandler overriddenEncryptionHandler, boolean signatureEnabled) {
        return new PgpHandler() {

            @Override
            public MimeMessage decryptMessage(com.zimbra.cs.mailbox.Mailbox mailbox, MimeMessage mimeMessage, int itemId) throws ServiceException {
                return overriddenEncryptionHandler.decryptMessage(new Account(mailbox.getAccount()), mimeMessage, itemId);
            }

            @Override
            public boolean verifyMessageSignature(Message msg, Element m, MimeMessage mm, OperationContext octxt) {
                return false;
            }

            @Override
            public boolean signatureEnabled() {
                return signatureEnabled;
            }

        };

    }

    public static void removeHandler() {
        PgpHandler.registerHandler(null);
    }

    public static void registerHandler(PgpHandler pgpHandler) {
        PgpHandler.registerHandler(pgpHandler);
    }

    public static void registerHandler() {
        removeHandler();
    }

    public static void registerHandler(OverriddenEncryptionHandler overriddenEncryptionHandler, boolean signatureEnabled) {
        registerHandler(createPgpHandler(overriddenEncryptionHandler, signatureEnabled));
    }

    public static Class<?> getRegisteredClass() {
        PgpHandler handler = PgpHandler.getHandler();
        return handler == null ? null : handler.getClass();
    }
}
