package net.kyma.gui;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.control.TreeItem;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode(of = "name", callSuper = false)
@ToString(of = "name")
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

    public boolean isBranch()
    {
        return true;
    }

    public String getPath()
    {
        return parentElement.getPath() + "/" + name;
    }

    public void detachFromParent()
    {
        parentElement.childElements.remove(name);
        parentElement = null;
    }
}
