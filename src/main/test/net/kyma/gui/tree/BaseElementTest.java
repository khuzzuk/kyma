package net.kyma.gui.tree;

import org.testng.Assert;
import org.testng.annotations.Test;

public class BaseElementTest
{
   @Test
   public void testDisjunction() {
      BaseElement element1 = createBaseElement("name1");
      BaseElement element2 = createBaseElement("name2");

      PathElementFactory.fillChild(element1, new String[]{"A", "A_A", "A_A_A", "file"}, 0);
      PathElementFactory.fillChild(element1, new String[]{"A", "A_A", "A_A_B", "file"}, 0);
      PathElementFactory.fillChild(element1, new String[]{"A", "A_B", "A_B_A", "file"}, 0);
      PathElementFactory.fillChild(element1, new String[]{"A", "A_B", "A_B_B", "file"}, 0);
      PathElementFactory.fillChild(element1, new String[]{"B", "B_A", "B_A_A", "file"}, 0);
      PathElementFactory.fillChild(element1, new String[]{"B", "B_A", "B_A_B", "file"}, 0);
      PathElementFactory.fillChild(element1, new String[]{"B", "B_B", "B_B_A", "file"}, 0);
      PathElementFactory.fillChild(element1, new String[]{"B", "B_B", "B_B_B", "file"}, 0);

      PathElementFactory.fillChild(element2, new String[]{"A", "A_A", "A_A_A", "file"}, 0);
      PathElementFactory.fillChild(element2, new String[]{"A", "A_A", "A_A_B", "file"}, 0);
      PathElementFactory.fillChild(element2, new String[]{"B", "B_A", "B_A_A", "file"}, 0);
      PathElementFactory.fillChild(element2, new String[]{"B", "B_B", "B_B_A", "file"}, 0);
      PathElementFactory.fillChild(element2, new String[]{"B", "B_B", "B_B_B", "file"}, 0);
      PathElementFactory.fillChild(element2, new String[]{"C", "C_A", "C_A_A", "file"}, 0);
      PathElementFactory.fillChild(element2, new String[]{"C", "C_A", "C_A_B", "file"}, 0);

      element1.update(element2);

      Assert.assertTrue(element1.hasChild("A"));
      Assert.assertTrue(element1.getChildElement("A").hasChild("A_A"));
      Assert.assertTrue(element1.getChildElement("A").getChildElement("A_A").hasChild("A_A_A"));
      Assert.assertTrue(element1.getChildElement("A").getChildElement("A_A").hasChild("A_A_B"));
      Assert.assertFalse(element1.getChildElement("A").hasChild("A_B"));

      Assert.assertTrue(element1.hasChild("B"));
      Assert.assertTrue(element1.getChildElement("B").hasChild("B_A"));
      Assert.assertTrue(element1.getChildElement("B").getChildElement("B_A").hasChild("B_A_A"));
      Assert.assertTrue(element1.getChildElement("B").hasChild("B_B"));
      Assert.assertTrue(element1.getChildElement("B").getChildElement("B_B").hasChild("B_B_A"));
      Assert.assertTrue(element1.getChildElement("B").getChildElement("B_B").hasChild("B_B_B"));

      Assert.assertTrue(element1.hasChild("C"));
      Assert.assertTrue(element1.getChildElement("C").hasChild("C_A"));
      Assert.assertTrue(element1.getChildElement("C").getChildElement("C_A").hasChild("C_A_A"));
      Assert.assertTrue(element1.getChildElement("C").getChildElement("C_A").hasChild("C_A_B"));
   }

   private BaseElement createBaseElement(String name) {
      BaseElement element = new BaseElement();
      element.setName(name);
      return element;
   }
}