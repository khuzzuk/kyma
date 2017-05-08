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
        addField(doc, YEAR, file.getDate());
        addField(doc, ALBUM, file.getAlbum());
        addField(doc, ALBUM_ARTIST, file.getAlbumArtist());
        addField(doc, ALBUM_ARTISTS, file.getAlbumArtists());
        addField(doc, ARTIST, file.getArtist());
        addField(doc, ARTISTS, file.getArtists());
        addField(doc, COMPOSER, file.getComposer());
        addField(doc, CONDUCTOR, file.getConductor());
        addField(doc, COUNTRY, file.getCountry());
        addField(doc, CUSTOM1, file.getCustom1());
        addField(doc, CUSTOM2, file.getCustom2());
        addField(doc, CUSTOM3, file.getCustom3());
        addField(doc, CUSTOM4, file.getCustom4());
        addField(doc, CUSTOM5, file.getCustom5());
        addField(doc, DISC_NO, file.getDiscNo());
        addField(doc, GENRE, file.getGenre());
        addField(doc, GROUP, file.getGroup());
        addField(doc, INSTRUMENT, file.getInstrument());
        addField(doc, MOOD, file.getMood());
        addField(doc, MOVEMENT, file.getMovement());
        addField(doc, OCCASION, file.getOccasion());
        addField(doc, OPUS, file.getOpus());
        addField(doc, ORCHESTRA, file.getOrchestra());
        addField(doc, QUALITY, file.getQuality());
        addField(doc, RANKING, file.getRanking());
        addField(doc, TEMPO, file.getTempo());
        addField(doc, TONALITY, file.getTonality());
        addField(doc, TRACK, file.getTrack());
        addField(doc, WORK, file.getWork());
        addField(doc, WORK_TYPE, file.getWorkType());

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
