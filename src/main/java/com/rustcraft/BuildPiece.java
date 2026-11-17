package com.rustcraft;

import java.util.Locale;

public enum BuildPiece {
    FOUNDATION(20),
    TRI_FOUNDATION(10),
    FLOOR(12),
    TRI_FLOOR(6),
    WALL(12),
    DOORWAY(9),
    WINDOW(10),
    STAIRS(15);

    public final int cost;

    BuildPiece(int cost) { this.cost = cost; }

    public String key() { return name().toLowerCase(Locale.ROOT); }

    public boolean isFoundation() { return this == FOUNDATION || this == TRI_FOUNDATION; }
    public boolean isTriangle() { return this == TRI_FOUNDATION || this == TRI_FLOOR; }
    public boolean isWall() { return this == WALL || this == DOORWAY || this == WINDOW; }
}
