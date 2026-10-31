package net.anomidae.interloper.world;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Everything the world needs to remember between days: where the street is,
 * whether the graffiti is there today, and which machines are watching it.
 */
public class SkyboxState extends SavedData {
    private static final String DATA_NAME = "interloper_skybox";

    private long graffitiDay = -1L;
    private boolean graffitiPresent;
    private long graffitiChance = 50L;

    private boolean streetBuilt;
    private BlockPos streetCenter = BlockPos.ZERO;
    @Nullable
    private BlockPos graffitiPos;

    private final List<BlockPos> ravenChecks = new ArrayList<>();
    private boolean skyboxUnlocked;

    public static SkyboxState get(ServerLevel level) {
        MinecraftServer server = level.getServer();
        ServerLevel overworld = server == null ? level : server.overworld();
        return overworld.getDataStorage().computeIfAbsent(SkyboxState::load, SkyboxState::new, DATA_NAME);
    }

    public static SkyboxState load(CompoundTag tag) {
        SkyboxState state = new SkyboxState();
        state.graffitiDay = tag.getLong("GraffitiDay");
        state.graffitiPresent = tag.getBoolean("Graffiti");
        state.graffitiChance = tag.contains("GraffitiChance") ? tag.getLong("GraffitiChance") : 50L;
        state.streetBuilt = tag.getBoolean("StreetBuilt");
        if (tag.contains("StreetX")) {
            state.streetCenter = new BlockPos(tag.getInt("StreetX"), tag.getInt("StreetY"), tag.getInt("StreetZ"));
        }
        if (tag.contains("GraffitiX")) {
            state.graffitiPos = new BlockPos(tag.getInt("GraffitiX"), tag.getInt("GraffitiY"), tag.getInt("GraffitiZ"));
        }
        ListTag checks = tag.getList("RavenChecks", 10);
        for (int i = 0; i < checks.size(); i++) {
            CompoundTag entry = checks.getCompound(i);
            state.ravenChecks.add(new BlockPos(entry.getInt("x"), entry.getInt("y"), entry.getInt("z")));
        }
        state.skyboxUnlocked = tag.getBoolean("SkyboxUnlocked");
        return state;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLong("GraffitiDay", this.graffitiDay);
        tag.putBoolean("Graffiti", this.graffitiPresent);
        tag.putLong("GraffitiChance", this.graffitiChance);
        tag.putBoolean("StreetBuilt", this.streetBuilt);
        tag.putInt("StreetX", this.streetCenter.getX());
        tag.putInt("StreetY", this.streetCenter.getY());
        tag.putInt("StreetZ", this.streetCenter.getZ());
        if (this.graffitiPos != null) {
            tag.putInt("GraffitiX", this.graffitiPos.getX());
            tag.putInt("GraffitiY", this.graffitiPos.getY());
            tag.putInt("GraffitiZ", this.graffitiPos.getZ());
        }
        ListTag checks = new ListTag();
        for (BlockPos pos : this.ravenChecks) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("x", pos.getX());
            entry.putInt("y", pos.getY());
            entry.putInt("z", pos.getZ());
            checks.add(entry);
        }
        tag.put("RavenChecks", checks);
        tag.putBoolean("SkyboxUnlocked", this.skyboxUnlocked);
        return tag;
    }

    // ------------------------------------------------------------------ graffiti

    public boolean graffitiPresent() {
        return this.graffitiPresent;
    }

    public long graffitiChance() {
        return this.graffitiChance;
    }

    public long graffitiDay() {
        return this.graffitiDay;
    }

    public void setGraffiti(long day, boolean present, long chance) {
        this.graffitiDay = day;
        this.graffitiPresent = present;
        this.graffitiChance = chance;
        this.setDirty();
    }

    @Nullable
    public BlockPos graffitiPos() {
        return this.graffitiPos;
    }

    public void setGraffitiPos(@Nullable BlockPos pos) {
        this.graffitiPos = pos;
        this.setDirty();
    }

    // ------------------------------------------------------------------ street

    public boolean streetBuilt() {
        return this.streetBuilt;
    }

    public BlockPos streetCenter() {
        return this.streetCenter;
    }

    public void setStreet(BlockPos center) {
        this.streetBuilt = true;
        this.streetCenter = center;
        this.setDirty();
    }

    // ------------------------------------------------------------------ ravencheck

    public List<BlockPos> ravenChecks() {
        return this.ravenChecks;
    }

    public void addRavenCheck(BlockPos pos) {
        if (!this.ravenChecks.contains(pos)) {
            this.ravenChecks.add(pos.immutable());
            this.setDirty();
        }
    }

    public void removeRavenCheck(BlockPos pos) {
        if (this.ravenChecks.remove(pos)) {
            this.setDirty();
        }
    }

    // ------------------------------------------------------------------ skybox

    public boolean skyboxUnlocked() {
        return this.skyboxUnlocked;
    }

    public void unlockSkybox() {
        this.skyboxUnlocked = true;
        this.setDirty();
    }

    /** Unused but kept so the save format stays stable if we ever bind a player. */
    public void touch(@Nullable UUID ignored) {
        this.setDirty();
    }
}
