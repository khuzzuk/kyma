package net.kyma.gui.tree;

import lombok.EqualsAndHashCode;
import net.kyma.data.QueryParameters;
import net.kyma.dm.SupportedField;

@EqualsAndHashCode(callSuper = true)
public class FilterRootElement extends BaseElement {
    @Override
    public QueryParameters toQuery() {
        return QueryParameters.builder().build();
    }
}
