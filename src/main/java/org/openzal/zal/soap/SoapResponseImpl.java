/*
 * ZAL - The abstraction layer for Zimbra.
 * Copyright (C) 2016 ZeXtras S.r.l.
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

import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapParseException;
import javax.annotation.Nonnull;

public class SoapResponseImpl implements SoapResponse
{
  public static SoapResponseImpl of(String wrapperElementName, String responseText) {
    return new SoapResponseImpl(parseJSON(wrapperElementName, responseText), null);
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
  private final InternalDocumentHelper.ElementFactory mElementFactory;

  public SoapResponseImpl(
    Element element,
    InternalDocumentHelper.ElementFactory elementFactory
  )
  {
    mElement = element;
    mElementFactory = elementFactory;
  }

  public SoapResponseImpl(
    SoapElement element,
    InternalDocumentHelper.ElementFactory elementFactory
  )
  {
    this(element.toZimbra(Element.class), elementFactory);
  }

  @Override
  public void setValue(String key, String value)
  {
    mElement.addAttribute(key, value);
  }

  @Override
  public void setValue(String key, boolean value)
  {
    mElement.addAttribute(key, value);
  }

  @Override
  public void setValue(String key, long value)
  {
    mElement.addAttribute(key, value);
  }

  @Override
  public void setQName(QName qName)
  {
   mElement = mElementFactory.createElement(qName);
  }

  @Override
  public void setResponse(SoapResponse soapResponse)
  {
    SoapResponseImpl response = (SoapResponseImpl)soapResponse;
    mElement = response.mElement;
  }

  @Nonnull
  @Override
  public SoapResponse createNode(String name)
  {
    return new SoapResponseImpl(mElement.addElement(name), mElementFactory);
  }

  public Element getElement()
  {
    return mElement;
  }
}
