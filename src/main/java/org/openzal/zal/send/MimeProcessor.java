/*
 * ZAL - Zextras Abstraction Layer.
 * Copyright (C) 2024 ZeXtras S.r.l.
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

package org.openzal.zal.send;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Mailbox;

import javax.mail.internet.MimeMessage;

public class MimeProcessor extends com.zimbra.cs.mime.MimeProcessor {
    private final ZalMimeProcessor zalMimeProcessor;

    public MimeProcessor(ZalMimeProcessor zalMimeProcessor) {
        this.zalMimeProcessor = zalMimeProcessor;
    }

    @Override
    public void process(MimeMessage mm, Mailbox mbox) throws ServiceException {
        zalMimeProcessor.process(mm, new org.openzal.zal.Mailbox(mbox));
    }
}
