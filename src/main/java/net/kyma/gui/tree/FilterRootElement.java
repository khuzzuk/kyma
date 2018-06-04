package net.kyma.gui.tree;

import lombok.EqualsAndHashCode;
import net.kyma.dm.DataQuery;

@EqualsAndHashCode(callSuper = true)
public class FilterRootElement extends BaseElement {
    @Override
    public DataQuery toQuery() {
        return DataQuery.empty();
    }
}
