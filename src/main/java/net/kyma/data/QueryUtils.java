package net.kyma.data;

import net.kyma.dm.SupportedField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

public class QueryUtils {
    public static Term termForPath(String path) {
        return new Term(SupportedField.PATH.getName(), path);
    }

    public static Query queryFrom(QueryParameters parameters) {
        return new TermQuery(new Term(parameters.getField().getName(), parameters.getValue()));
    }
}
