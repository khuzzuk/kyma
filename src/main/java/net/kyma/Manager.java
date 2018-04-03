package net.kyma;

import java.io.FileNotFoundException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;
import net.kyma.gui.MainWindow;
import net.kyma.gui.TableColumnFactory;
import net.kyma.gui.controllers.ContentView;
import net.kyma.gui.controllers.ControllerDistributor;
import net.kyma.gui.controllers.MainController;
import net.kyma.gui.controllers.ManagerPaneController;
import net.kyma.gui.controllers.PlayerPaneController;
import pl.khuzzuk.messaging.Bus;

@Log4j2
public class Manager extends Application {

    private static MainWindow mainWindow;

    public static void main(String[] args)
    {
        Bus<EventType> bus = createBus();

        //GUI
        TableColumnFactory columnFactory = new TableColumnFactory(bus);
        ManagerPaneController managerPaneController = new ManagerPaneController(bus, columnFactory);
        MainController mainController = new MainController(bus, managerPaneController);

        PlayerPaneController playerPaneController = new PlayerPaneController(bus);

        ObjectContainer container = new ObjectContainer(bus);
        container.createContainer();

        ContentView contentView = new ContentView(bus, columnFactory);
        container.putToContainer(EventType.RET_CONTENT_VIEW, contentView);

        ControllerDistributor controllerDistributor = new ControllerDistributor(
              mainController,
              playerPaneController,
              managerPaneController,
              contentView);

        Platform.runLater(() -> mainWindow = new MainWindow(bus, controllerDistributor, mainController));

        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        mainWindow.initMainWindow(primaryStage);
        mainWindow.show();
    }

    private static Bus<EventType> createBus()
    {
        try {
            return Bus.initializeBus(EventType.class, new BusLogger(), true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Bus.initializeBus(EventType.class, System.out, true);
        }
    }
}
