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

    private static void drawStarLines(Path star, int start, int endInclusive) {
        for (int i = start; i <= endInclusive; i++) {
            double t = toRadians(36 * i);
            double r = R / ((i % 2) + 1);
            double x = sin(t) * r;
            double y = cos(t) * r;
            star.getElements().add(new LineTo(x, y));
        }
    }

    private static Path getStar() {
        Path star = new Path();
        star.getElements().add(new MoveTo(0, R));
        drawStarLines(star, 0 , 9);
        star.setStroke(Color.WHITE);
        star.getElements().add(new ClosePath());
        star.getStyleClass().add("star");
        return star;
    }

    private static Group getHalfStar() {
        Path half1 = new Path();
        half1.getElements().add(new MoveTo(0, R));
        drawStarLines(half1, 0, 5);
        half1.setFill(Color.WHITE);
        half1.setStroke(Color.WHITE);
        half1.getElements().add(new ClosePath());
        Path half2 = new Path();
        half2.getElements().add(new MoveTo(0, R));
        drawStarLines(half2, 5, 10);
        half2.setStroke(Color.WHITE);
        Group group = new Group(half1, half2);
        group.getStyleClass().add("star");
        return group;
    }

    public static Node defineForRating(int rating) {
        HBox hBox = new HBox();
        for (int i = 0; i < 5; i++) {
            if (rating / 2 > i) {
                Path star = getStar();
                star.setFill(Color.WHITE);
                star.setStroke(Color.WHITE);
                star.getStyleClass().add("star");
                hBox.getChildren().add(star);
            } else if (rating / 2 == i && rating % 2 == 1) {
                hBox.getChildren().add(getHalfStar());
            } else {
                hBox.getChildren().add(getStar());
            }
        }
        return hBox;
    }
}
