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

import com.zimbra.soap.DocumentHandler;
import com.zimbra.soap.SoapServlet;
import org.dom4j.QName;
import org.openzal.zal.Utils;
import org.openzal.zal.log.ZimbraLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SoapServiceManager
{
  private final static Map<QName, DocumentHandler> sOriginalHandlersMap;

  static
  {
    try
    {
      sOriginalHandlersMap = new ConcurrentHashMap<QName, DocumentHandler>(64);
    }
    catch (Throwable ex)
    {
      ZimbraLog.extensions.fatal("ZAL Reflection Initialization Exception: " + Utils.exceptionToString(ex));
      throw new RuntimeException(ex);
    }
  }

  static class InternalHandlerMapPublisher implements HandlerMapPublisher
  {
    @Override
    public void receivedHandlerMap(Map<QName, DocumentHandler> handlerMap)
    {
      for (Map.Entry<QName, DocumentHandler> entry : handlerMap.entrySet())
      {
        if (!sOriginalHandlersMap.containsKey(entry.getKey()))
        {
          sOriginalHandlersMap.put(entry.getKey(), entry.getValue());
        }
      }
    }
  }

  public void register(SoapService soapService)
  {
    SoapServlet.addService(
      soapService.getServiceName(),
      new InternalDocumentService(
        soapService,
        new InternalHandlerMapPublisher()
      )
    );
  }

  public void unregister(SoapService soapService)
  {
    SoapServlet.addService(
      soapService.getServiceName(),
      new InternalUnregisterDocumentService(soapService)
    );
    SoapServlet.invalidateServiceCache(soapService.getServiceName());
  }

  public void overrideZimbraHandler(OverridenSoapService soapService)
  {
    SoapServlet.addService(
      soapService.getServiceName(),
      new InternalOverrideDocumentServiceImpl(
        soapService,
        new InternalHandlerMapPublisher(),
        sOriginalHandlersMap
      )
    );
  }


  public void restoreZimbraHandlers(OverridenSoapService soapService)
  {
    SoapServlet.addService(
      soapService.getServiceName(),
      new InternalRestoreDocumentService(soapService, sOriginalHandlersMap)
    );
  }

}
