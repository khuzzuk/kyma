package net.kyma;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import net.kyma.dm.SoundFile;
import net.kyma.dm.SupportedField;
import net.kyma.gui.SoundFileEditor;
import net.kyma.gui.controllers.ContentView;
import net.kyma.gui.controllers.ManagerPaneController;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class GuiPrivateFields {
   public static Map<SupportedField, Collection<String>> contentViewSuggestions;
   public static SoundFileEditor contentViewSoundFileEditor;
   public static TableView<SoundFile> mainContentView;
   public static TableView<SoundFile> playlistView;

   @After("set(java.util.Map net.kyma.gui.controllers.ContentView.suggestions) && args(suggestions)")
   public void setContentViewSuggestions(Map<SupportedField, Collection<String>> suggestions) {
      contentViewSuggestions = suggestions;
   }

   @After("set(net.kyma.gui.SoundFileEditor net.kyma.gui.controllers.ContentView.editor) && args(editor)")
   public void setContentViewSoundFileEditor(SoundFileEditor editor) {
      contentViewSoundFileEditor = editor;
   }

   @After("execution(void net.kyma.gui.controllers.ContentView.initialize(..)) && this(contentView)")
   public void setMainContentView(ContentView contentView) {
      mainContentView = getPrivateFieldValue("mainContentView", contentView);
   }

   @After("execution(void net.kyma.gui.controllers.ManagerPaneController.initialize(..)) && this(controller)")
   public void setPlaylistView(ManagerPaneController controller) {
      playlistView = getPrivateFieldValue("playlist", controller);
   }

   private static <T> T getPrivateFieldValue(String fieldName, Object object) {
      try {
         Field mainContentViewField = object.getClass().getDeclaredField(fieldName);
         mainContentViewField.setAccessible(true);
         return (T) mainContentViewField.get(object);
      } catch (IllegalAccessException | NoSuchFieldException e) {
         e.printStackTrace();
         return null;
      }
   }
}
