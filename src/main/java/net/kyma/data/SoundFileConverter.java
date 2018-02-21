package net.kyma.data;

import lombok.extern.log4j.Log4j2;
import net.kyma.dm.Rating;
import net.kyma.dm.SoundFile;
import net.kyma.dm.SupportedField;
import net.kyma.player.Format;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTagField;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static net.kyma.dm.SupportedField.*;

@Log4j2
@Singleton
public class SoundFileConverter {
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
        sound.setIndexedPath(indexedPath);

        Optional.ofNullable(MetadataConverter.getMetadataFrom(file)).ifPresent(m -> fillData(sound, m));
        return sound;
    }

    private SoundFile from(Document document) {
        SoundFile soundFile = new SoundFile();
        SET.forEach(m -> m.getSetter().accept(soundFile, document.get(m.getName())));

        soundFile.setCounter(Optional.ofNullable(document.getField("counter"))
                .map(IndexableField::numericValue)
                .map(Number::intValue).orElse(0));

        return soundFile;
    }

    private void fillData(SoundFile sound, Tag metadata) {
        SupportedField.SUPPORTED_TAG.forEach(f -> f.getSetter().accept(sound, metadata.getFirst(f.getMappedKey())));
        sound.setRate(Rating.getRatingBy(NumberUtils.toInt(metadata.getFirst(FieldKey.RATING), 0),
                sound.getFormat()));

        String comment = getComment(metadata);
        if (comment.contains(":")) {
            String counter = comment.substring(0, comment.indexOf(":"));
            if (NumberUtils.isDigits(counter)) {
                sound.setCounter(NumberUtils.toInt(counter));
            }
        }
    }

    private String getComment(Tag metadata) {
        List<TagField> commFields = metadata.getFields(FieldKey.COMMENT);
        if (commFields.isEmpty()) return "";
        TagField tagField = commFields.get(commFields.size() - 1);
        String commentField = tagField.toString();
        if (tagField instanceof VorbisCommentTagField) {
            return commentField;
        }
        int startIndex = commentField.indexOf("Text=") + 6;
        int endIndex = commentField.indexOf('"', startIndex);
        return startIndex > 0 && endIndex > startIndex ? commentField.substring(startIndex, endIndex) : "";
    }
}
