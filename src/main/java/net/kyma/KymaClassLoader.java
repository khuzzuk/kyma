package net.kyma;

public class KymaClassLoader extends ClassLoader {
   @Override
   protected Class<?> findClass(String name) throws ClassNotFoundException
   {
      return super.findClass(name);
   }
}
