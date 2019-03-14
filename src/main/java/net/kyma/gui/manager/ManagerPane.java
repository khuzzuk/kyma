package net.kyma.gui.manager;

import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeView;
import javafx.scene.layout.GridPane;
import net.kyma.dm.SoundFile;

public class ManagerPane extends GridPane {
    private final ManagerPaneController managerPaneController;

    private TreeView<String> filesList = new TreeView<>();
    private TableView<SoundFile> contentView = new TableView<>();
    private TableView<SoundFile> playlist = new TableView<>();
    private ListView<String> moodFilter = new ListView<>();
    private ListView<String> genreFilter = new ListView<>();
    private ListView<String> occasionFilter = new ListView<>();

    private GridPane contentViewGrid = new GridPane();
    private SplitPane mainSplitPane = new SplitPane();

    public ManagerPane(ManagerPaneController managerPaneController) {
        this.managerPaneController = managerPaneController;

        add(filesList, 0, 0);
        add(mainSplitPane, 1, 0);
        add(playlist, 2, 0);
    }
}
