package net.kyma.gui.tree;

import javafx.scene.control.TreeItem;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.kyma.dm.DataQuery;
import net.kyma.dm.SupportedField;

import java.util.*;

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
        childElements.putIfAbsent(child.getName(), child);
        if (!getChildren().contains(child)) {
            getChildren().add(child);
            getChildren().sort(Comparator.comparing(TreeItem::getValue));
        }
    }

    public void setName(String name) {
        this.name = name;
        this.setValue(name);
    }

    private void removeChildElementBy(String name) {
        getChildren().remove(childElements.get(name));
        childElements.remove(name);
    }

    public BaseElement getChildElement(String name) {
        return childElements.get(name);
    }

    public boolean hasChild(String name) {
        return childElements.containsKey(name);
    }

    public void update(BaseElement updates) {
        Collection<String> toRemove = new LinkedList<>();
        for (Map.Entry<String, BaseElement> childNode : childElements.entrySet()) {
            if (updates.childElements.containsKey(childNode.getKey())) {
                childNode.getValue().update(updates.getChildElement(childNode.getKey()));
                updates.removeChildElementBy(childNode.getKey());
            } else {
                toRemove.add(childNode.getKey());
            }
        }
        toRemove.forEach(this::removeChildElementBy);
        for (BaseElement elementToInsert : updates.childElements.values()) {
            addChild(elementToInsert);
        }
    }

    public String getPath() {
        return parentElement.getPath() + "/" + name;
    }

    public String getFullPath() {
        return parentElement.getFullPath() + "/" + name;
    }

    public String getIndexingPath() {
        return parentElement.getIndexingPath();
    }

    public void applyTo(DataQuery query) {
        query.and(SupportedField.PATH, "*" + getPath() + "/*", true)
              .and(SupportedField.INDEXED_PATH, getIndexingPath(), false);
    }
}
