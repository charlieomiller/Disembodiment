package net.charl.disembodiment.config;

import ca.weblite.objc.Client;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ModClientConfigs {
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        CLIENT = new Client(b);
        CLIENT_SPEC = b.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }

    public static final class Client {
        public final ForgeConfigSpec.BooleanValue enableVignette;
        public final ForgeConfigSpec.BooleanValue enableHudTimer;

        Client(ForgeConfigSpec.Builder b) {
            b.push("visuals");
            enableVignette = b
                    .comment("Display pulsing vignette effect while dematerialized?")
                    .define("enable_vignette", true);
            enableHudTimer = b
                    .comment("Display on-screen timer?")
                    .define("enable_hud_timer", true);
            b.pop();
        }
    }

    private ModClientConfigs() {}
}
