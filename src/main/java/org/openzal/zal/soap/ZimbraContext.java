/*
 * ZAL - Zextras Abstraction Layer.
 * Copyright (C) 2023 ZeXtras S.r.l.
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

package org.openzal.zal.soap;

import org.openzal.zal.Continuation;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.Element;

public interface ZimbraContext extends SoapNode
{
  SoapResponse proxyRequestTo(String accountId);

  String getTargetAccountId();

  String getAuthenticatedAccontId();

  String getRequesterIp();

  SoapResponse execLocalRequest();

  HttpServletRequest getHttpServletRequest();

  Continuation getContinuation();

  boolean isDelegatedAuth();

  InternalDocumentHelper.ElementFactory getElementFactory();

  SoapElement getRequest();

  boolean hasParameter(String key);
}
