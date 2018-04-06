package net.kyma.gui.contenttree;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class RootElement extends BaseElement {
    private final String rootPath;
    public RootElement(String indexedPath) {
        rootPath = indexedPath;
        setName(indexedPath.substring(indexedPath.lastIndexOf('/') + 1));
    }

    @Override
    public String getPath()
    {
        return rootPath + getName();
    }

    @Override
    public void detachFromParent()
    {
        throw new UnsupportedOperationException("Cannot detach root");
    }
}
