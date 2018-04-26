package net.kyma.initialization;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class InitializationAspect {
   @Before("@annotation(Property)")
   public void check() {
      System.out.println("\n\n\n \t\t\t weaved\n\n\n");
   }
}
