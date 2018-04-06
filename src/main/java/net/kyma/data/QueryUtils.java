package net.kyma.data;

import lombok.experimental.UtilityClass;
import net.kyma.dm.SupportedField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

@UtilityClass
class QueryUtils {
    static Term termForPath(String path) {
        return new Term(SupportedField.PATH.getName(), path);
    }

    static Query queryFrom(QueryParameters parameters) {
        return new TermQuery(new Term(parameters.getField().getName(), parameters.getValue()));
    }
}
