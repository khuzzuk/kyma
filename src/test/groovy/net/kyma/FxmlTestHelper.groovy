package net.kyma

import groovy.transform.CompileStatic
import javafx.fxml.FXML
import spock.lang.Specification

import java.lang.reflect.Field

@CompileStatic
abstract class FxmlTestHelper extends Specification {
    void initFxmlFor(def object) {
        for (Field field : object.getClass().getDeclaredFields()) {
            if (field.getDeclaredAnnotation(FXML.class) != null) {
                Mock(field.getDeclaringClass())
            }
        }
    }
}
