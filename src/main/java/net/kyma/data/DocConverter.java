package net.kyma.data;

import net.kyma.dm.MetadataField;
import net.kyma.dm.SoundFile;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;

import static net.kyma.dm.MetadataField.*;
import static org.apache.lucene.document.Field.Store.YES;

public class DocConverter {
    public Document docFrom(SoundFile file) {
        Document doc = new Document();
        addField(doc, PATH, file.getPath());
        addField(doc, INDEXED_PATH, file.getIndexedPath());
        addField(doc, FILE_NAME, file.getFileName());

        addField(doc, TITLE, file.getTitle());
        return doc;
    }

    private void addField(Document doc, MetadataField field, String value) {
        doc.add(new StringField(field.getName(), value, YES));
    }
}
