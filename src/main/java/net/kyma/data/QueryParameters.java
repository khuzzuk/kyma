package net.kyma.data;

import static net.kyma.data.QueryUtils.queryFrom;

import java.io.IOException;

import lombok.Builder;
import lombok.Getter;
import net.kyma.EventType;
import net.kyma.dm.SupportedField;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;

@Getter
@Builder
public class QueryParameters {
   private SupportedField field;
   private String value;
   private EventType returnTopic;

   ScoreDoc[] performQuery(IndexSearcher searcher) throws IOException {
      if (field == null || value == null) return new ScoreDoc[0];
      return searcher.search(queryFrom(this), Integer.MAX_VALUE).scoreDocs;
   }
}
