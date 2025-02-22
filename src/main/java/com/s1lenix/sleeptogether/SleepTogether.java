package com.s1lenix.sleeptogether;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(SleepTogether.MOD_ID)
public class SleepTogether {
    public static final String MOD_ID = "sleeptogether";

    public SleepTogether() {
        // Используйте ModConfig.Type из Forge, а не из вашего класса
        ModLoadingContext.get().registerConfig(
                ModConfig.Type.SERVER,
                SleepModConfig.SPEC // SPEC из вашего класса
        );

        MinecraftForge.EVENT_BUS.register(SleepEventHandler.class);
    }
}