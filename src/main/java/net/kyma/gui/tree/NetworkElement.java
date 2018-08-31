package net.kyma.gui.tree;

import lombok.RequiredArgsConstructor;
import net.kyma.dm.DataQuery;
import net.kyma.gui.NetworkPopup;

@RequiredArgsConstructor
public class NetworkElement extends BaseElement {
    private final NetworkPopup networkPopup;

    @Override
    public void applyTo(DataQuery query) {
        //TODO replace logic
    }

    public void importFromNetwork() {
        networkPopup.show();
    }
}
