package net.charl.disembodiment.block.entity;

import net.charl.disembodiment.Disembodiment;
import net.charl.disembodiment.block.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static net.charl.disembodiment.Disembodiment.MOD_ID;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);

    public static final RegistryObject<BlockEntityType<DematerializerBlockEntity>> DEMATERIALIZER_BE =
            BLOCK_ENTITIES.register("dematerializer_be", () ->
                    BlockEntityType.Builder.of(DematerializerBlockEntity::new,
                            ModBlocks.DEMATERIALIZER.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
