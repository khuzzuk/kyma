package net.kyma.gui.tree;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.kyma.dm.SoundFile;

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
        if (!(child instanceof SoundElement) && !getChildren().contains(child)) {
            getChildren().add(child);
            getChildren().sort(Comparator.comparing(TreeItem::getValue));
        }
    }

    public void setName(String name) {
        this.name = name;
        this.setValue(name);
    }

    public BaseElement getChildElement(String name) {
        return childElements.get(name);
    }

    public boolean hasChild(String name) {
        return childElements.containsKey(name);
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

    public void fill(ObservableList<SoundFile> toFill) {
        toFill.clear();
        toFill.addAll(getChildElements().values()
             .stream().filter(e -> e instanceof SoundElement)
             .map(e -> (SoundElement) e)
             .map(SoundElement::getSoundFile)
             .collect(Collectors.toList()));
    }
}
