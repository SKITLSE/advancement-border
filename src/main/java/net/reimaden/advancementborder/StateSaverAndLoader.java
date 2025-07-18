package net.reimaden.advancementborder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class StateSaverAndLoader extends SavedData {

    private static final String ADV_KEY   = "completedAdvancements";
    private static final String FRESH_KEY = "isFreshWorld";

    /* --------- состояние (public, чтобы видеть из других классов) --------- */
    public final Set<ResourceLocation> completedAdvancements = new HashSet<>();
    public boolean isFreshWorld = true;

    /* -------------------- сохранение в NBT -------------------- */
    // В 1.21.7 у SavedData НЕТ абстрактного метода, поэтому без @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        completedAdvancements.forEach(id -> list.add(StringTag.valueOf(id.toString())));
        tag.put(ADV_KEY, list);
        tag.putBoolean(FRESH_KEY, isFreshWorld);
        return tag;
    }

    /* -------------------- загрузка из NBT -------------------- */
    public static StateSaverAndLoader load(CompoundTag tag) {
        StateSaverAndLoader state = new StateSaverAndLoader();

        // getList(String) → Optional<ListTag>
        ListTag list = tag.getList(ADV_KEY).orElse(new ListTag());
        for (int i = 0; i < list.size(); i++) {
            Optional<String> strOpt = list.getString(i);      // Optional<String>
            strOpt.ifPresent(s -> state.completedAdvancements.add(ResourceLocation.parse(s)));
        }

        state.isFreshWorld = tag.getBoolean(FRESH_KEY).orElse(true);
        return state;
    }

    /* -------------------- тип данных для DataStorage -------------------- */
    private static final Codec<StateSaverAndLoader> CODEC =
            RecordCodecBuilder.create(inst -> inst.group(
                    // Списком строк, каждая строка = id ачивки
                    ResourceLocation.CODEC.listOf().fieldOf("completedAdvancements")
                            .forGetter(s -> List.copyOf(s.completedAdvancements)),
                    // Булево, по умолчанию true
                    Codec.BOOL.fieldOf("isFreshWorld").orElse(true)
                            .forGetter(s -> s.isFreshWorld)
            ).apply(inst, (list, fresh) -> {
                StateSaverAndLoader s = new StateSaverAndLoader();
                s.completedAdvancements.addAll(list);
                s.isFreshWorld = fresh;
                return s;
            }));

    /* ---------- SavedDataType ---------- */
    private static final SavedDataType<StateSaverAndLoader> TYPE =
            new SavedDataType<>(
                    "advancementborder_state",
                    ctx -> new StateSaverAndLoader(),   // конструктор «пустого» состояния
                    ctx -> CODEC,                       // теперь НЕ null
                    null                                // dataFixType можно оставить null
            );


    /* ─────────────── Получение / создание экземпляра ─────────────── */
    public static StateSaverAndLoader get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }
    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        return get(server.overworld());
    }
}
