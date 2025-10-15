package net.charl.disembodiment.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.charl.disembodiment.Disembodiment;
import net.charl.disembodiment.config.ModClientConfigs;
import net.charl.disembodiment.networking.TimerSyncS2C;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Disembodiment.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientVignetteOverlay {
    private static final ResourceLocation VIGNETTE = new ResourceLocation(Disembodiment.MOD_ID, "textures/misc/white_vignette.png");
    private static final float DEFAULT_TICKS_PER_SEC = 20;
    private static float alpha = 0f;

    // pulsing effect parameters
    private final static float BASE_ALPHA  = 0.40f;
    private final static float PULSE_AMPL  = 0.15f; // +/- around BASE_ALPHA
    private final static float PERIOD_SEC  = 4.0f; // one full pulse every X seconds

    private static final IGuiOverlay OVERLAY = (gui, g, partial, w, h) -> {
        if (!ModClientConfigs.CLIENT.enableVignette.get()) { return; } // Skip all dis if vignette is disabled

        var disp = ClientTimerHudState.getDisplay();

        boolean active = disp.phase() == TimerSyncS2C.Phase.ACTIVE;

        float target;
        if (active) {
            Minecraft mc = Minecraft.getInstance();
            long gameTime = (mc.level != null) ? mc.level.getGameTime() : 0L;
            float tSec = gameTime / DEFAULT_TICKS_PER_SEC + partial;

            // my friend Sine. Wave
            float omega = (float)(2.0 * Math.PI / PERIOD_SEC);
            float pulse = (float)Math.sin(omega * tSec);

            target = Math.min(1f, Math.max(0f, BASE_ALPHA + PULSE_AMPL * pulse));
        } else { target = 0.0f; } // fade out when inactive

        alpha += (target - alpha) * 0.15f;
        if (alpha < 0.01f) return; // doesn't need to be drawn if this transparent

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha); // slight tint

        // stretches the vignette to the entire screen
        g.blit(VIGNETTE, 0, 0, -90, 0, 0, w, h, w, h);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    };

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent e) {
        e.registerBelow(new ResourceLocation(Disembodiment.MOD_ID, "demat_timer"),
                "demat_vignette", OVERLAY);
    }

    private ClientVignetteOverlay() {}
}
