package net.kyma.gui;


import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

public class StarsFactory {
    private static final int R = -8;
    private static final int starsDistance = 22;

    private static void drawStarLines(Path star, int start, int endInclusive) {
        for (int i = start; i <= endInclusive; i++) {
            double t = toRadians(36 * i);
            double r = R / ((i % 2) + 1);
            double x = sin(t) * r;
            double y = cos(t) * r;
            star.getElements().add(new LineTo(x, y));
        }
        star.getElements().add(new ClosePath());
    }

    private static Path getStar() {
        Path star = new Path();
        star.getElements().add(new MoveTo(0, R));
        drawStarLines(star, 0 , 9);
        return star;
    }

    private static Group getHalfStar(Color first, Color second) {
        Path half1 = new Path();
        half1.getElements().add(new MoveTo(0, R));
        drawStarLines(half1, 0, 5);
        half1.setFill(first);
        Path half2 = new Path();
        half2.getElements().add(new MoveTo(0, R));
        drawStarLines(half2, 5, 9);
        half2.setFill(second);
        return new Group(half1, half2);
    }

    public static Node defineForRating(int rating) {
        HBox hBox = new HBox();
        for (int i = 0; i < 5; i++) {
            if (rating / 2 > i) {
                Path star = getStar();
                star.setFill(Color.BLACK);
                hBox.getChildren().add(star);
            } else if (rating / 2 == i && rating % 2 == 1) {
                hBox.getChildren().add(getHalfStar(Color.BLACK, Color.WHITE));
                //star.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                  //      new Stop(0, Color.BLACK), new Stop(1, Color.WHITE)));
            } else {
                hBox.getChildren().add(getStar());
            }
        }
        return hBox;
    }
}
