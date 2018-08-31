package net.kyma.log;

import net.kyma.EventType;
import org.apache.commons.lang3.exception.ExceptionUtils;
import pl.khuzzuk.messaging.Bus;

public class Logger {
    public static void reportToUser(Bus<EventType> bus, Exception e) {
        bus.message(EventType.SHOW_ALERT).withContent(ExceptionUtils.getStackTrace(e)).send();
    }
}
