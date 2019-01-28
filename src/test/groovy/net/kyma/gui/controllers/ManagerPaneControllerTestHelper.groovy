package net.kyma.gui.controllers

import groovy.transform.CompileStatic
import javafx.scene.control.ListView
import javafx.scene.control.TableView
import javafx.scene.control.TreeView
import net.kyma.dm.SoundFile

@CompileStatic
class ManagerPaneControllerTestHelper {
    ManagerPaneController managerPaneController
    ListView<String> moodFilter
    TableView<SoundFile> contentView
    TableView<SoundFile> playList
    TreeView<String> filesList

    void retrieveFields(ManagerPaneController managerPaneController) {
        this.managerPaneController = managerPaneController
        if (managerPaneController == null) throw new IllegalArgumentException('controller cannot be null')

        moodFilter = managerPaneController.moodFilter
        contentView = managerPaneController.contentView
        playList = managerPaneController.playlist
        filesList = managerPaneController.filesList
    }

    boolean isValid() {
        moodFilter != null &&
                contentView != null &&
                filesList != null
    }
}
