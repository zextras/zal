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

package org.openzal.zal.index;

import com.zimbra.cs.convert.AttachmentInfo;
import com.zimbra.cs.mime.*;
import org.apache.lucene.document.Document;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javax.activation.DataSource;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IndexerManager
{
  private static final List<Indexer>             sIndexerList = Collections.synchronizedList(new LinkedList<Indexer>());

  @Nullable
  private static       Map<String, MimeHandlerManager.HandlerInfo>       sOriginalMap;


  public void register(Indexer indexer)
  {
    sIndexerList.add(indexer);
  }

  public void unregister(Indexer indexer)
  {
    sIndexerList.remove(indexer);
  }

  public void attachToZimbra()
  {
    sOriginalMap = MimeHandlerManager.getHandlers();
    MimeHandlerManager.setHandlers(new IndexerProxyMap(
            sOriginalMap,
            new MimeHandlerProviderImpl()
    ));
  }

  public void detach()
  {
    if( sOriginalMap != null )
    {
      MimeHandlerManager.setHandlers(sOriginalMap);
    }
  }

  @Nullable
  public static Indexer getBestIndexer(String contentType, String fileExtension)
  {
    for (Indexer indexer : sIndexerList)
    {
      if (indexer.canHandle(contentType, fileExtension))
      {
        return indexer;
      }
    }
    return null;
  }

  class MimeHandlerProviderImpl implements MimeHandlerProvider
  {
    @Nullable
    @Override
    public MimeHandlerManager.HandlerInfo getMimeHandlerFor(String contentType, String fileExtension)
    {
      Indexer indexer = getBestIndexer(contentType, fileExtension);
      if (indexer != null)
      {
        return createHandlerInfoProxy(
          InternalMimeHandler.class,
          contentType
        );
      }

      return null;
    }
  }

  private MimeHandlerManager.HandlerInfo createHandlerInfoProxy(Class<? extends MimeHandler> cls, String contentType)
  {
    try
    {
      var info = new MimeHandlerManager.HandlerInfo();

      info.setRealMimeType(contentType);
      info.setClass(cls);

      info.setMimeType(new MimeTypeInfo() {
        @Override
        public String[] getMimeTypes() {
          return new String[0];
        }

        @Override
        public String getExtension() {
          return null;
        }

        @Override
        public String getHandlerClass() {
          return null;
        }

        @Override
        public boolean isIndexingEnabled() {
          return true;
        }

        @Override
        public String getDescription() {
          return null;
        }

        @Override
        public Set<String> getFileExtensions() {
          return null;
        }

        @Override
        public int getPriority() {
          return 0;
        }
      });

      return info;
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  public static class InternalMimeHandler extends MimeHandler
  {
    private String content;

    @Override
    protected boolean runsExternally()
    {
      return false;
    }

    @Override
    protected void addFields(Document doc)
    {

    }

    private Indexer getIndexer()
    {
      Indexer indexer = getBestIndexer(getContentType(), getExtension());
      return indexer == null ? new EmptyIndexer() : indexer;
    }

    @Override
    protected String getContentImpl() throws MimeHandlerException
    {
      if (content == null)
      {
        try {
          content = getIndexer().extractPlainText(
              getDataSource(),
              getContentType(),
              getExtension(),
              getFilename()
          );
        } catch (Exception e) {
          throw new MimeHandlerException(e);
        }
      }

      return content;
    }

    @Nonnull
    private String getExtension()
    {
      String extension = "";
      String filename = getFilename();
      if( filename != null)
      {
        int extensionIndex = filename.lastIndexOf('.');
        if (extensionIndex != -1)
        {
          extension = filename.substring(extensionIndex + 1);
        }
      }
      return extension;
    }

    @Override
    public String convert(AttachmentInfo doc, String urlPart)
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean doConversion()
    {
      return false;
    }

    public Object getInstance()
    {
      return this;
    }

    private static class EmptyIndexer implements Indexer
    {
      @Override
      public boolean canHandle(String contentType, String fileExtension)
      {
        return false;
      }

      @Override
      public String extractPlainText(DataSource dataSource,
                                     String contentType,
                                     String fileExtension,
                                     String fileName)
      {
        return "";
      }
    }
  }
}
