package net.kyma.data;

import lombok.Builder;
import lombok.Getter;
import net.kyma.EventType;
import net.kyma.dm.SupportedField;

@Getter
@Builder
public class QueryParameters {
   private SupportedField field;
   private String value;
   private EventType returnTopic;
}
