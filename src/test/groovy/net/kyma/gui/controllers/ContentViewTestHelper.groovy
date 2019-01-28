package net.kyma.gui.controllers

import javafx.scene.control.TableView
import net.kyma.dm.SoundFile
import net.kyma.dm.SupportedField

class ContentViewTestHelper {
    static Map<SupportedField, Collection<String>> getSuggestions(ContentView contentView) {
        contentView.getClass().getDeclaredField('suggestions').get(contentView) as Map<SupportedField, Collection<String>>
    }

    static TableView<SoundFile> getMainContentView(ContentView contentView) {
        contentView.getClass().getDeclaredField('mainContentView').get(contentView) as TableView<SoundFile>
    }

    static boolean hasSuggestions(ContentView contentView) {
        contentView.getClass().getDeclaredField('suggestions').get(contentView) != null
    }

    static boolean hasEditor(ContentView contentView) {
        contentView.getClass().getDeclaredField('editor').get(contentView) != null
    }
}
