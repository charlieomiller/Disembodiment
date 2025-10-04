package net.charl.disembodiment.block.entity;

import net.charl.disembodiment.Disembodiment;
import net.charl.disembodiment.config.ModConfigs;
import net.charl.disembodiment.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class DematerializerBlockEntity extends BlockEntity {
    private static final int DEFAULT_TICKS_PER_SEC = 20;
    private static class PlayerTimer {
        int bufferTicks; // amount of ticks before player is dematerialized after shilling Inkor
        int mainTicks; // amount of ticks player is dematerialized for
        boolean active; // false = buffer countdown, true = main countdown
    }
    private final Map<UUID, PlayerTimer> timers = new HashMap<>();

    public DematerializerBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.DEMATERIALIZER_BE.get(), pPos, pBlockState);
    }

    public boolean canAcceptFuel(UUID playerId) {
        PlayerTimer t = timers.get(playerId);
        return t == null || !t.active;
    }

    public void addInkorForPlayer(UUID playerId) {
        PlayerTimer t = timers.computeIfAbsent(playerId, id -> new PlayerTimer());

        int ticksToAddToMain = ModConfigs.SERVER.secondsPerInkor.get() * DEFAULT_TICKS_PER_SEC;
        if (ticksToAddToMain > 0) { t.mainTicks += ticksToAddToMain; }

        int ticksToAddToBuffer = ModConfigs.SERVER.secondsBeforeDematerialization.get() * DEFAULT_TICKS_PER_SEC;
        if (ticksToAddToBuffer > 0) { t.bufferTicks = ticksToAddToBuffer; }

        setChanged();
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (pLevel.isClientSide) { return; }
        if (timers.isEmpty()) { return; }

        boolean changed = false;
        Iterator<Map.Entry<UUID, PlayerTimer>> it = timers.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<UUID, PlayerTimer> e = it.next();
            PlayerTimer t = e.getValue();
            UUID playerId = e.getKey();

            if (!t.active) { // Buffer State
                if (t.bufferTicks > 0) {
                    if (t.bufferTicks == ModConfigs.SERVER.secondsBeforeDematerialization.get() * DEFAULT_TICKS_PER_SEC) {
                        System.out.println("BUFFER STARTED");
                        if (pLevel instanceof ServerLevel sl) { // Pattern matching casts sl equal to pLevel as type ServerLevel
                            ServerPlayer sp = sl.getServer().getPlayerList().getPlayer(playerId);
                            if (sp != null) {

                                sp.connection.send(
                                        new ClientboundSoundPacket(
                                                ModSounds.DEMATERIALIZER_START_BUFFER.getHolder().orElseThrow(), // Need this instead of just .get
                                                SoundSource.BLOCKS, pPos.getX(), pPos.getY(), pPos.getZ(), 1f, 1f, 0)
                                );
                            }
                        }
                    }
                    t.bufferTicks--;
                    changed = true;
                    if (t.bufferTicks <= 0 && t.mainTicks > 0) {
                        t.active = true;
                        changed = true;
                        // trigger start effects here
                        System.out.println("BUFFER OVER");
                    } else { System.out.println(playerId + " HAS " + t.bufferTicks + " BUFFER TICKS REMAINING"); }
                } else {
                    if (t.mainTicks <= 0) { it.remove(); changed = true; } // clean up entries that have no remaining buffer or main ticks
                }
            }
            else if (t.active) { // Dematerialization State
                if (t.mainTicks > 0) {
                    t.mainTicks--;
                    changed = true;
                    if (t.mainTicks == 0) {
                        t.active = false;
                        it.remove();
                        changed = true;
                        System.out.println(playerId + " HAS RETURNED TO NORMAL");
                        // trigger end effects here
                    } else { System.out.println(playerId + " HAS " + t.mainTicks + " MAIN TICKS REMAINING"); }
                } else { t.active = false; it.remove(); changed = true; } // Should never be called
            }
        }

        if (changed) setChanged();
    }

    // junk for persistence

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        ListTag list = new ListTag();

        for (Map.Entry<UUID, PlayerTimer> e : timers.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Player", e.getKey());
            entry.putInt("Buffer", e.getValue().bufferTicks);
            entry.putInt("Main", e.getValue().mainTicks);
            entry.putBoolean("Active", e.getValue().active);
            list.add(entry);
        }
        pTag.put("PlayerTimers", list);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        timers.clear();

        if (pTag.contains("PlayerTimers", Tag.TAG_LIST)) {
            ListTag list = pTag.getList("PlayerTimers", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                PlayerTimer pt = new PlayerTimer();
                pt.bufferTicks  = Math.max(0, entry.getInt("Buffer"));
                pt.mainTicks    = Math.max(0, entry.getInt("Main"));
                pt.active       = entry.getBoolean("Active");
                UUID id = entry.getUUID("Player");

                if (pt.bufferTicks > 0 || pt.mainTicks > 0) { timers.put(id, pt); }
            }
        }
    }


}