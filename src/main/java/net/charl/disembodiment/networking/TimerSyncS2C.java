package net.charl.disembodiment.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TimerSyncS2C {
    public enum Phase { NONE, BUFFER, ACTIVE }
    public final Phase phase;
    public final int ticksRemaining;
    public final int ticksTotal;

    public TimerSyncS2C(Phase phase, int ticksRemaining, int ticksTotal) {
        this.phase = phase;
        this.ticksRemaining = ticksRemaining;
        this.ticksTotal = ticksTotal;
    }

    public static void encode(TimerSyncS2C m, FriendlyByteBuf buf) {
        buf.writeEnum(m.phase);
        buf.writeVarInt(m.ticksRemaining);
        buf.writeVarInt(m.ticksTotal);
    }
    public static TimerSyncS2C decode(FriendlyByteBuf buf) {
        Phase ph = buf.readEnum(Phase.class);
        int rem = buf.readVarInt();
        int tot = buf.readVarInt();
        return new TimerSyncS2C(ph, rem, tot);
    }

    public static void handle(TimerSyncS2C m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () ->
                        () -> net.charl.disembodiment.client.ClientTimerHudState.applyServerSnapshot(m)
                )
        );
        ctx.get().setPacketHandled(true);
    }
}
