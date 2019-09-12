package net.kyma.dm;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.kyma.player.Format;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import pl.khuzzuk.functions.Validators;

import java.util.Collections;
import java.util.EnumSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.apache.commons.lang3.StringUtils.isNoneBlank;

@Getter
@Log4j2
public enum SupportedField
{
   PATH("path", null, SoundFile::getPath, SoundFile::setPath),
   INDEXED_PATH("indexedPath", null, SoundFile::getIndexedPath, SoundFile::setIndexedPath),
   FORMAT("format", null, s -> s.getFormat().name(), (s, v) -> s.setFormat(Format.forPath(s.getPath()))),
   FILE_NAME("fileName", null, SoundFile::getFileName, SoundFile::setFileName),
   COUNTER("playCounter", null, s -> String.valueOf(s.getCounter()),
         (s, v) -> s.setCounter(NumberUtils.toInt(v))),
   TITLE("title", FieldKey.TITLE, SoundFile::getTitle, SoundFile::setTitle),
   YEAR("year", FieldKey.YEAR, SoundFile::getDate, SoundFile::setDate, StringUtils::isNoneBlank),
   ALBUM("album", FieldKey.ALBUM, SoundFile::getAlbum, SoundFile::setAlbum),
   ALBUM_ARTIST("albumArtist", FieldKey.ALBUM_ARTIST, SoundFile::getAlbumArtist, SoundFile::setAlbumArtist),
   ALBUM_ARTISTS("albumArtists", FieldKey.ALBUM_ARTISTS, SoundFile::getAlbumArtists, SoundFile::setAlbumArtists),
   ARTIST("artist", FieldKey.ARTIST, SoundFile::getArtist, SoundFile::setArtist),
   ARTISTS("artists", FieldKey.ARTISTS, SoundFile::getArtists, SoundFile::setArtists),
   COMPOSER("composer", FieldKey.COMPOSER, SoundFile::getComposer, SoundFile::setComposer),
   CONDUCTOR("conductor", FieldKey.CONDUCTOR, SoundFile::getConductor, SoundFile::setConductor),
   COUNTRY("country", FieldKey.COUNTRY, SoundFile::getCountry, SoundFile::setCountry),
   CUSTOM1("custom1", FieldKey.CUSTOM1, SoundFile::getCustom1, SoundFile::setCustom1),
   CUSTOM2("custom2", FieldKey.CUSTOM2, SoundFile::getCustom2, SoundFile::setCustom2),
   CUSTOM3("custom3", FieldKey.CUSTOM3, SoundFile::getCustom3, SoundFile::setCustom3),
   CUSTOM4("custom4", FieldKey.CUSTOM4, SoundFile::getCustom4, SoundFile::setCustom4),
   CUSTOM5("custom5", FieldKey.CUSTOM5, SoundFile::getCustom5, SoundFile::setCustom5),
   DISC_NO("discNo", FieldKey.DISC_NO, SoundFile::getDiscNo, SoundFile::setDiscNo, NumberUtils::isDigits),
   GENRE("genre", FieldKey.GENRE, SoundFile::getGenre, SoundFile::setGenre),
   GROUP("group", FieldKey.GROUP, SoundFile::getGroup, SoundFile::setGroup),
   INSTRUMENT("instrument", FieldKey.INSTRUMENT, SoundFile::getInstrument, SoundFile::setInstrument),
   MOVEMENT("movement", FieldKey.MOVEMENT, SoundFile::getMovement, SoundFile::setMovement),
   OCCASION("occasion", FieldKey.OCCASION, SoundFile::getOccasion, SoundFile::setOccasion),
   OPUS("opus", FieldKey.OPUS, SoundFile::getOpus, SoundFile::setOpus),
   ORCHESTRA("orchestra", FieldKey.ORCHESTRA, SoundFile::getOrchestra, SoundFile::setOrchestra),
   QUALITY("quality", FieldKey.QUALITY, SoundFile::getQuality, SoundFile::setQuality),
   RANKING("ranking", FieldKey.RANKING, SoundFile::getRanking, SoundFile::setRanking),
   TEMPO("tempo", FieldKey.TEMPO, SoundFile::getTempo, SoundFile::setTempo),
   TONALITY("tonality", FieldKey.TONALITY, SoundFile::getTonality, SoundFile::setTonality),
   TRACK("track", FieldKey.TRACK, SoundFile::getTrack, SoundFile::setTrack),
   WORK("work", FieldKey.WORK, SoundFile::getWork, SoundFile::setWork),
   WORK_TYPE("workType", FieldKey.WORK_TYPE, SoundFile::getWorkType, SoundFile::setWorkType),
   MOOD("mood", FieldKey.MOOD, SoundFile::getMood, SoundFile::setMood, Validators.nullSafe(), new MoodConverter()),

