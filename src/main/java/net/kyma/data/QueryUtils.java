package net.kyma.data;

import net.kyma.dm.SupportedField;
import org.apache.lucene.index.Term;

public class QueryUtils {
    public static Term termForPath(String path) {
        return new Term(SupportedField.PATH.getName(), path);
    }
}
