package net.kyma.gui.controllers

import javafx.scene.control.TableView
import net.kyma.dm.SoundFile
import net.kyma.dm.SupportedField
import net.kyma.gui.content.ContentPaneController

class ContentViewTestHelper {
    static Map<SupportedField, Collection<String>> getSuggestions(ContentPaneController contentView) {
        contentView.getClass().getDeclaredField('suggestions').get(contentView) as Map<SupportedField, Collection<String>>
    }

    static TableView<SoundFile> getMainContentView(ContentPaneController contentView) {
        contentView.getClass().getDeclaredField('mainContentView').get(contentView) as TableView<SoundFile>
    }

    static boolean hasSuggestions(ContentPaneController contentView) {
        contentView.getClass().getDeclaredField('suggestions').get(contentView) != null
    }

    static boolean hasEditor(ContentPaneController contentView) {
        contentView.getClass().getDeclaredField('editor').get(contentView) != null
    }
}
