package net.kyma.data;

import net.kyma.dm.SupportedFields;
import org.apache.lucene.index.Term;

public class QueryUtils {
    public static Term termForPath(String path) {
        return new Term(SupportedFields.PATH.getName(), path);
    }
}
