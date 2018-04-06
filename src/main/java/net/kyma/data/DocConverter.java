package net.kyma.data;

import static org.apache.lucene.document.Field.Store.YES;

import lombok.extern.log4j.Log4j2;
import net.kyma.dm.SoundFile;
import net.kyma.dm.SupportedField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;

@SuppressWarnings("unused")
@Log4j2
public class DocConverter {
    Document docFrom(SoundFile file) {
        Document doc = new Document();
        SupportedField.SET.forEach(m -> addField(doc, m, m.getGetter().apply(file)));
        doc.add(new StoredField("counter", file.getCounter()));
        return doc;
    }

    private void addField(Document doc, SupportedField field, String value) {
        if (value == null) return;
        doc.add(new StringField(field.getName(), value, YES));
    }
}
