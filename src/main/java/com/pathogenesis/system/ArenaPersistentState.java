package com.pathogenesis.system;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

public class ArenaPersistentState extends PersistentState {

    private boolean built = false;
    private boolean bossDefeated = false;
    private boolean skinTerrainBuilt = false;
    private boolean parkourBuilt = false;
    private int cx = 0, cy = 64, cz = 0;

    public ArenaPersistentState() {}

    public boolean isBuilt() { return built; }
    public boolean isBossDefeated() { return bossDefeated; }
    public boolean isSkinTerrainBuilt() { return skinTerrainBuilt; }
    public boolean isParkourBuilt() { return parkourBuilt; }
    public BlockPos getCenter() { return new BlockPos(cx, cy, cz); }

    public void setBuilt(boolean v) { built = v; markDirty(); }
    public void setBossDefeated(boolean v) { bossDefeated = v; markDirty(); }
    public void setSkinTerrainBuilt(boolean v) { skinTerrainBuilt = v; markDirty(); }
    public void setParkourBuilt(boolean v) { parkourBuilt = v; markDirty(); }
    public void setCenter(BlockPos pos) { cx = pos.getX(); cy = pos.getY(); cz = pos.getZ(); markDirty(); }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        nbt.putBoolean("built", built);
        nbt.putBoolean("bossDefeated", bossDefeated);
        nbt.putBoolean("skinTerrainBuilt", skinTerrainBuilt);
        nbt.putBoolean("parkourBuilt7", parkourBuilt);
        nbt.putInt("cx", cx);
        nbt.putInt("cy", cy);
        nbt.putInt("cz", cz);
        return nbt;
    }

    public static ArenaPersistentState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        ArenaPersistentState s = new ArenaPersistentState();
        s.built = nbt.getBoolean("built");
        s.bossDefeated = nbt.getBoolean("bossDefeated");
        s.skinTerrainBuilt = nbt.getBoolean("skinTerrainBuilt");
        s.parkourBuilt = nbt.getBoolean("parkourBuilt7");
        s.cx = nbt.getInt("cx");
        s.cy = nbt.getInt("cy");
        s.cz = nbt.getInt("cz");
        return s;
    }

    public static ArenaPersistentState getOrCreate(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
            new PersistentState.Type<>(
                ArenaPersistentState::new,
                ArenaPersistentState::fromNbt,
                null
            ),
            "pathogenesis_arena"
        );
    }
}
