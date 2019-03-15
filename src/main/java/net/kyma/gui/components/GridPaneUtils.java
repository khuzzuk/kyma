package net.kyma.gui.components;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GridPaneUtils {
    public static ColumnConstraints columnConstraints(double percentageWidth) {
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setHgrow(Priority.SOMETIMES);
        if (percentageWidth > 0) {
            columnConstraints.setPercentWidth(percentageWidth);
        }
        columnConstraints.setPercentWidth(percentageWidth);
        return columnConstraints;
    }

    public static RowConstraints rowConstraints(double percentHeight) {
        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setVgrow(Priority.ALWAYS);
        if (percentHeight > 0) {
            rowConstraints.setPercentHeight(percentHeight);
        }
        return rowConstraints;
    }
}
