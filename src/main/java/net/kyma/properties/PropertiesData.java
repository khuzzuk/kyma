package net.kyma.properties;

import static net.kyma.dm.SupportedField.ALBUM;
import static net.kyma.dm.SupportedField.ARTIST;
import static net.kyma.dm.SupportedField.MOOD;
import static net.kyma.dm.SupportedField.OCCASION;
import static net.kyma.dm.SupportedField.RATE;
import static net.kyma.dm.SupportedField.TEMPO;
import static net.kyma.dm.SupportedField.TITLE;

import java.util.ArrayList;

import lombok.Data;
import net.kyma.properties.UIProperties.ColumnDefinition;

@Data
public class PropertiesData
{
   private UIProperties uiProperties;
   private PlayerProperties playerProperties;

   public static PropertiesData defaultProperties()
   {
      PropertiesData data = new PropertiesData();
      UIProperties uiProperties = new UIProperties();
      uiProperties.setX(100);
      uiProperties.setY(50);
      uiProperties.setWidth(600);
      uiProperties.setHeight(400);
      uiProperties.setColumnDefinitions(new ArrayList<>());
      uiProperties.add(
            new ColumnDefinition(TITLE, 100),
            new ColumnDefinition(RATE, 100),
            new ColumnDefinition(ALBUM, 100),
            new ColumnDefinition(ARTIST, 100),
            new ColumnDefinition(MOOD, 100),
            new ColumnDefinition(OCCASION, 100),
            new ColumnDefinition(TEMPO, 100));
      data.uiProperties = uiProperties;

      PlayerProperties playerProperties = new PlayerProperties();
      playerProperties.setVolume(100);
      data.playerProperties = playerProperties;
      return data;
   }
}
