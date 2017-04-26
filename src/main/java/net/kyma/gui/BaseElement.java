package net.kyma.gui;

import javafx.scene.control.TreeItem;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class BaseElement extends TreeItem<String> {
    private String name;
    private BaseElement parentElement;
    private Map<String, BaseElement> childElements;

    public BaseElement() {
        childElements = new HashMap<>();
    }

    public void addChild(BaseElement child) {
        childElements.put(child.getName(), child);
        if (!(child instanceof SoundElement)) {
            getChildren().add(child);
        }
    }

    public void setName(String name) {
        this.name = name;
        this.setValue(name);
    }

    public BaseElement getChildElement(String name) {
        return childElements.get(name);
    }

}