   // Not in Supported Tags, but implements tag setter
   RATE("rate", FieldKey.RATING, s -> s.getRate() != null ? s.getRate().name() : Rating.UNDEFINED.name(),
         (s, v) -> s.setRate(isNoneBlank(v) ? Rating.valueOf(v) : Rating.UNDEFINED), Validators.alwaysTrue(), new RateConverter())
         {
            @Override
            public void setField(Tag tag, SoundFile soundFile)
            {
               tag.deleteField(getMappedKey());
               try
               {
                  tag.setField(getMappedKey(), String.valueOf(soundFile.getRateValue()));
               }
               catch (FieldDataInvalidException e)
               {
                  log.error("Cannot set Field " + getMappedKey().name() + " with value " + soundFile.getRateValue());
                  log.error(e);
               }
            }
         };

   private final String name;
   private final FieldKey mappedKey;
   private final Function<SoundFile, String> getter;
   private final BiConsumer<SoundFile, String> setter;
   private Predicate<String> validator;

   private BiFunction<Tag, SoundFile, String> tagExtractor = (tag, soundfile) -> tag.getFirst(getMappedKey());

   public static final Set<SupportedField> SET = Collections.unmodifiableSet(EnumSet.allOf(SupportedField.class));
   public static final Set<SupportedField> SUPPORTED_TAG = Collections.unmodifiableSet(EnumSet.of(
         TITLE, YEAR, ALBUM, ALBUM_ARTIST, ALBUM_ARTISTS, ARTIST, ARTISTS, COMPOSER, CONDUCTOR, COUNTRY,
         CUSTOM1, CUSTOM2, CUSTOM3, CUSTOM4, CUSTOM5, DISC_NO, GENRE, GROUP, INSTRUMENT, MOOD, MOVEMENT,
         OCCASION, OPUS, ORCHESTRA, QUALITY, RANKING, TEMPO, TONALITY, TRACK, WORK, WORK_TYPE, RATE));

   SupportedField(String name, FieldKey mappedKey,
         Function<SoundFile, String> getter, BiConsumer<SoundFile, String> setter)
   {
      this(name, mappedKey, getter, setter, Validators.nullSafe());
   }

   SupportedField(String name, FieldKey mappedKey,
         Function<SoundFile, String> getter, BiConsumer<SoundFile, String> setter,
         Predicate<String> validator)
   {
      this.name = name;
      this.mappedKey = mappedKey;
      this.getter = getter;
      this.setter = setter;
      this.validator = validator;
   }

   SupportedField(String name, FieldKey mappedKey,
         Function<SoundFile, String> getter, BiConsumer<SoundFile, String> setter,
         Predicate<String> validator, BiFunction<Tag, SoundFile, String> tagExtractor)
   {
      this(name, mappedKey, getter, setter, validator);
      this.tagExtractor = tagExtractor;
   }

   public void setField(Tag tag, SoundFile soundFile)
   {
      String value = getter.apply(soundFile);
      try
      {
         if (validator.test(value))
         {
            tag.setField(mappedKey, value);
         }
      }
      catch (FieldDataInvalidException e)
      {
         log.error("Cannot set Field " + mappedKey.name() + " with value " + value);
         log.error(e);
      }
   }

   public void putTagValueToSoundFile(Tag tag, SoundFile soundFile)
   {
      getSetter().accept(soundFile, tagExtractor.apply(tag, soundFile));
   }

   public static SupportedField getByName(String name)
   {
      return SET.stream().filter(field -> field.name.equals(name)).findAny()
            .orElseThrow(() -> new NoSuchElementException(name));
   }
}
