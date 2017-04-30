[1mdiff --git a/src/main/java/net/kyma/properties/PropertiesManager.java b/src/main/java/net/kyma/properties/PropertiesManager.java[m
[1mindex 3d7fd02..2e5da87 100644[m
[1m--- a/src/main/java/net/kyma/properties/PropertiesManager.java[m
[1m+++ b/src/main/java/net/kyma/properties/PropertiesManager.java[m
[36m@@ -1,14 +1,16 @@[m
 package net.kyma.properties;[m
 [m
[31m-import lombok.Getter;[m
[31m-import lombok.Setter;[m
[32m+[m[32mimport org.apache.commons.lang3.math.NumberUtils;[m
[32m+[m[32mimport org.jcp.xml.dsig.internal.dom.ApacheCanonicalizer;[m
 import pl.khuzzuk.messaging.Bus;[m
 [m
 import javax.inject.Inject;[m
 import javax.inject.Named;[m
[32m+[m
 import java.io.FileOutputStream;[m
 import java.io.IOException;[m
 import java.io.OutputStream;[m
[32m+[m
 import java.util.Properties;[m
 [m
 public class PropertiesManager {[m
[36m@@ -18,7 +20,11 @@[m [mpublic class PropertiesManager {[m
     @Inject[m
     @Named("userProperties")[m
     private Properties properties;[m
[31m-    [m
[32m+[m
[32m+[m[32m    @Inject[m
[32m+[m[32m    @Named("messages")[m
[32m+[m[32m    private Properties messages;[m
[32m+[m
     private String lastAlbum;[m
     private String volume;[m
     private String musicPosition;[m
[36m@@ -52,4 +58,19 @@[m [mpublic class PropertiesManager {[m
             }[m
         }[m
     }[m
[32m+[m
[32m+[m[32m    // TODO: Dimension?[m
[32m+[m[32m    public void initializationDimension() {[m
[32m+[m[32m        //bus.setResponse("", );[m
[32m+[m[32m    }[m
[32m+[m
[32m+[m[32m    public void windowDimension() {[m
[32m+[m[32m        String x = messages.getProperty("player.window.dimension.set.x");[m
[32m+[m[32m        String y = messages.getProperty("player.window.dimension.set.y");[m
[32m+[m[32m        if(NumberUtils.isDigits(x) && NumberUtils.isDigits(y))[m
[32m+[m[32m            if (NumberUtils.isParsable(x) && NumberUtils.isParsable(y)){[m
[32m+[m[32m                int x1 = NumberUtils.toInt(x);[m
[32m+[m[32m                int y1 = NumberUtils.toInt(y);[m
[32m+[m[32m            }[m
[32m+[m[32m    }[m
 }[m
[1mdiff --git a/src/main/resources/messages.properties b/src/main/resources/messages.properties[m
[1mindex 74dd8a8..54548be 100644[m
[1m--- a/src/main/resources/messages.properties[m
[1m+++ b/src/main/resources/messages.properties[m
[36m@@ -25,4 +25,8 @@[m [mdata.index.gui.amount=data.index.gui.amount[m
 data.index.gui.progress=data.index.gui.progress[m
 data.index.gui.finish=data.index.gui.finish[m
 [m
[31m-data.view.refresh=data.view.refresh[m
\ No newline at end of file[m
[32m+[m[32mdata.view.refresh=data.view.refresh[m
[32m+[m
[32m+[m[32mplayer.window.dimension.set.x=[m
[32m+[m[32mplayer.window.dimension.set.y=[m
[32m+[m[32mplayer.window.dimension.get=[m
\ No newline at end of file[m
