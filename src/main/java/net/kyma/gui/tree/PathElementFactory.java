package net.kyma.gui.tree;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PathElementFactory {
   public static void fillChild(BaseElement parent, String[] path, int pos) {
      if (pos == path.length - 1) {
         return;
      }

      BaseElement child = parent.getChildElement(path[pos]);
      if (child == null) {
         child = new BaseElement();
         child.setName(path[pos]);
         parent.addChild(child);
         child.setParentElement(parent);
      }
      fillChild(child, path, pos + 1);
   }
}
