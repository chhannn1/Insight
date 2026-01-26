package com.chan1.client.feature.misc;

import com.chan1.client.config.InsightConfig;
import com.chan1.client.util.detection.CrosshairDetector;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;


public final class BedBlockDetector {



    public static BlockPos getTargetedBedPos() {
        CrosshairDetector.BlockTarget<BedBlock> target = CrosshairDetector.getTargetedBlockInfo(BedBlock.class);
        if (target == null) {
            return null;
        }

        return getBedHeadPos(target.pos(), target.state());
    }

    private static BlockPos getBedHeadPos(BlockPos pos, BlockState state) {
        if (state.getValue(BedBlock.PART) == BedPart.HEAD) {
            return pos;
        }

        return pos.relative(BedBlock.getConnectedDirection(state));
    }

    // weird pos stuff, im sure there is a way better solution
    public static Vec3 getBedBillboardPosition(BlockPos bedHeadPos) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return Vec3.atCenterOf(bedHeadPos).add(0, 1.0, 0);
        }

        BlockState state = minecraft.level.getBlockState(bedHeadPos);
        if (!(state.getBlock() instanceof BedBlock)) {
            return Vec3.atCenterOf(bedHeadPos).add(0, 1.0, 0);
        }

        BlockPos footPos = bedHeadPos.relative(BedBlock.getConnectedDirection(state));

        double centerX = (bedHeadPos.getX() + footPos.getX()) / 2.0 + 0.5;
        double centerZ = (bedHeadPos.getZ() + footPos.getZ()) / 2.0 + 0.5;
        double centerY = bedHeadPos.getY() + 0.5 + InsightConfig.getTooltipYOffset();

        return new Vec3(centerX, centerY, centerZ);
    }
}
