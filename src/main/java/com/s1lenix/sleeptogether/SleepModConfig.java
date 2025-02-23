package com.s1lenix.sleeptogether;

import net.minecraftforge.common.ForgeConfigSpec;

public class SleepModConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue REQUIRED_PERCENT;
    public static final ForgeConfigSpec.IntValue EFFECT_DURATION;
    public static final ForgeConfigSpec.IntValue PUNISHMENT_DELAY;

    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        REQUIRED_PERCENT = builder
                .comment("Процент игроков для пропуска ночи (0-100)")
                .defineInRange("requiredPercent", 50, 0, 100);

        EFFECT_DURATION = builder
                .comment("Продолжительность эффекта регенерации в секундах")
                .defineInRange("effectDuration", 10, 1, 300);

        PUNISHMENT_DELAY = builder
                .comment("Задержка перед наказанием в секундах")
                .defineInRange("punishmentDelay", 5, 1, 60);

        SPEC = builder.build();
    }
}