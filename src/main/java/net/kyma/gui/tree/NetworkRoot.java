package net.kyma.gui.tree;

import net.kyma.EventType;
import net.kyma.dm.DataQuery;
import net.kyma.gui.NetworkPopup;
import pl.khuzzuk.messaging.Bus;

public class NetworkRoot extends RootElement {
    private final NetworkPopup networkPopup;

    public NetworkRoot(NetworkPopup networkPopup) {
        super("Imported from Youtube");
        this.networkPopup = networkPopup;
    }

    @Override
    public void onClick(Bus<EventType> bus, DataQuery dataQuery) {
        networkPopup.show();
    }

    @Override
    public void update(BaseElement updates) {
        getChildElements().clear();
        addChild(NetworkElement.from(updates));
    }
}
