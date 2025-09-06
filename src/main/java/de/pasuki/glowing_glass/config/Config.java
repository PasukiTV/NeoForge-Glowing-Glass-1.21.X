package de.pasuki.glowing_glass.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    public static final ModConfigSpec COMMON_SPEC;

    public static final ModConfigSpec.IntValue LIGHT_LEVEL_BLOCKS; // 0..15
    public static final ModConfigSpec.IntValue LIGHT_LEVEL_PANES;  // 0..15

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("glowing_glass");

        LIGHT_LEVEL_BLOCKS = b
                .comment("Light level for Glowing Glass blocks (0-15).")
                .defineInRange("lightLevelBlocks", 12, 0, 15);

        LIGHT_LEVEL_PANES = b
                .comment("Light level for Glowing Glass panes (0-15).")
                .defineInRange("lightLevelPanes", 10, 0, 15);

        b.pop();
        COMMON_SPEC = b.build();
    }

    private Config() {}
}
