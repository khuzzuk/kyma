package net.kyma.data;

import java.util.List;

import lombok.experimental.UtilityClass;
import net.kyma.dm.DataQuery;
import net.kyma.dm.SupportedField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;

@UtilityClass
class QueryUtils {
    static Term termForPath(String path) {
        return new Term(SupportedField.PATH.getName(), path);
    }

    static Query queryFrom(QueryParameters parameters) {
        return new TermQuery(new Term(parameters.getField().getName(), parameters.getValue()));
    }

    static Query wildcardQueryFrom(QueryParameters parameters) {
        return new WildcardQuery(new Term(parameters.getField().getName(), parameters.getValue()));
    }

    static Query from(DataQuery dataQuery) {
        if (dataQuery.getParameters().size() == 1) {
            return fromSingle(dataQuery.getParameters().get(0));
        } else if (dataQuery.getParameters().size() > 1){
            return fromMultiple(dataQuery.getParameters());
        } else {
            return new MatchAllDocsQuery();
        }
    }

    private static Query fromMultiple(List<DataQuery.Parameter> parameters) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        parameters.stream()
              .map(QueryUtils::fromSingle)
              .forEach(query -> builder.add(query, BooleanClause.Occur.MUST));
        return builder.build();
    }

    private static Query fromSingle(DataQuery.Parameter parameter) {
        return parameter.isWildCard()
              ? new WildcardQuery(termFrom(parameter))
              : new TermQuery(termFrom(parameter));
    }

    private static Term termFrom(DataQuery.Parameter parameter) {
        return new Term(parameter.getField().getName(), parameter.getValue());
    }
}
