package net.kyma.initialization;

import java.lang.reflect.Field;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

@Aspect
@Log4j2
public class InitializationAspect {
   @After("set(* *) && @annotation(net.kyma.initialization.Dependency) && this(dependable)")
   public synchronized void check(Dependable dependable) {
      for (Field field : dependable.getClass().getDeclaredFields()) {
         if (isProperty(field) && isEmpty(field, dependable)) {
            return;
         }
      }
      if (!dependable.isInitialized()) {
         dependable.afterDependenciesSet();
         dependable.setInitialized(true);
      }
   }

   private boolean isProperty(Field field) {
      return field.getDeclaredAnnotation(Dependency.class) != null;
   }

   private boolean isEmpty(Field field, Object instance) {
      try {
         field.setAccessible(true);
         return field.get(instance) == null;
      } catch (IllegalAccessException e) {
         log.error("error during initialization of {}", instance.getClass());
         return false;
      }
   }
}
