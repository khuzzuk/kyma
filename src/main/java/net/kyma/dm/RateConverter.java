package net.kyma.dm;

import static net.kyma.dm.Rating.getRatingBy;
import static org.apache.commons.lang3.math.NumberUtils.toInt;
import static org.jaudiotagger.tag.FieldKey.RATING;

import java.util.function.BiFunction;

import org.jaudiotagger.tag.Tag;

public class RateConverter implements BiFunction<Tag, SoundFile, String>
{
   @Override
   public String apply(Tag tag, SoundFile soundFile)
   {
      return getRatingBy(toInt(tag.getFirst(RATING), 0), soundFile.getFormat()).name();
   }
}
