package net.reimaden.advancementborder;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashSet;
import java.util.Set;

public final class StateSaverAndLoader extends SavedData {

    // === keys ===
    private static final String ADVANCEMENTS_KEY = "completedAdvancements";
    private static final String FRESH_WORLD_KEY   = "isFreshWorld";

    // === state ===
    private final Set<ResourceLocation> completedAdvancements = new HashSet<>();
    private boolean isFreshWorld = true;

    /* -------------------------------------------------------------
     *  SAVE
     * ------------------------------------------------------------- */
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (ResourceLocation rl : completedAdvancements) {
            list.add(StringTag.valueOf(rl.toString()));
        }
        tag.put(ADVANCEMENTS_KEY, list);
        tag.putBoolean(FRESH_WORLD_KEY, isFreshWorld);
        return tag;
    }

    /* -------------------------------------------------------------
     *  LOAD
     * ------------------------------------------------------------- */
    public static StateSaverAndLoader load(CompoundTag tag, HolderLookup.Provider provider) {
        StateSaverAndLoader state = new StateSaverAndLoader();

        ListTag list = tag.getList(ADVANCEMENTS_KEY, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            // parse() бросает при недопустимой строке; можно заменить tryParse, если хочешь мягче
            state.completedAdvancements.add(ResourceLocation.parse(list.getString(i)));
        }

        state.isFreshWorld = tag.getBoolean(FRESH_WORLD_KEY);
        return state;
    }

    /* -------------------------------------------------------------
     *  Factory & accessor
     * ------------------------------------------------------------- */
    private static final SavedData.Factory<StateSaverAndLoader> TYPE =
            new SavedData.Factory<>(StateSaverAndLoader::new, StateSaverAndLoader::load, null);

    public static StateSaverAndLoader get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE, AdvancementBorder.MOD_ID);
    }

    // удобный метод, чтобы пометить данные «грязными»
    public boolean addCompleted(ResourceLocation id) {
        boolean changed = completedAdvancements.add(id);
        if (changed) setDirty();
        return changed;
    }
}
