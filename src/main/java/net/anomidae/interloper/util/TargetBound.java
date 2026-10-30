package net.anomidae.interloper.util;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * An entity that hunts one specific player instead of "whoever is closest".
 * This is what keeps multiplayer sane: two players, two hunts, no crossfire.
 */
public interface TargetBound {
    @Nullable
    UUID boundPlayerId();

    void bindPlayer(@Nullable UUID playerId);

    default boolean isBoundTo(UUID playerId) {
        return playerId != null && playerId.equals(this.boundPlayerId());
    }
}
