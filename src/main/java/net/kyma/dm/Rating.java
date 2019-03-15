package net.kyma.dm;

import javafx.scene.Node;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyma.gui.components.StarsFactory;
import net.kyma.player.Format;

import java.util.EnumSet;
import java.util.Set;

@AllArgsConstructor
public enum Rating {
    UNDEFINED(0, 0, 0, 0, 0, 0, 0),
    NONE(1, 1, 14, 1, 1, 1, 0),
    HALF(15, 15, 30, 2, 9, 5, 1),
    ONE(31, 31, 31, 10, 19, 10, 2),
    ONE_HALF(32, 32, 48, 20, 29, 20, 3),
    TWO(64, 49, 64, 30, 39, 30, 4),
    TWO_HALF(96, 65, 96, 40, 49, 40, 5),
    THREE(128, 97, 159, 50, 59, 50, 6),
    THREE_HALF(160, 160, 195, 60, 69, 60, 7),
    FOUR(196, 196, 223, 70, 79, 70, 8),
    FOUR_HALF(224, 224, 251, 80, 89, 80, 9),
    FIVE(255, 252, 255, 90, 100, 99, 10);
    @Getter
    private final int value;
    private final int min;
    private final int max;
    private final int minP;
    private final int maxP;
    @Getter
    private final int valueP;
    @Getter
    private final int rate;
    private static final Set<Rating> SET = EnumSet.allOf(Rating.class);

    public static Node getStarFor(int rate) {
        return StarsFactory.defineForRating(rate);
    }

    public static void setRate(int rate, SoundFile file) {
        SET.stream().filter(r -> r.rate == rate).findAny().ifPresent(file::setRate);
    }

    public static Rating getRatingBy(int tagRate, Format format) {
        if (format.isByteScale()) {
            return SET.stream().filter(r -> r.min <= tagRate && r.max >= tagRate).findAny().orElse(UNDEFINED);
        } else {
            return SET.stream().filter(r -> r.minP <= tagRate && r.maxP >= tagRate).findAny().orElse(UNDEFINED);
        }
    }

    public static Rating getFor(int rate) {
        return SET.stream().filter(r -> r.rate == rate).findAny().orElse(UNDEFINED);
    }
}
