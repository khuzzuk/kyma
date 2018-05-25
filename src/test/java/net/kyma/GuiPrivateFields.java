package net.kyma;

import java.util.Collection;
import java.util.Map;

import javafx.scene.control.ListView;
import net.kyma.dm.SupportedField;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class GuiPrivateFields {
   public static Map<SupportedField, Collection<String>> contentViewSuggestions;
   public static ListView<String> managerPaneMoodFilter;

   @After("set(java.util.Map net.kyma.gui.controllers.ContentView.suggestions) && args(suggestions)")
   public void setContentViewSuggestions(Map<SupportedField, Collection<String>> suggestions) {
      contentViewSuggestions = suggestions;
   }
}
