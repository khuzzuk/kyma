package net.kyma.data;

import static net.kyma.EventType.DATA_QUERY_RESULT_FOR_CONTENT_VIEW;
import static net.kyma.dm.SupportedField.INDEXED_PATH;
import static net.kyma.dm.SupportedField.PATH;

import java.io.IOException;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;

/**
 * Query that match wildcard wrapped {@link net.kyma.dm.SupportedField#PATH}
 * and regular {@link net.kyma.dm.SupportedField#INDEXED_PATH}
 */
public class PathQueryParameters extends QueryParameters {
   @Getter(AccessLevel.PACKAGE)
   private String indexingPath;
   public PathQueryParameters(String value, String indexingPath) {
      super(PATH, "*" + value + "/*", DATA_QUERY_RESULT_FOR_CONTENT_VIEW);
      this.indexingPath = indexingPath;
   }

   @Override
   ScoreDoc[] performQuery(IndexSearcher searcher) throws IOException {
      if (getValue() == null || indexingPath == null) return new ScoreDoc[0];
      return searcher.search(createQuery(), Integer.MAX_VALUE).scoreDocs;
   }

   Query createQuery() {
      return new BooleanQuery.Builder()
            .add(new WildcardQuery(new Term(getField().getName(), getValue())), BooleanClause.Occur.MUST)
            .add(new TermQuery(new Term(INDEXED_PATH.getName(), indexingPath)), BooleanClause.Occur.MUST)
            .build();
   }
}
