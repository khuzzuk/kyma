package net.kyma.gui.tree;

import static net.kyma.EventType.DATA_QUERY_RESULT_FOR_CONTENT_VIEW;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kyma.EventType;
import net.kyma.data.QueryParameters;
import net.kyma.dm.SupportedField;
import pl.khuzzuk.messaging.Bus;

@EqualsAndHashCode(callSuper = true)
@Data
public class ContentElement extends BaseElement {
   private final Bus<EventType> bus;
   private final SupportedField field;

   @Override
   public QueryParameters toQuery()
   {
      return QueryParameters.builder()
            .returnTopic(DATA_QUERY_RESULT_FOR_CONTENT_VIEW)
            .field(field)
            .value(getName()).build();
   }
}
