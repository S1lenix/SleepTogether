package com.s1lenix.sleeptogether;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

public class SleepEventHandler {
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        tickCounter++;
        if (tickCounter >= 20) {
            tickCounter = 0;
            ServerLevel level = ServerLifecycleHooks.getCurrentServer().overworld();
            checkSleepingPlayers(level);
        }
    }

    private static void checkSleepingPlayers(ServerLevel level) {
        int total = 0;
        int sleeping = 0;

        for (ServerPlayer player : level.players()) {
            if (!player.isSpectator()) {
                total++;
                if (player.isSleeping()) sleeping++;
            }
        }

        if (total == 0) return;

        int percent = (int) ((sleeping / (float) total) * 100);
        if (percent >= SleepModConfig.REQUIRED_PERCENT.get()) { // Теперь используется get()
            skipNight(level, percent);
        }
    }

    private static void skipNight(ServerLevel level, int percent) {
        level.setDayTime(0);
        level.players().forEach(p -> {
            if (p.isSleeping()) p.stopSleeping();
        });

        // Сообщение в чат
        Component msg = Component.literal("Ночь пропущена! Спали " + percent + "% игроков");
        level.getServer().getPlayerList().broadcastSystemMessage(msg, false);

        // Звук телепортации (можно заменить на другой)
        level.players().forEach(player -> {
            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
        });
    }
}