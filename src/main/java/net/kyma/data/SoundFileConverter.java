package net.kyma.data;

import static net.kyma.EventType.DATA_CONVERT_FROM_DOC;
import static net.kyma.EventType.PLAYLIST_ADD_FILE;
import static net.kyma.data.PathUtils.normalizePath;
import static net.kyma.dm.SupportedField.SET;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.dm.SoundFile;
import net.kyma.dm.SupportedField;
import net.kyma.player.Format;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.id3.AbstractTagFrameBody;
import org.jaudiotagger.tag.id3.ID3v23Frame;
import org.jaudiotagger.tag.id3.ID3v23Tag;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTagField;
import pl.khuzzuk.messaging.Bus;

@Log4j2
public class SoundFileConverter {

    private static final String ID3_COMMENT_TAG_PREFIX = "Text=";

    public SoundFileConverter(Bus<EventType> bus) {
        bus.subscribingFor(PLAYLIST_ADD_FILE).<File>accept(f -> from(f, f.getParent())).subscribe();
        bus.subscribingFor(DATA_CONVERT_FROM_DOC)
              .<Collection<Document>, Collection<SoundFile>>mapResponse(docs -> docs.stream()
                    .map(this::from)
                    .collect(Collectors.toList()))
              .subscribe();
    }

    public SoundFile from(File file, String indexedPath) {
        SoundFile sound = new SoundFile();
        sound.setPath(normalizePath(file.getPath()));
        sound.setFormat(Format.forPath(file.getPath()));
        sound.setFileName(normalizePath(file.getName()));
        sound.setIndexedPath(normalizePath(indexedPath));

        Optional.ofNullable(MetadataConverter.getMetadataFrom(file)).ifPresent(m -> fillData(sound, m));
        return sound;
    }

    SoundFile from(Document document) {
        SoundFile soundFile = new SoundFile();
        SET.forEach(m -> m.getSetter().accept(soundFile, document.get(m.getName())));

        soundFile.setCounter(Optional.ofNullable(document.getField("counter"))
                .map(IndexableField::numericValue)
                .map(Number::intValue).orElse(0));

        soundFile.setLength(Optional.ofNullable(document.getField("length"))
                                    .map(IndexableField::numericValue)
                                    .map(Number::longValue)
                                    .orElse(0L));
        return soundFile;
    }

    private void fillData(SoundFile sound, Tag metadata) {
        SupportedField.SUPPORTED_TAG.forEach(f -> f.putTagValueToSoundFile(metadata, sound));

        String comment = getComment(metadata);
        if (comment.contains(":")) {
            String counter = comment.substring(0, comment.indexOf(':'));
            if (NumberUtils.isDigits(counter)) {
                sound.setCounter(NumberUtils.toInt(counter));
            }
        }
    }

    private String getComment(Tag metadata) {
        List<TagField> commFields = metadata.getFields(FieldKey.COMMENT);
        if (commFields.isEmpty()) return "";

        if (metadata instanceof ID3v23Tag)
        {
            return commFields.stream()
                  .filter(frame -> frame instanceof ID3v23Frame)
                  .map(ID3v23Frame.class::cast)
                  .map(ID3v23Frame::getBody)
                  .filter(body -> body.getBriefDescription().contains(ID3_COMMENT_TAG_PREFIX))
                  .findFirst()
                  .map(AbstractTagFrameBody::getUserFriendlyValue)
                  .orElse("");
        }

        TagField tagField = commFields.get(commFields.size() - 1);
        if (tagField instanceof VorbisCommentTagField) {
            return tagField.toString();
        }

        return commFields.stream()
              .map(TagField::toString)
              .filter(text -> text.contains(ID3_COMMENT_TAG_PREFIX))
              .findFirst()
              .map(value -> value.substring(value.indexOf(ID3_COMMENT_TAG_PREFIX) + 6, value.lastIndexOf('"')))
              .orElse("");
    }
}
