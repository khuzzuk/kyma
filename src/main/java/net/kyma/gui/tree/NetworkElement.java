package net.kyma.gui.tree;

import net.kyma.EventType;
import net.kyma.dm.DataQuery;
import pl.khuzzuk.messaging.Bus;

class NetworkElement extends BaseElement {
    public static NetworkElement from(BaseElement baseElement) {
        NetworkElement networkElement = new NetworkElement();
        networkElement.setName(baseElement.getName());
        networkElement.setParentElement(baseElement.getParentElement());
        baseElement.getChildElements().values()
                .forEach(e -> networkElement.addChild(from(e)));
        return networkElement;
    }

    @Override
    public void onClick(Bus<EventType> bus, DataQuery dataQuery) {
        bus.message(EventType.DATA_WEB_DOWNLOADS_QUERY)
                .withContent(getName())
                .withResponse(EventType.DATA_QUERY_RESULT_FOR_CONTENT_VIEW)
                .send();
    }
}
