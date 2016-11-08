package net.kyma.mcichon.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ItemModel {

    private final StringProperty name;
    private final StringProperty time;

    public ItemModel() {
        this( null , null );
    }

    /**
     * Constructor with some initial data.
     * @param name
     * @param time
     */
    public ItemModel(  String name , String time   ) {
        this.name  = new SimpleStringProperty( name );
        this.time  = new SimpleStringProperty( time );
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set( name );
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getTime() {
        return time.get();
    }

    public void setTime(String time) {
        this.time.set( time );
    }

    public StringProperty timeProperty() {
        return time;
    }
}