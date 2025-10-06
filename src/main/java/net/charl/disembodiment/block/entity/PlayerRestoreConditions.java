package net.charl.disembodiment.block.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class PlayerRestoreConditions {
    private final ResourceKey<Level> dimension;
    private final Vec3 pos;
    private final float yaw, pitch;
    private final GameType prevMode;

    private PlayerRestoreConditions(ResourceKey<Level> dimension, Vec3 pos, float yaw, float pitch, GameType prevMode) {
        this.dimension = dimension;
        this.pos = pos;
        this.yaw = yaw;
        this.pitch = pitch;
        this.prevMode = prevMode;
    }

    public static PlayerRestoreConditions capture(ServerPlayer sp) {
        return new PlayerRestoreConditions(
                sp.level().dimension(),
                sp.position(),
                sp.getYRot(),
                sp.getXRot(),
                sp.gameMode.getGameModeForPlayer()
        );
    }

    public void restore(ServerPlayer sp, MinecraftServer server) {
        ServerLevel destination = server.getLevel(dimension);
        if (destination != null && pos != null) {
            sp.teleportTo(destination, pos.x, pos.y, pos.z, yaw, pitch);
        }
        sp.gameMode.changeGameModeForPlayer(prevMode);
        sp.connection.send(new ClientboundGameEventPacket(
                ClientboundGameEventPacket.CHANGE_GAME_MODE,
                prevMode.getId()
        ));
        sp.onUpdateAbilities();
    }

    public CompoundTag toTag() {
        CompoundTag t = new CompoundTag();
        t.putString("Dim", dimension.location().toString());
        t.putDouble("X", pos.x);
        t.putDouble("Y", pos.y);
        t.putDouble("Z", pos.z);
        t.putFloat("Yaw", yaw);
        t.putFloat("Pitch", pitch);
        t.putString("PrevMode", prevMode.getName());
        return t;
    }

    @Nullable
    public static PlayerRestoreConditions fromTag(CompoundTag t) {
        if (t == null) { return null; }
        String dimStr = t.getString("Dim");
        ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimStr));
        Vec3 pos = new Vec3(t.getDouble("X"), t.getDouble("Y"), t.getDouble("Z"));
        float yaw = t.getFloat("Yaw");
        float pitch = t.getFloat("Pitch");
        GameType mode = GameType.byName(t.getString("PrevMode"), GameType.SURVIVAL);
        return new PlayerRestoreConditions(dim, pos, yaw, pitch, mode);
    }
}