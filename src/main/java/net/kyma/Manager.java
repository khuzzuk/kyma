package net.kyma;

import java.io.FileNotFoundException;

import javafx.application.Application;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;
import net.kyma.gui.MainWindow;
import net.kyma.gui.controllers.ControllerDistributor;
import pl.khuzzuk.functions.ForceGate;
import pl.khuzzuk.messaging.Bus;

@Log4j2
public class Manager extends Application {
    static Bus<EventType> bus;
    static MainWindow mainWindow;
    static Stage currentStage;
    private static ForceGate gate = ForceGate.of(2, Manager::initMainWindow);

   public static void main(String[] args)
    {
        bus = createBus();
        prepareApp("index/", bus);
        launch(args);
    }

    static void prepareApp(String indexingPath, Bus<EventType> bus) {
       bus.subscribingFor(EventType.RET_CONTROLLER_DISTRIBUTOR).onFXThread()
             .<ControllerDistributor>accept(controllerDistributor -> {
          mainWindow = new MainWindow(bus, controllerDistributor);
          gate.on();
       }).subscribe();

       ObjectContainer container = new ObjectContainer(bus);
       container.createContainer(indexingPath);
    }

    @Override
    public void start(Stage primaryStage) {
       currentStage = primaryStage;
       gate.on();
    }

   private static void initMainWindow() {
      mainWindow.initMainWindow(currentStage);
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
