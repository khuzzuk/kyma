package net.kyma;

import java.util.Collection;
import java.util.Map;

import javafx.scene.control.ListView;
import net.kyma.dm.SupportedField;
import net.kyma.gui.SoundFileEditor;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class GuiPrivateFields {
   public static Map<SupportedField, Collection<String>> contentViewSuggestions;
   public static SoundFileEditor contentViewSoundFileEditor;

   @After("set(java.util.Map net.kyma.gui.controllers.ContentView.suggestions) && args(suggestions)")
   public void setContentViewSuggestions(Map<SupportedField, Collection<String>> suggestions) {
      contentViewSuggestions = suggestions;
   }

   @After("set(net.kyma.gui.SoundFileEditor net.kyma.gui.controllers.ContentView.editor) && args(editor)")
   public void setContentViewSoundFileEditor(SoundFileEditor editor) {
      contentViewSoundFileEditor = editor;
   }
}