package net.charl.disembodiment.sound;

import net.charl.disembodiment.Disembodiment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Disembodiment.MOD_ID);

    public static final RegistryObject<SoundEvent> DEMATERIALIZER_START_BUFFER = registerSoundEvents("dematerializer_start_buffer");
    public static final RegistryObject<SoundEvent> DEMATERIALIZER_ACTIVATE_LOOP = registerSoundEvents("dematerializer_activate_loop");
    public static final RegistryObject<SoundEvent> PLAYER_DEMATERIALIZATION_LOOP = registerSoundEvents("player_dematerialization_loop");

    private static RegistryObject<SoundEvent> registerSoundEvents(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Disembodiment.MOD_ID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
