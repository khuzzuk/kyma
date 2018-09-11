package net.kyma.gui.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kyma.EventType;
import net.kyma.dm.DataQuery;
import net.kyma.dm.SupportedField;
import pl.khuzzuk.messaging.Bus;

@EqualsAndHashCode(callSuper = true)
@Data
public class ContentElement extends BaseElement {
   private final SupportedField field;

   @Override
   void applyTo(DataQuery query) {
      query.and(field, getName(), false);
   }
}
