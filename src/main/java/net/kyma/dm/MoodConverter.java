package net.kyma.dm;

import java.util.Optional;
import java.util.function.BiFunction;

import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.id3.AbstractTagFrame;
import org.jaudiotagger.tag.id3.AbstractTagFrameBody;
import org.jaudiotagger.tag.id3.ID3v23Frame;

public class MoodConverter implements BiFunction<Tag, SoundFile, String>
{
   @Override
   public String apply(Tag tag, SoundFile soundFile)
   {
      return Optional.ofNullable(tag.getFirst(FieldKey.MOOD))
            .filter(StringUtils::isNoneBlank)
            .orElseGet(() -> getFromComment(tag));
   }

   private String getFromComment(Tag tag)
   {
      return tag.getFields(FieldKey.COMMENT).stream()
            .filter(rawTag -> rawTag instanceof ID3v23Frame)
            .map(ID3v23Frame.class::cast)
            .map(AbstractTagFrame::getBody)
            .filter(body -> body.getBriefDescription().contains("MusicMatch_Mood"))
            .map(AbstractTagFrameBody::getUserFriendlyValue)
            .findAny().orElse(null);
   }
}
