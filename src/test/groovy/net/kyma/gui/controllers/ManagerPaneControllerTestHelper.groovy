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
    TreeView<String> filesList

    void retrieveFields(ManagerPaneController managerPaneController) {
        this.managerPaneController = managerPaneController
        if (managerPaneController == null) false

        moodFilter = managerPaneController.moodFilter
        contentView = managerPaneController.contentView
        filesList = managerPaneController.filesList

        moodFilter != null &&
                contentView != null &&
                filesList != null
    }

    boolean isValid() {
        moodFilter != null &&
                contentView != null &&
                filesList != null
    }
}
