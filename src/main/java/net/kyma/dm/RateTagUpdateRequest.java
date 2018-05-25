package net.kyma.dm;

public class RateTagUpdateRequest extends TagUpdateRequest {
   private Rating value;
   public RateTagUpdateRequest(Rating value)
   {
      super(SupportedField.RATE, value.name());
      this.value = value;
   }

   @Override
   public SoundFile update(SoundFile file)
   {
      file.setRate(value);
      return file;
   }
}
