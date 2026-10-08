package org.openzal.zal.mime;

import com.zimbra.cs.mime.MimeTypeInfo;
import java.util.HashSet;
import java.util.Set;

/**
 * Mock implementation of {@link MimeTypeInfo} for testing.
 *
 * @author ysasaki
 */
public class MockMimeTypeInfo implements MimeTypeInfo {
  private String[] mimeTypes = new String[0];
  private Set<String> fileExtensions = new HashSet<String>();
  private String description;
  private boolean indexingEnabled;
  private String extension;
  private String handlerClass;
  private int priority;

  public String[] getMimeTypes() {
    return mimeTypes;
  }

  public void setMimeTypes(String... value) {
    mimeTypes = value;
  }

  public String getExtension() {
    return extension;
  }

    public String getHandlerClass() {
    return handlerClass;
  }

  public void setHandlerClass(String value) {
    handlerClass = value;
  }

  public boolean isIndexingEnabled() {
    return indexingEnabled;
  }

    public String getDescription() {
    return description;
  }

    public Set<String> getFileExtensions() {
    return fileExtensions;
  }

  public void setFileExtensions(String... value) {
    fileExtensions.clear();
    for (String ext : value) {
      fileExtensions.add(ext);
    }
  }

  public int getPriority() {
    return priority;
  }

}
