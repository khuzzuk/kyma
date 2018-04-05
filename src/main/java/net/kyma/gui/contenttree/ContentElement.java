package net.kyma.gui.contenttree;

import static net.kyma.EventType.DATA_QUERY;
import static net.kyma.EventType.DATA_QUERY_RESULT_FOR_CONTENT_VIEW;

import javafx.collections.ObservableList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kyma.EventType;
import net.kyma.data.QueryParameters;
import net.kyma.dm.SoundFile;
import net.kyma.dm.SupportedField;
import pl.khuzzuk.messaging.Bus;

@EqualsAndHashCode(callSuper = true)
@Data
public class ContentElement extends BaseElement {
   private final Bus<EventType> bus;
   private final SupportedField field;

   @Override
   public boolean isBranch()
   {
      return false;
   }

   @Override
   public void fill(ObservableList<SoundFile> toFill)
   {
      bus.message(DATA_QUERY).withContent(QueryParameters.builder()
            .field(field)
            .value(getName())
            .returnTopic(DATA_QUERY_RESULT_FOR_CONTENT_VIEW).build()).send();
   }
}
