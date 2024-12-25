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

import com.zimbra.cs.smime.SmimeHandler;
import com.zextras.mailbox.encryption.smime.SmimeHandlerImpl;
import org.openzal.zal.Mailbox;

import javax.mail.internet.MimeMessage;

public final class SmimeHandlerRegisterer {

    private SmimeHandlerRegisterer() {}

    public static SmimeHandler createSmimeHandler(OverriddenEncryptionHandler overriddenEncryptionHandler, boolean signatureEnabled) {
        return new SmimeHandlerImpl() {

            @Override
            public MimeMessage decryptMessage(com.zimbra.cs.mailbox.Mailbox mailbox, MimeMessage mimeMessage, int itemId) {
                return overriddenEncryptionHandler.decryptMessage(new Mailbox(mailbox), mimeMessage, itemId);
            }

            @Override
            public boolean signatureEnabled() {
                return signatureEnabled || super.signatureEnabled();
            }

        };

    }

    public static void removeHandler() {
        SmimeHandler.registerHandler(null);
    }

    public static void registerHandler(SmimeHandler smimeHandler) {
        SmimeHandler.registerHandler(smimeHandler);
    }

    public static void registerHandler() {
        if (SmimeHandlerImpl.class.getName().equals(SmimeHandler.getHandler().getClass().getName())) {
            return;
        }
        removeHandler();
    }

    public static Class<?> getRegisteredClass() {
        SmimeHandler handler = SmimeHandler.getHandler();
        return handler == null ? null : handler.getClass();
    }

    public static void registerHandler(OverriddenEncryptionHandler overriddenEncryptionHandler, boolean signatureEnabled) {
        registerHandler(createSmimeHandler(overriddenEncryptionHandler, signatureEnabled));
    }
}
