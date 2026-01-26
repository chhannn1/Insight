package com.chan1.client.feature.misc;

import com.chan1.client.config.InsightConfig;
import com.chan1.client.util.detection.CrosshairDetector;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BeetrootBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class CropGrowthDetector {



    public record CropInfo(BlockPos pos, String cropName, int currentAge, int maxAge, boolean isFullyGrown) {
        public float getGrowthPercent() {
            if (maxAge == 0) return 100f;
            return (currentAge / (float) maxAge) * 100f;
        }
    }


    public static CropInfo getTargetedCropInfo() {
        BlockHitResult blockHit = CrosshairDetector.getTargetedBlockHit();
        if (blockHit == null || blockHit.getType() != HitResult.Type.BLOCK) {
            return null;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return null;
        }

        BlockPos pos = blockHit.getBlockPos();
        BlockState state = minecraft.level.getBlockState(pos);
        return getCropInfo(pos, state);
    }


    private static CropInfo getCropInfo(BlockPos pos, BlockState state) {
        Block block = state.getBlock();

        if (block instanceof CropBlock crop) {
            int age = crop.getAge(state);
            int maxAge = crop.getMaxAge();
            String name = getCropName(block);
            return new CropInfo(pos, name, age, maxAge, age >= maxAge);
        }

        if (block instanceof BeetrootBlock) {
            int age = state.getValue(BeetrootBlock.AGE);
            int maxAge = 3;
            return new CropInfo(pos, "Beetroot", age, maxAge, age >= maxAge);
        }

        if (block instanceof NetherWartBlock) {
            int age = state.getValue(NetherWartBlock.AGE);
            int maxAge = 3;
            return new CropInfo(pos, "Nether Wart", age, maxAge, age >= maxAge);
        }

        if (block instanceof StemBlock) {
            int age = state.getValue(StemBlock.AGE);
            int maxAge = 7;
            String name = block.getName().getString();
            return new CropInfo(pos, name, age, maxAge, age >= maxAge);
        }

        if (block instanceof SweetBerryBushBlock) {
            int age = state.getValue(SweetBerryBushBlock.AGE);
            int maxAge = 3;
            return new CropInfo(pos, "Sweet Berries", age, maxAge, age >= maxAge);
        }


        if (block instanceof CocoaBlock) {
            int age = state.getValue(CocoaBlock.AGE);
            int maxAge = 2;
            return new CropInfo(pos, "Cocoa", age, maxAge, age >= maxAge);
        }

        return null;
    }


    private static String getCropName(Block block) {
        String name = block.getName().getString();
        // just in case :p
        if (name.toLowerCase().contains("wheat")) return "Wheat";
        if (name.toLowerCase().contains("carrot")) return "Carrots";
        if (name.toLowerCase().contains("potato")) return "Potatoes";
        return name;
    }


    public static Vec3 getCropBillboardPosition(BlockPos cropPos) {
        return Vec3.atCenterOf(cropPos).add(0, 0.1 + InsightConfig.getTooltipYOffset(), 0);
    }
}
