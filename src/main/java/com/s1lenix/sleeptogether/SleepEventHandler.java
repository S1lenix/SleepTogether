package com.s1lenix.sleeptogether;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SleepEventHandler {
    // Логгер для записи отладочной информации
    private static final Logger log = LoggerFactory.getLogger(SleepEventHandler.class);

    // Счетчик тиков для отслеживания секундных интервалов
    private static int tickCounter = 0;

    // Таймер наказания (в тиках)
    private static int punishmentTimer = 0;

    // Флаг активности таймера наказания
    private static boolean isPunishmentScheduled = false;

    // Текущий процент спящих игроков
    private static int percentOfPlayers = 0;

    // TODO: Поменять логику с тиковой системы на систему с проверкой "игрок на кровати"
    // Основной обработчик игровых тиков
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        // Работаем только в конце игрового тика
        if (event.phase != TickEvent.Phase.END) return;

        // Получаем текущий игровой мир
        ServerLevel level = ServerLifecycleHooks.getCurrentServer().overworld();
        tickCounter++;

        if (tickCounter >= 20) {
            tickCounter = 0; // Сбрасываем счетчик
            checkSleepingPlayers(level); // Проверяем спящих игроков

            // Если таймер активен
            if (isPunishmentScheduled) {
                punishmentTimer++; // Увеличиваем таймер
                int delaySeconds = SleepModConfig.PUNISHMENT_DELAY.get();

                // Если таймер достиг нужного значения
                if (punishmentTimer >= delaySeconds) {
                    applyPunishment(level); // Применяем наказание
                    isPunishmentScheduled = false; // Выключаем таймер
                    punishmentTimer = 0; // Сбрасываем счетчик
                }
            }
        }
    }

    // Метод проверки спящих игроков
    private static void checkSleepingPlayers(@NotNull ServerLevel level) {
        // Получаем текущее игровое время (0-24000)
        long timeOfDay = level.getDayTime() % 24000;

        //       Работаем только ночью (с 13000 до 23000)
        if (timeOfDay < 13000 || timeOfDay > 23000) return;

        int totalPlayers = 0;    // Всего игроков
        int sleepingPlayers = 0; // Спящих игроков

        // Считаем игроков в мире
        for (ServerPlayer player : level.players()) {
            if (!player.isSpectator()) { // Игнорируем наблюдателей
                totalPlayers++;
                if (player.isSleeping()) sleepingPlayers++;
            }
        }
        punishOrNot(totalPlayers, sleepingPlayers, level);
    }


    private static void punishOrNot(int totalPlayers, int sleepingPlayers, ServerLevel level) {
        log.info("Всего игроков: {}, Спящих: {}", totalPlayers, sleepingPlayers);

        // Если игроков нет - выходим
        if (totalPlayers == 0) return;

        // Рассчитываем процент спящих
        percentOfPlayers = (int) ((sleepingPlayers / (float) totalPlayers) * 100);
        int requiredPercent = SleepModConfig.REQUIRED_PERCENT.get();

        log.info("Процент спящих: {}, Требуется: {}", percentOfPlayers, requiredPercent);

        // Если есть спящие, но меньше нужного процента
        if (sleepingPlayers > 0 && percentOfPlayers < requiredPercent) {
            // Если таймер не активирован
            if (!isPunishmentScheduled) {
                isPunishmentScheduled = true; // Активируем таймер
                punishmentTimer = 0; // Сбрасываем счетчик

                // Отправляем предупреждение в чат
                level.getServer().getPlayerList().broadcastSystemMessage(
                        Component.literal("Слишком мало спящих! Если через " +
                                SleepModConfig.PUNISHMENT_DELAY.get() + " секунд никто не ляжет спать " +
                                requiredPercent + "% игроков, ночь будет тяжелой!"),
                        false
                );
                log.info("Таймер наказания активирован на {} секунд", SleepModConfig.PUNISHMENT_DELAY.get());
            }
        }
        // Если достигнут нужный процент
        else if (percentOfPlayers >= requiredPercent) {
            skipNightWithRegen(level, percentOfPlayers); // Пропускаем ночь с регенерацией
            isPunishmentScheduled = false; // Отключаем таймер
        }
    }

    // Метод применения наказания
    private static void applyPunishment(ServerLevel level) {
        log.info("Применяем наказание");
        // Для всех игроков в мире
        level.players().forEach(player -> {
            // Если игрок не спит
            if (!player.isSleeping()) {
                // Накладываем эффект голода II на 10 секунд
                player.addEffect(new MobEffectInstance(
                        MobEffects.HUNGER,
                        SleepModConfig.EFFECT_DURATION.get() * 20,
                        1
                ));
            }
        });
        skipNight(level, percentOfPlayers); // Пропускаем ночь
    }

    // Метод пропуска ночи с регенерацией
    private static void skipNightWithRegen(ServerLevel level, int percentOfPlayers) {
        log.info("Пропускаем ночь с {}% спящих", percentOfPlayers);
        // Для всех игроков в мире
        level.players().forEach(player -> {
            // Если игрок спит
            if (player.isSleeping()) {
                // Накладываем эффект регенерации II на 10 секунд
                player.addEffect(new MobEffectInstance(
                        MobEffects.REGENERATION,
                        SleepModConfig.EFFECT_DURATION.get() * 20,
                        1
                ));
            }
        });
        skipNight(level, percentOfPlayers); // Пропускаем ночь
    }

    // TODO Поменять замену игрового времени на реальную смену дня и ночи
    // Базовый метод пропуска ночи
    private static void skipNight(ServerLevel level, int percent) {
        log.info("Пропуск ночи");
        level.setDayTime(0); // Устанавливаем время на 6:00 (0 тиков)

        // Будим всех игроков
        level.players().forEach(p -> {
            if (p.isSleeping()) p.stopSleeping();
        });

        // Отправляем сообщение в чат
        Component msg = Component.literal("Ночь пропущена! Спали " + percent + "% игроков");
        level.getServer().getPlayerList().broadcastSystemMessage(msg, false);
    }
}