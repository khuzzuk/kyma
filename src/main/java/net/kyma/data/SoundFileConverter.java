package net.kyma.data;

import lombok.extern.log4j.Log4j2;
import net.kyma.dm.Rating;
import net.kyma.dm.SoundFile;
import net.kyma.player.Format;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.text.ParseException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static net.kyma.dm.MetadataField.*;

@Log4j2
@Singleton
public class SoundFileConverter {
    private static final String[] dateParsers = new String[]{"yyyy", "yyyy-mm-dd"};
    @Inject
    public SoundFileConverter(Bus bus, @Named("messages") Properties messages) {
        bus.<File, SoundFile>setResponse(messages.getProperty("playlist.add.file"), f -> from(f, f.getParent()));
        bus.<Collection<Document>>setReaction(messages.getProperty("data.convert.from.doc.gui"),
                docs -> bus.send(messages.getProperty("data.view.refresh"),
                        docs.stream().map(this::from).collect(Collectors.toList())));
    }

    SoundFile from(File file, String indexedPath) {
        SoundFile sound = new SoundFile();
        sound.setPath(file.getPath());
        sound.setFormat(Format.forPath(file.getPath()));
        sound.setFileName(file.getName());
        sound.setIndexedPath(file.getPath().replace(indexedPath, ""));

        Optional.ofNullable(MetadataConverter.getMetadataFrom(file)).ifPresent(m -> fillData(sound, m));
        return sound;
    }

    private SoundFile from(Document document) {
        SoundFile soundFile = new SoundFile();
        soundFile.setPath(document.get(PATH.getName()));
        soundFile.setFormat(Format.forPath(soundFile.getPath()));
        SET.forEach(m -> m.getSetter().accept(soundFile, document.get(m.getName())));

        soundFile.setCounter(Optional.ofNullable(document.getField("counter"))
                .map(IndexableField::numericValue)
                .map(Number::intValue).orElse(0));

        return soundFile;
    }

    private void fillData(SoundFile sound, Tag metadata) {
        sound.setTitle(metadata.getFirst(FieldKey.TITLE));
        sound.setRate(Rating.getRatingBy(NumberUtils.toInt(metadata.getFirst(FieldKey.RATING), 0),
                sound.getFormat()));
        sound.setDate(metadata.getFirst(FieldKey.YEAR));
        sound.setAlbum(metadata.getFirst(FieldKey.ALBUM));
        sound.setAlbumArtist(metadata.getFirst(FieldKey.ALBUM_ARTIST));
        sound.setAlbumArtists(metadata.getFirst(FieldKey.ALBUM_ARTISTS));
        sound.setArtist(metadata.getFirst(FieldKey.ARTIST));
        sound.setArtists(metadata.getFirst(FieldKey.ARTISTS));
        sound.setComposer(metadata.getFirst(FieldKey.COMPOSER));
        sound.setConductor(metadata.getFirst(FieldKey.CONDUCTOR));
        sound.setCountry(metadata.getFirst(FieldKey.COUNTRY));
        sound.setCustom1(metadata.getFirst(FieldKey.CUSTOM1));
        sound.setCustom2(metadata.getFirst(FieldKey.CUSTOM2));
        sound.setCustom3(metadata.getFirst(FieldKey.CUSTOM3));
        sound.setCustom4(metadata.getFirst(FieldKey.CUSTOM4));
        sound.setCustom5(metadata.getFirst(FieldKey.CUSTOM5));
        sound.setDiscNo(metadata.getFirst(FieldKey.DISC_NO));
        sound.setGenre(metadata.getFirst(FieldKey.GENRE));
        sound.setGroup(metadata.getFirst(FieldKey.GROUP));
        sound.setInstrument(metadata.getFirst(FieldKey.INSTRUMENT));
        sound.setMood(metadata.getFirst(FieldKey.MOOD));
        sound.setMovement(metadata.getFirst(FieldKey.MOVEMENT));
        sound.setOccasion(metadata.getFirst(FieldKey.OCCASION));
        sound.setOpus(metadata.getFirst(FieldKey.OPUS));
        sound.setOrchestra(metadata.getFirst(FieldKey.ORCHESTRA));
        sound.setQuality(metadata.getFirst(FieldKey.QUALITY));
        sound.setRanking(metadata.getFirst(FieldKey.RANKING));
        sound.setTempo(metadata.getFirst(FieldKey.TEMPO));
        sound.setTonality(metadata.getFirst(FieldKey.TONALITY));
        sound.setTrack(metadata.getFirst(FieldKey.TRACK));
        sound.setWork(metadata.getFirst(FieldKey.WORK));
        sound.setWorkType(metadata.getFirst(FieldKey.WORK_TYPE));
    }
}
