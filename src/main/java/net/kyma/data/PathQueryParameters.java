package net.kyma.data;

import static net.kyma.EventType.DATA_QUERY_RESULT_FOR_CONTENT_VIEW;
import static net.kyma.dm.SupportedField.PATH;

import java.io.IOException;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;

public class PathQueryParameters extends QueryParameters {
   public PathQueryParameters(String value) {
      super(PATH, "*" + value + "/*", DATA_QUERY_RESULT_FOR_CONTENT_VIEW);
   }

   @Override
   ScoreDoc[] performQuery(IndexSearcher searcher) throws IOException
   {
      if (getValue() == null) return new ScoreDoc[0];
      return searcher.search(QueryUtils.wildcardQueryFrom(this), Integer.MAX_VALUE).scoreDocs;
   }
}
