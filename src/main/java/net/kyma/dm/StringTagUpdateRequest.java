package net.kyma.dm;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StringTagUpdateRequest implements TagUpdateRequest
{
   private SupportedField field;
   private String value;

   @Override
   public SoundFile update(SoundFile file)
   {
      field.getSetter().accept(file, value);
      return file;
   }
}
