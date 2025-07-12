package net.reimaden.advancementborder;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public final class StateSaverAndLoader extends SavedData {

    private static final String ADVANCEMENTS_KEY = "completedAdvancements";
    private static final String FRESH_WORLD_KEY   = "isFreshWorld";

    /* ---------- Состояние (оставляем public, чтобы другой класс видел) ---------- */
    public final Set<ResourceLocation> completedAdvancements = new HashSet<>();
    public boolean isFreshWorld = true;

    /* ---------- Сохранение ---------- */
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider lookup) {
        ListTag list = new ListTag();
        completedAdvancements.forEach(id -> list.add(StringTag.valueOf(id.toString())));
        tag.put(ADVANCEMENTS_KEY, list);
        tag.putBoolean(FRESH_WORLD_KEY, isFreshWorld);
        return tag;
    }

    /* ---------- Загрузка ---------- */
    public static StateSaverAndLoader load(CompoundTag tag, HolderLookup.Provider lookup) {
        StateSaverAndLoader state = new StateSaverAndLoader();

        ListTag list = tag.getList(ADVANCEMENTS_KEY);  // new API: только имя
        for (int i = 0; i < list.size(); i++) {
            Optional<String> strOpt = list.getString(i);   // Optional<String>
            strOpt.ifPresent(s -> state.completedAdvancements.add(ResourceLocation.parse(s)));
        }

        state.isFreshWorld = tag.getBoolean(FRESH_WORLD_KEY).orElse(true);
        return state;
    }

    /* ---------- Тип данных ---------- */
    private static final SavedDataType<StateSaverAndLoader> TYPE =
            new SavedDataType<>(StateSaverAndLoader::load, StateSaverAndLoader::new);

    /* ---------- Доступ к state ---------- */
    public static StateSaverAndLoader get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);   // 1-аргументная версия
    }

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        return get(server.overworld());
    }
}
