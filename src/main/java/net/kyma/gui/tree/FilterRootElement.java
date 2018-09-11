package net.kyma.gui.tree;

import lombok.EqualsAndHashCode;
import net.kyma.dm.DataQuery;

@EqualsAndHashCode(callSuper = true)
public class FilterRootElement extends BaseElement {
    @Override
    void applyTo(DataQuery query)
    {
        //nothing to query
    }
}
