package net.charl.disembodiment.util;

import net.charl.disembodiment.Disembodiment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> DEMATERIALIZER_FUEL = tag("dematerializer_fuel");

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(new ResourceLocation(Disembodiment.MOD_ID, name));
        }
    }

    public static class Items {

    }
}
