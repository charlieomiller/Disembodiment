package net.charl.disembodiment.client;

import net.charl.disembodiment.Disembodiment;
import net.charl.disembodiment.config.ModClientConfigs;
import net.charl.disembodiment.networking.TimerSyncS2C;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Disembodiment.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientHudOverlay {
    private static final IGuiOverlay DEMAT_TIMER = (gui, g, partialTick, screenW, screenH) -> {
        if (!ModClientConfigs.CLIENT.enableHudTimer.get()) { return; } // Skip all dis if hud timer is disabled

        var display = ClientTimerHudState.getDisplay();
        if (!display.visible()) { return; }

        var font = gui.getFont();
        String text = display.label();

        int x = screenW / 2 - font.width(text) / 2;
        int y = screenH - 50; // CHANGE THIS TO SCALE WITH SCREEN HEIGHT?? eg. (screenH - screenH/20)

        int pad = 4;
        int w = font.width(text) + pad * 2;
        int h = font.lineHeight + 2;
        g.fill(x - pad, y - 2, x - pad + w, y - 2 + h, 0xAA000000);
        int color = (display.phase() == TimerSyncS2C.Phase.BUFFER) ? 0xFF66CCFF : 0xFF77FF77;

        g.drawString(font, text, x, y, color, true);
    };

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent e) {
        e.registerAboveAll("demat_timer", DEMAT_TIMER);
    }
}
