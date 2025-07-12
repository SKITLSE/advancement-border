package net.reimaden.advancementborder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.saveddata.SavedDataTypes;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashSet;

public final class StateSaverAndLoader extends SavedData {
    private static final String ADVANCEMENTS_KEY = "completedAdvancements";
    private static final String FRESH_WORLD_KEY = "isFreshWorld";
    public HashSet<ResourceLocation> completedAdvancements = new HashSet<>();
    public boolean isFreshWorld = true;

    @Override
    public CompoundTag save(CompoundTag nbt) {
        ListTag advancementList = new ListTag();
        for (ResourceLocation advancement : completedAdvancements) {
            advancementList.add(StringTag.valueOf(advancement.toString()));
        }
        nbt.put(ADVANCEMENTS_KEY, advancementList);
        nbt.putBoolean(FRESH_WORLD_KEY, isFreshWorld);
        return nbt;
    }

    public static final SavedDataType<StateSaverAndLoader> TYPE = new SavedDataType<>(
        StateSaverAndLoader::new,
        tag -> {
            StateSaverAndLoader state = new StateSaverAndLoader();
            if (tag.contains(ADVANCEMENTS_KEY) && tag.get(ADVANCEMENTS_KEY) instanceof ListTag listTag) {
                for (int i = 0; i < listTag.size(); i++) {
                    ResourceLocation.parse(listTag.getString(i))
                        .ifPresent(state.completedAdvancements::add);
                }
            }
            state.isFreshWorld = tag.getBoolean(FRESH_WORLD_KEY).orElse(true);
            return state;
        }
    );

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        ServerLevel world = server.overworld();
        DimensionDataStorage manager = world.getDataStorage();
        return manager.computeIfAbsent(TYPE, AdvancementBorder.MOD_ID);
    }
}
