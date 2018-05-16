package net.kyma

import groovy.transform.CompileStatic
import javafx.event.Event
import javafx.fxml.FXML
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import spock.lang.Specification

import java.lang.reflect.Field

@CompileStatic
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

    void fireEventOn(javafx.scene.Node node, int x, int y) {
        Event.fireEvent(node, new MouseEvent(
                MouseEvent.MOUSE_CLICKED, x, y, x, y, MouseButton.PRIMARY, 1,
                false, false, false, false, false,
                false, false, false,
                false, false, null))
    }
}
