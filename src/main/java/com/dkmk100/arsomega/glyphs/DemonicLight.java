package com.dkmk100.arsomega.glyphs;

import com.dkmk100.arsomega.util.RegistryHandler;
import com.hollingsworth.arsnouveau.api.ANFakePlayer;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.util.BlockUtil;
import com.hollingsworth.arsnouveau.common.block.SconceBlock;
import com.hollingsworth.arsnouveau.common.block.tile.LightTile;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import com.hollingsworth.arsnouveau.setup.BlockRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public class DemonicLight extends TierFourEffect {
    public static DemonicLight INSTANCE = new DemonicLight("demonic_light","Demonic Light");

    public DemonicLight(String name, String description) {
        super(RegistryHandler.getGlyphName(name),description);
    }

    @Override
    public void onResolveEntity(EntityHitResult rayTraceResult, Level world, @Nullable LivingEntity shooter, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver) {
        if (rayTraceResult.getEntity() instanceof ILightable) {
            ((ILightable)rayTraceResult.getEntity()).onLight(rayTraceResult, world, shooter, spellStats, spellContext);
        }

        if (rayTraceResult.getEntity() instanceof LivingEntity) {
            if(!spellStats.hasBuff(AugmentSensitive.INSTANCE)) {
                this.applyConfigPotion((LivingEntity) rayTraceResult.getEntity(), MobEffects.GLOWING, spellStats);
            }
            if(!spellStats.hasBuff(AugmentExtract.INSTANCE)) {
                this.applyConfigPotion((LivingEntity) rayTraceResult.getEntity(), MobEffects.NIGHT_VISION, spellStats, false);
            }
        }
    }

    @Override
    protected void addDefaultAugmentLimits(Map<ResourceLocation, Integer> defaults) {
        defaults.put(AugmentAmplify.INSTANCE.getRegistryName(), 2);
    }

    //to change later
    @Override
    public void onResolveBlock(BlockHitResult rayTraceResult, Level world, @Nullable LivingEntity shooter, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver) {
        BlockPos pos = rayTraceResult.getBlockPos().relative(rayTraceResult.getDirection());
        if (BlockUtil.destroyRespectsClaim(this.getPlayer(shooter, (ServerLevel)world), world, pos)) {
            BlockPos rawPos = rayTraceResult.getBlockPos();
            Block block = world.getBlockState(rawPos).getBlock();
            if (world.getBlockEntity(rawPos) instanceof ILightable) {
                ((ILightable)world.getBlockEntity(rayTraceResult.getBlockPos())).onLight(rayTraceResult, world, shooter, spellStats, spellContext);
            }
            else if(block == Blocks.GLOWSTONE){
                world.setBlockAndUpdate(rawPos, Blocks.DEEPSLATE_COAL_ORE.defaultBlockState());
            }
            else if(block == RegistryHandler.DEMONIC_GLOWSTONE.get()){
                world.setBlockAndUpdate(rawPos, Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState());
            }
            else {
                if (world.getBlockState(pos).getMaterial().isReplaceable() && world.isUnobstructed(BlockRegistry.LIGHT_BLOCK.defaultBlockState(), pos, CollisionContext.of(ANFakePlayer.getPlayer((ServerLevel)world)))) {
                    world.setBlockAndUpdate(pos, (BlockState)BlockRegistry.LIGHT_BLOCK.defaultBlockState().setValue(SconceBlock.LIGHT_LEVEL, Math.max(0, Math.min(15, 14 + (int)spellStats.getAmpMultiplier()))));
                    LightTile tile = (LightTile)world.getBlockEntity(pos);
                    if (tile != null) {
                        tile.color = spellContext.getColors();
                    }
                }

            }
        }
    }

    @Override
    public void buildConfig(ForgeConfigSpec.Builder builder) {
        super.buildConfig(builder);
        this.addPotionConfig(builder, 45);
    }

    @Override
    public int getDefaultManaCost() {
        return 250;
    }

    @Override
    @Nonnull
    public Set<AbstractAugment> getCompatibleAugments() {
        return this.augmentSetOf(new AbstractAugment[]{AugmentAmplify.INSTANCE, AugmentDurationDown.INSTANCE, AugmentDampen.INSTANCE, AugmentExtendTime.INSTANCE, AugmentSensitive.INSTANCE});
    }

    @Override
    public String getBookDescription() {
        return "If cast on a block, a permanent light source is created. May be amplified up to Glowstone brightness, or Dampened for a lower light level. When cast on yourself, you will receive night vision. When cast on other entities, they will receive Night Vision and Glowing.";
    }

    @Override
    @Nonnull
    public Set<SpellSchool> getSchools() {
        return this.setOf(new SpellSchool[]{SpellSchools.CONJURATION});
    }
}
