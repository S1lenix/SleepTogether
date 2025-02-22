package com.s1lenix.sleeptogether;

import net.minecraftforge.common.ForgeConfigSpec;

public class SleepModConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue REQUIRED_PERCENT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        REQUIRED_PERCENT = builder
                .comment("Процент игроков для пропуска ночи (0-100)")
                .defineInRange("requiredPercent", 50, 0, 100);

        SPEC = builder.build();
    }
}