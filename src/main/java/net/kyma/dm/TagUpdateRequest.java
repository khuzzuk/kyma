package net.kyma.dm;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TagUpdateRequest
{
   private SupportedField field;
   private String value;

   public SoundFile update(SoundFile file)
   {
      field.getSetter().accept(file, value);
      return file;
   }
}
