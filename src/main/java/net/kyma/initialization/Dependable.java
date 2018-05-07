package net.kyma.initialization;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class Dependable {
   @Getter(AccessLevel.PACKAGE)
   @Setter(AccessLevel.PACKAGE)
   private boolean initialized;

   public abstract void afterDependenciesSet();
}
