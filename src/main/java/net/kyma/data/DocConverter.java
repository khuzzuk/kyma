package net.kyma.data;

import lombok.extern.log4j.Log4j2;
import net.kyma.dm.SupportedFields;
import net.kyma.dm.Rating;
import net.kyma.dm.SoundFile;
import net.kyma.player.Format;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;

import static org.apache.lucene.document.Field.Store.YES;

@SuppressWarnings("unused")
@Log4j2
class DocConverter {
    Document docFrom(SoundFile file) {
        Document doc = new Document();
        SupportedFields.SET.forEach(m -> addField(doc, m, m.getGetter().apply(file)));
        doc.add(new StoredField("counter", file.getCounter()));
        return doc;
    }

    private void addField(Document doc, SupportedFields field, String value) {
        if (value == null) return;
        doc.add(new StringField(field.getName(), value, YES));
    }

    private void addField(Document doc, @SuppressWarnings("SameParameterValue") SupportedFields field, int value) {
        doc.add(new StoredField(field.getName(), value));
        doc.add(new IntPoint(field.getName(), value));
    }

    private void addField(Document doc, SupportedFields field, Rating rating, Format format) {
        addField(doc, field, format.isByteScale() ? rating.getValue() : rating.getValueP());
    }
}
