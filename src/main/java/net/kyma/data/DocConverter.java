package net.kyma.data;

import net.kyma.dm.MetadataField;
import net.kyma.dm.SoundFile;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;

import static net.kyma.dm.MetadataField.*;
import static org.apache.lucene.document.Field.Store.YES;

@SuppressWarnings("unused")
class DocConverter {
    Document docFrom(SoundFile file) {
        Document doc = new Document();

        addField(doc, PATH, file.getPath());
        addField(doc, INDEXED_PATH, file.getIndexedPath());
        addField(doc, FILE_NAME, file.getFileName());
        addField(doc, TITLE, file.getTitle());
        addField(doc, RATE, file.getRate());
        addField(doc, YEAR, file.getYear());
        addField(doc, ALBUM, file.getAlbum());

        return doc;
    }

    private void addField(Document doc, MetadataField field, String value) {
        if (value == null) return;
        doc.add(new StringField(field.getName(), value, YES));
    }

    private void addField(Document doc, @SuppressWarnings("SameParameterValue") MetadataField field, int value) {
        doc.add(new StoredField(field.getName(), value));
        doc.add(new IntPoint(field.getName(), value));
    }
}
