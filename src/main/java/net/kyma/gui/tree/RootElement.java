package net.kyma.gui.tree;

import lombok.EqualsAndHashCode;
import net.kyma.data.QueryParameters;

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
        return getName();
    }

    @Override
    public String getFullPath()
    {
        return rootPath + getName();
    }

    @Override
    public void detachFromParent()
    {
        throw new UnsupportedOperationException("Cannot detach root");
    }

    @Override
    public QueryParameters toQuery()
    {
        return QueryParameters.builder().build();
    }
}
