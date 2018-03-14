package net.kyma.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.kyma.dm.SupportedField;

@Data
public class UIProperties
{
   private boolean fullScreen;
   private boolean maximized;
   private int x;
   private int y;
   private int width;
   private int height;
   private List<ColumnDefinition> columnDefinitions;

   void add(ColumnDefinition... definitions)
   {
      if (columnDefinitions == null)
      {
         columnDefinitions = new ArrayList<>();
      }
      Collections.addAll(columnDefinitions, definitions);
   }

   @Data
   @AllArgsConstructor
   @NoArgsConstructor
   public static class ColumnDefinition
   {
      private SupportedField field;
      private double size;
   }
}
