package net.kyma.data;

import net.kyma.dm.MetadataField;
import org.apache.lucene.index.Term;

public class QueryUtils {
    public static Term termForPath(String path) {
        return new Term(MetadataField.PATH.getName(), path);
    }
}
