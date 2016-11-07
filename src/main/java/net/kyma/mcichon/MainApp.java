package net.kyma.mcichon;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.kyma.mcichon.model.ItemModel;
import net.kyma.mcichon.view.controllers.MainEditDialogController;
import net.kyma.mcichon.view.controllers.MusicOverviewController;

import java.io.IOException;

public class MainApp extends Application {

    private Stage       primaryStage;
    private BorderPane  rootLayout;

    /**
     * The data as an observable list of Items.
     */
    private ObservableList< ItemModel > itemMusic = FXCollections.observableArrayList();

    /**
     * Constructor
     */
    public MainApp() {
        String name = Thread.currentThread().getName();
        System.out.println("FXLifeCycleApp() constructor: " + name);

        // Add some sample data (music)
        itemMusic.add( new ItemModel( "Side to side" ,               "3:20" ) );
        itemMusic.add( new ItemModel( "Treat you better" ,           "2:50" ) );
        itemMusic.add( new ItemModel( "This is what you came for" ,  "2:20" ) );
        itemMusic.add( new ItemModel( "Cornelia" ,                   "3:20" ) );
        itemMusic.add( new ItemModel( "Starboy"  ,                   "2:44" ) );
        itemMusic.add( new ItemModel( "We don't talk anymore" ,      "3:20" ) );
        itemMusic.add( new ItemModel( "All in my head" ,             "3:20" ) );
        itemMusic.add( new ItemModel( "Can't Stop the feeling" ,     "3:10" ) );
        itemMusic.add( new ItemModel( "Hold Up" ,                    "3:00" ) );
    }

    /**
     * Returns the data as an observable list of Items.
     * @return
     */
    public ObservableList< ItemModel > getItemData() {
        return itemMusic;
    }

    @Override
    public void start( Stage primaryStage ) {
        String name = Thread.currentThread().getName();
        System.out.println( "start() method: " + name );

        this.primaryStage = primaryStage;
        this.primaryStage.setTitle( "Mp3Fx" );

        // Set the application icon.
        //this.primaryStage.getIcons().add( new Image( "file:resources/images/address_book_32.png" ) );

        initRootLayout();

        showItemOverview();
    }

    /**
     * Initializes the root layout.
     */
    public void initRootLayout() {
        String name = Thread.currentThread().getName();
        System.out.println( "init() method: " + name );

        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation( MainApp.class.getResource( "/fxml/RootLayout.fxml" ) );
            rootLayout = ( BorderPane ) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene( rootLayout );
            primaryStage.setScene( scene );
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows the item overview inside the root layout.
     */
    public void showItemOverview() {
        String name = Thread.currentThread().getName();
        System.out.println("showItemOverview() method: " + name);

        try {
            // Load item overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation( MainApp.class.getResource( "/fxml/ItemOverview.fxml" ) );
            AnchorPane itemOverview = ( AnchorPane ) loader.load();

            // Set item overview into the center of root layout.
            rootLayout.setCenter( itemOverview );

            // Give the controller access to the main app.
            MusicOverviewController controller = loader.getController();
            controller.setMainApp( this );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens a dialog to edit details for the specified item. If the user
     * clicks OK, the changes are saved into the provided item object and true
     * is returned.
     *
     * @param itemModel the item object to be edited
     * @return true if the user clicked OK, false otherwise.
     */
    public boolean showItemEditDialog( ItemModel itemModel ) {
        String name = Thread.currentThread().getName();
        System.out.println( "showItemEditDialog() method: " + name );

        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation( MainApp.class.getResource( "/fxml/ItemEditDialog.fxml" ) );
            AnchorPane page = ( AnchorPane ) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle        ("Edit Item");
            dialogStage.initModality    ( Modality.WINDOW_MODAL );
            dialogStage.initOwner       ( primaryStage );
            Scene scene = new Scene     ( page );
            dialogStage.setScene        ( scene );

            // Set the item into the controller.
            MainEditDialogController controller = loader.getController();
            controller.setDialogStage( dialogStage );
            controller.setItem( itemModel );

            // Set the dialog icon.
            //dialogStage.getIcons().add(new Image( "file:resources/images/edit.png" ) );

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns the main stage.
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void stop() {
        String name = Thread.currentThread().getName();
        System.out.println("stop() method: " + name);
    }
}