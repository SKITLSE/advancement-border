package net.reimaden.advancementborder;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public final class StateSaverAndLoader extends SavedData {
    private static final String ADVANCEMENTS_KEY = "completedAdvancements";
    private static final String FRESH_WORLD_KEY = "isFreshWorld";
    public HashSet<ResourceLocation> completedAdvancements = new HashSet<>();
    public boolean isFreshWorld = true;

    @Override
    public @NotNull CompoundTag save(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        ListTag advancementList = new ListTag();
        for (ResourceLocation advancement : completedAdvancements) {
            advancementList.add(StringTag.valueOf(advancement.toString()));
        }
        nbt.put(ADVANCEMENTS_KEY, advancementList);
        nbt.putBoolean(FRESH_WORLD_KEY, isFreshWorld);
        return nbt;
    }

    private static StateSaverAndLoader createFromNbt(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        StateSaverAndLoader state = new StateSaverAndLoader();
        ListTag advancementList = nbt.getList(ADVANCEMENTS_KEY, Tag.TAG_STRING);
        for (int i = 0; i < advancementList.size(); i++) {
            ResourceLocation id = ResourceLocation.parse(advancementList.getString(i));
            state.completedAdvancements.add(id);
        }
        state.isFreshWorld = nbt.getBoolean(FRESH_WORLD_KEY);
        return state;
    }

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        ServerLevel world = server.overworld();
        DimensionDataStorage manager = world.getDataStorage();
        return manager.computeIfAbsent(
            tag -> createFromNbt(tag, world.registryAccess()),
            StateSaverAndLoader::new,
            AdvancementBorder.MOD_ID
        );
    }

}
