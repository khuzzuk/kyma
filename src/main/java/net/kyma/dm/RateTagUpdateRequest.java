package net.kyma.dm;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RateTagUpdateRequest implements TagUpdateRequest {
   private Rating value;

   @Override
   public SoundFile update(SoundFile file)
   {
      file.setRate(value);
      return file;
   }
}
