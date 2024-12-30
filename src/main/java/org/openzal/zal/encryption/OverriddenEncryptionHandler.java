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

import org.openzal.zal.Mailbox;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public interface OverriddenEncryptionHandler {

    void sign(Mailbox mailbox, MimeMessage mimeMessage, String certificatePassword);

    void encrypt(Mailbox mailbox, MimeMessage mimeMessage, String certificatePassword);

    MimeMessage decryptMessage(Mailbox mailbox, MimeMessage mimeMessage, int itemId);

    void registerHandler();

    boolean isRegistered();

    default String getFrom(MimeMessage mimeMessage) {
        String from = null;

        try {

            Address fromAddress = mimeMessage.getFrom()[0];

            if (fromAddress instanceof InternetAddress fromAddr) {
                from = fromAddr.getAddress();
            }

            if (from == null) {
                throw new RuntimeException("from can not be defined");
            }

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return from;
    }
}
