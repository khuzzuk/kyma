package net.kyma.gui.manager;

import javafx.geometry.Orientation;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import net.kyma.dm.SoundFile;

import static net.kyma.gui.components.GridPaneUtils.columnConstraints;
import static net.kyma.gui.components.GridPaneUtils.rowConstraints;

public class ManagerPane extends GridPane {

    private static final int FIRST_COL = 20;
    private static final int SECOND_COL = 60;
    private static final int THIRD_COL = 20;
    private static final double FILTERS_CONTENT_DIVISION = 0.2;

    public ManagerPane(ManagerPaneController managerPaneController, TableView<SoundFile> contentView) {
        getRowConstraints().addAll(rowConstraints(0));
        getColumnConstraints().addAll(columnConstraints(FIRST_COL), columnConstraints(SECOND_COL), columnConstraints(THIRD_COL));

        TreeView<String> filesList = new TreeView<>();
        filesList.setOnMouseClicked(event -> managerPaneController.requestUpdateContentView());
        filesList.setOnKeyReleased(managerPaneController::onKeyReleased);

        ListView<String> moodFilter = new ListView<>();
        moodFilter.setOnMouseClicked(event -> managerPaneController.requestUpdateContentView());
        HBox.setHgrow(moodFilter, Priority.ALWAYS);

        ListView<String> genreFilter = new ListView<>();
        genreFilter.setOnMouseClicked(event -> managerPaneController.requestUpdateContentView());
        HBox.setHgrow(genreFilter, Priority.ALWAYS);

        ListView<String> occasionFilter = new ListView<>();
        occasionFilter.setOnMouseClicked(event -> managerPaneController.requestUpdateContentView());
        HBox.setHgrow(occasionFilter, Priority.ALWAYS);

        HBox filters = new HBox(moodFilter, genreFilter, occasionFilter);

        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.setDividerPositions(FILTERS_CONTENT_DIVISION);
        mainSplitPane.setOrientation(Orientation.VERTICAL);
        mainSplitPane.getItems().addAll(filters, contentView);

        TableView<SoundFile> playlist = new TableView<>();
        playlist.setOnKeyReleased(managerPaneController::removeFromPlaylist);

        add(filesList, 0, 0);
        add(mainSplitPane, 1, 0);
        add(playlist, 2, 0);

        managerPaneController.setFilesList(filesList);
        managerPaneController.setContentView(contentView);
        managerPaneController.setPlaylist(playlist);
        managerPaneController.setMoodFilter(moodFilter);
        managerPaneController.setGenreFilter(genreFilter);
        managerPaneController.setOccasionFilter(occasionFilter);
        managerPaneController.initialize();
    }
}
