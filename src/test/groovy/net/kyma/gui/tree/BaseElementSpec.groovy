package net.kyma.gui.tree

import spock.lang.Specification

class BaseElementSpec extends Specification {
    def "check update graph"() {
        given:
        BaseElement element1 = createBaseElement("element1")
        BaseElement element2 = createBaseElement("element2")

        element1.addChildFor(["A", "A_A", "A_A_A", "file"] as String[], 0)
        element1.addChildFor(["A", "A_A", "A_A_B", "file"] as String[], 0)
        element1.addChildFor(["A", "A_B", "A_B_A", "file"] as String[], 0)
        element1.addChildFor(["A", "A_B", "A_B_B", "file"] as String[], 0)
        element1.addChildFor(["B", "B_A", "B_A_A", "file"] as String[], 0)
        element1.addChildFor(["B", "B_A", "B_A_B", "file"] as String[], 0)
        element1.addChildFor(["B", "B_B", "B_B_A", "file"] as String[], 0)
        element1.addChildFor(["B", "B_B", "B_B_B", "file"] as String[], 0)
        element1.addChildFor(["D", "D_A", "D_A_A", "file"] as String[], 0)

        element2.addChildFor(["A", "A_A", "A_A_A", "file"] as String[], 0)
        element2.addChildFor(["A", "A_A", "A_A_B", "file"] as String[], 0)
        element2.addChildFor(["B", "B_A", "B_A_A", "file"] as String[], 0)
        element2.addChildFor(["B", "B_B", "B_B_A", "file"] as String[], 0)
        element2.addChildFor(["B", "B_B", "B_B_B", "file"] as String[], 0)
        element2.addChildFor(["C", "C_A", "C_A_A", "file"] as String[], 0)
        element2.addChildFor(["C", "C_A", "C_A_B", "file"] as String[], 0)

        when:
        element1.update(element2);

        then:
        element1.hasChild("A")
        element1.getChildElement("A").hasChild("A_A")
        element1.getChildElement("A").getChildElement("A_A").hasChild("A_A_A")
        element1.getChildElement("A").getChildElement("A_A").hasChild("A_A_B")
        !element1.getChildElement("A").hasChild("A_B")

        element1.hasChild("B")
        element1.getChildElement("B").hasChild("B_A")
        element1.getChildElement("B").getChildElement("B_A").hasChild("B_A_A")
        element1.getChildElement("B").hasChild("B_B")
        element1.getChildElement("B").getChildElement("B_B").hasChild("B_B_A")
        element1.getChildElement("B").getChildElement("B_B").hasChild("B_B_B")

        element1.hasChild("C")
        element1.getChildElement("C").hasChild("C_A")
        element1.getChildElement("C").getChildElement("C_A").hasChild("C_A_A")
        element1.getChildElement("C").getChildElement("C_A").hasChild("C_A_B")

        !element1.hasChild("D")
    }

    def 'check if ConcurrentModificationException is not thrown'() {
        given:
        BaseElement element1 = createBaseElement("element1")
        BaseElement element2 = createBaseElement("element2")

        element1.addChildFor(["A", "A_A", "A_A_A", "file"] as String[], 0)
        element1.addChildFor(["B", "B_A", "B_A_A", "file"] as String[], 0)

        when:
        element1.update(element2)

        then:
        element1.childElements.size() == 0
    }

    private static BaseElement createBaseElement(String name) {
        BaseElement element = new BaseElement()
        element.setName(name)
        element
    }
}
