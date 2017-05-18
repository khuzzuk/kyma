package net.kyma.data;

import net.kyma.dm.MetadataField;
import net.kyma.dm.Rating;
import net.kyma.dm.SoundFile;
import net.kyma.player.Format;
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
        MetadataField.SET.forEach(m -> addField(doc, m, m.getGetter().apply(file)));
        doc.add(new StoredField("counter", file.getCounter()));
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

    private void addField(Document doc, MetadataField field, Rating rating, Format format) {
        addField(doc, field, format.isByteScale() ? rating.getValue() : rating.getValueP());
    }
}
