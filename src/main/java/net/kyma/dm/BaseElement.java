package net.kyma.dm;

import javafx.scene.control.TreeItem;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

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
        getChildren().add(child);
    }

    public BaseElement getChildElement(String name) {
        return childElements.get(name);
    }
}