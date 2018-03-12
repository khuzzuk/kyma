package net.kyma.properties;

import lombok.Data;

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
      data.uiProperties = uiProperties;

      PlayerProperties playerProperties = new PlayerProperties();
      playerProperties.setVolume(100);
      data.playerProperties = playerProperties;
      return data;
   }
}
