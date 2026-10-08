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

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapParseException;

public class SoapResponseImpl implements SoapResponse
{
  public static SoapResponseImpl of(String wrapperElementName, String responseText) {
    return new SoapResponseImpl(parseJSON(wrapperElementName, responseText));
  }

  public static SoapResponseImpl fromJson(String jsonText) {
    try {
      return new SoapResponseImpl(Element.parseJSON(jsonText));
    } catch (SoapParseException e) {
      throw new RuntimeException(e);
    }
  }

  private static Element parseJSON(String action, String content) {
    try {
      return Element.parseJSON(
          content,
          new org.dom4j.QName(action),
          Element.JSONElement.mFactory
      );
    } catch (SoapParseException e) {
      throw new RuntimeException(e);
    }
  }

  private Element mElement;

  public SoapResponseImpl(
    Element element
  )
  {
    mElement = element;
  }

  public SoapResponseImpl(
    SoapElement element
  )
  {
    this(element.toZimbra(Element.class));
  }

  @Override
  public void setValue(String key, String value)
  {
    mElement.addAttribute(key, value);
  }

    @Override
  public void setResponse(SoapResponse soapResponse)
  {
    SoapResponseImpl response = (SoapResponseImpl)soapResponse;
    mElement = response.mElement;
  }

    public Element getElement()
  {
    return mElement;
  }

  public String getAttribute(String name) throws org.openzal.zal.exceptions.ServiceException {
    try {
      return getElement().getAttribute(name);
    } catch (ServiceException e) {
      throw new org.openzal.zal.exceptions.ServiceException(e);
    }
  }

  public String getElementName() {
    return getElement().getName();
  }

  public String toJsonString() {
    return getElement().toString();
  }
}
