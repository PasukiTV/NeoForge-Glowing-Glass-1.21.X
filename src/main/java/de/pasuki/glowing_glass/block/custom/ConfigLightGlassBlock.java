package de.pasuki.glowing_glass.block.custom;

import de.pasuki.glowing_glass.config.ConfigCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ConfigLightGlassBlock extends TransparentBlock {
    public ConfigLightGlassBlock(Properties props) { super(props); }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return Math.max(0, Math.min(15, ConfigCache.lightBlocks));
    }
}
