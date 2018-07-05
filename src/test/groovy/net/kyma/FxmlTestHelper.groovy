package net.kyma

import javafx.application.Platform
import javafx.event.Event
import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.scene.control.TableView
import javafx.scene.control.TreeView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import net.kyma.dm.SoundFile
import spock.lang.Specification

import java.lang.reflect.Field

abstract class FxmlTestHelper extends Specification {
    void initFxmlFor(def object) {
        for (Field field : object.getClass().getDeclaredFields()) {
            if (field.getDeclaredAnnotation(FXML.class) != null) {
                field.setAccessible(true)
                if (field.get(object) != null) {
                    try {
                        def mock = field.getType().newInstance()
                        field.set(object, mock)
                    } catch (Exception e) {
                        println '\n\n\n\n'
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    void clickMouseOn(javafx.scene.Node node, int x, int y) {
        clickMouseOn(node, x, y, 1)
    }

    void clickMouseOn(javafx.scene.Node node, int x, int y, int clickCount) {
        Event.fireEvent(node, new MouseEvent(
                MouseEvent.MOUSE_CLICKED, x, y, x, y, MouseButton.PRIMARY, clickCount,
                false, false, false, false, false,
                false, false, false,
                false, false, null))
    }

    void fireEventOn(javafx.scene.Node node, KeyCode keyCode, boolean shift, boolean control, boolean alt) {
        Platform.runLater({
            Event.fireEvent(node, new KeyEvent(KeyEvent.KEY_RELEASED, '', '', keyCode,
                    shift, control, alt, false))
        })
    }

    void selectFirst(TreeView<?> treeview) {
        treeview.getRoot().expanded = true
        treeview.getRoot().getChildren().forEach({ it.expanded = true })
        treeview.selectionModel.select(2)
    }

    void selectFirst(TableView<?> tableView) {
        tableView.selectionModel.select(0)
    }

    void select(ListView<?> listView, int item) {
        listView.selectionModel.select(item)
    }

    void selectBySoundFilePath(TableView<SoundFile> tableView, SoundFile byPath) {
        tableView.selectionModel.select(byPath)
    }

    boolean hasValue(Object o, String fieldName) {
        def field = o.getClass().getDeclaredField(fieldName)
        field.setAccessible(true)
        field.get(o) != null
    }
}
