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
    static Bus<EventType> bus;
    private static MainWindow mainWindow;
    private static ControllerDistributor controllerDistributor;
    private static MainController mainController;

   public static void main(String[] args)
    {
        bus = createBus();
        prepareApp("index/", bus);
        Platform.runLater(() -> mainWindow = new MainWindow(bus, controllerDistributor, mainController));
        launch(args);
    }

    static void prepareApp(String indexingPath, Bus<EventType> bus) {
       TableColumnFactory columnFactory = new TableColumnFactory(bus);
       ManagerPaneController managerPaneController = new ManagerPaneController(bus, columnFactory);
       mainController = new MainController(bus, managerPaneController);

       PlayerPaneController playerPaneController = new PlayerPaneController(bus);

       ObjectContainer container = new ObjectContainer(bus);
       container.createContainer(indexingPath);

       ContentView contentView = new ContentView(bus, columnFactory);
       container.putToContainer(EventType.RET_CONTENT_VIEW, contentView);

       controllerDistributor = new ControllerDistributor(
             mainController,
             playerPaneController,
             managerPaneController,
             contentView);

    }

    @Override
    public void start(Stage primaryStage)
    {
        mainWindow.initMainWindow(primaryStage);
        mainWindow.show();
    }

    static Bus<EventType> createBus()
    {
        try {
            BusLogger busLogger = new BusLogger();
            Bus<EventType> bus = Bus.initializeBus(EventType.class, busLogger, true);
            busLogger.setBus(bus);
            return bus;
        } catch (FileNotFoundException e) {
            log.error("Cannot start bus, try starting with System out logger", e);
            return Bus.initializeBus(EventType.class, System.out, true); //NOSONAR
        }
    }
}
