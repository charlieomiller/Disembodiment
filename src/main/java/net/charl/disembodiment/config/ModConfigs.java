package net.charl.disembodiment.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfigs {

    public static final ForgeConfigSpec SERVER_SPEC;
    public static final Server SERVER;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        SERVER = new Server(b);
        SERVER_SPEC = b.build();
    }

    public static final class Server {
        public final ForgeConfigSpec.IntValue secondsPerInkor;
        public final ForgeConfigSpec.IntValue secondsBeforeDematerialization;

        Server(ForgeConfigSpec.Builder b) {
            b.push("balance");
            secondsPerInkor = b
                    .comment("How many seconds of dematerialization one Inkor grants.")
                    .defineInRange("seconds_per_inkor", 15, 0, 3600);

            secondsBeforeDematerialization = b
                    .comment("How many seconds the buffer between inserting Inkor and fully dematerializing lasts.")
                    .defineInRange("seconds_before_dematerialization", 1, 0, 60);
            b.pop();
        }
    }

    private ModConfigs() {}
}
