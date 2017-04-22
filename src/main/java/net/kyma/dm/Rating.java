package net.kyma.dm;

import javafx.scene.Group;
import javafx.scene.Node;
import net.kyma.gui.StarsFactory;

import java.util.EnumSet;
import java.util.Set;

public enum Rating {
    UNDEFINED(0, 0, 0, 0),
    NONE(1, 1, 14, 0),
    HALF(15, 15, 30, 1),
    ONE(31, 31, 31, 2),
    ONE_HALF(32, 32, 48, 3),
    TWO(64, 49, 64, 4),
    TWO_HALF(96, 65, 96, 5),
    THREE(128, 97, 159, 6),
    THREE_HALF(160, 160, 195, 7),
    FOUR(196, 196, 223, 8),
    FOUR_HALF(224, 224, 251, 9),
    FIVE(255, 252, 255, 10);
    private int value;
    private int min;
    private int max;
    private int rate;
    private static Set<Rating> SET = EnumSet.allOf(Rating.class);

    Rating(int value, int min, int max, int rate) {
        this.value = value;
        this.min = min;
        this.max = max;
        this.rate = rate;
    }

    public static Node getStarFor(int tagRate) {
        return StarsFactory.defineForRating(getRatingBy(tagRate).rate);
    }

    private static Rating getRatingBy(int tagRate) {
        return SET.stream().filter(r -> r.min <= tagRate && r.max >= tagRate).findAny().orElse(UNDEFINED);
    }
}
