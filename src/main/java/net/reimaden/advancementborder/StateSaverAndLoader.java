package net.reimaden.advancementborder;

import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashSet;
import java.util.Set;

public final class StateSaverAndLoader extends SavedData {

    private static final String ADVANCEMENTS_KEY = "completedAdvancements";
    private static final String FRESH_WORLD_KEY   = "isFreshWorld";

    private final Set<ResourceLocation> completedAdvancements = new HashSet<>();
    private boolean isFreshWorld = true;

    /* ---------- save ---------- */
    @Override
    public CompoundTag save(CompoundTag tag) {          // NOTE: без HolderLookup в 1.21
        ListTag list = new ListTag();
        for (ResourceLocation rl : completedAdvancements) {
            list.add(StringTag.valueOf(rl.toString()));
        }
        tag.put(ADVANCEMENTS_KEY, list);
        tag.putBoolean(FRESH_WORLD_KEY, isFreshWorld);
        return tag;
    }

    /* ---------- load ---------- */
    public static StateSaverAndLoader load(CompoundTag tag) {
        StateSaverAndLoader state = new StateSaverAndLoader();

        ListTag list = tag.getList(ADVANCEMENTS_KEY, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            state.completedAdvancements.add(ResourceLocation.parse(list.getString(i)));
        }
        state.isFreshWorld = tag.getBoolean(FRESH_WORLD_KEY);
        return state;
    }

    /* ---------- SavedDataType & accessor ---------- */
    private static final SavedDataType<StateSaverAndLoader> TYPE =
            new SavedDataType<>(StateSaverAndLoader::new, StateSaverAndLoader::load);

    public static StateSaverAndLoader get(ServerLevel level) {
        // computeIfAbsent теперь принимает только TYPE
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    /* ---------- helper ---------- */
    public boolean addCompleted(ResourceLocation id) {
        boolean changed = completedAdvancements.add(id);
        if (changed) setDirty();        // помечаем .dat как «грязный»
        return changed;
    }
}
