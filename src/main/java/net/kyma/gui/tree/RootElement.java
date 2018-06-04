package net.kyma.gui.tree;

import lombok.EqualsAndHashCode;
import net.kyma.dm.DataQuery;

@EqualsAndHashCode(callSuper = true)
public class RootElement extends BaseElement {
    private final String rootPath;

    public RootElement(String indexedPath) {
        rootPath = indexedPath;
        setName(indexedPath.substring(indexedPath.lastIndexOf('/') + 1));
    }

    @Override
    public String getPath() {
        return getName();
    }

    @Override
    public String getFullPath() {
        return getName();
    }

    @Override
    public String getIndexingPath() {
        return rootPath;
    }

    @Override
    public void detachFromParent() {
        throw new UnsupportedOperationException("Cannot detach root");
    }

    @Override
    public boolean hasParent()
    {
        return false;
    }

    @Override
    public DataQuery toQuery() {
        return DataQuery.empty();
    }
}
