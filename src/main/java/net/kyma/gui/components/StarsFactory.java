package net.kyma.gui.components;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;

import static java.lang.Math.*;

@UtilityClass
public class StarsFactory
{
   private static final int R = -8;
   private static final int START_SPACING = 16;

   private static void drawStarLines(Path star, int start, int endInclusive)
   {
      for (int i = start; i <= endInclusive; i++)
      {
         double t = toRadians((double) 36 * i);
         double r = (double) R / ((i % 2) + 1);
         double x = sin(t) * r;
         double y = cos(t) * r;
         star.getElements().add(new LineTo(x, y));
      }
   }

   private static Path getStar()
   {
      Path star = new Path();
      star.getElements().add(new MoveTo(0, R));
      drawStarLines(star, 0, 9);
      star.getElements().add(new ClosePath());
      star.getStyleClass().add("star");
      return star;
   }

   private static Pair<Path, Path> getHalfStar()
   {
      Path half1 = new Path();
      half1.setFill(Color.BLACK);
      half1.getElements().add(new MoveTo(0, R));
      drawStarLines(half1, 0, 5);
      half1.getElements().add(new ClosePath());
      half1.getStyleClass().add("star");
      Path half2 = new Path();
      half2.getElements().add(new MoveTo(0, R));
      drawStarLines(half2, 5, 10);
      half2.getStyleClass().add("star");
      half2.getStyleClass().add("star-empty");
      return Pair.of(half1, half2);
   }

   public static Node defineForRating(int rating)
   {
      AnchorPane pane = new AnchorPane();
      for (int i = 0; i < 5; i++)
      {
         if (rating / 2 > i)
         {
            Path star = getStar();
            star.getStyleClass().add("star");
            star.setFill(Color.BLACK);
            pane.getChildren().add(star);
            AnchorPane.setTopAnchor(star, 0d);
            AnchorPane.setLeftAnchor(star, (double) (i * START_SPACING) + i);
         }
         else if (rating / 2 == i && rating % 2 == 1)
         {
            Pair<Path, Path> halfStar = getHalfStar();
            pane.getChildren().add(halfStar.getLeft());
            AnchorPane.setTopAnchor(halfStar.getLeft(), 0d);
            AnchorPane.setLeftAnchor(halfStar.getLeft(), (double) (i * START_SPACING) + i);
            pane.getChildren().add(halfStar.getRight());
            AnchorPane.setTopAnchor(halfStar.getRight(), 0d);
            AnchorPane.setLeftAnchor(halfStar.getRight(), (double) (i * START_SPACING) + (double) START_SPACING / 2 + i);
         }
         else
         {
            Path star = getStar();
            star.getStyleClass().add("star-empty");
            pane.getChildren().add(star);
            AnchorPane.setTopAnchor(star, 0d);
            AnchorPane.setLeftAnchor(star, (double) (i * START_SPACING) + i);
         }
      }
      pane.setMinHeight(START_SPACING + 2);
      return pane;
   }
}
