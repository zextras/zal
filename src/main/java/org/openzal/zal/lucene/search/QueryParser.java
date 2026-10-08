package org.openzal.zal.lucene.search;

public class QueryParser
{

  public static boolean isValid(String str) {
    try {
      new com.zimbra.cs.index.query.parser.QueryParser(null, null)
          .parse(str);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
