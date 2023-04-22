package com.dkmk100.arsomega.items;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import net.minecraft.world.item.Item.Properties;

public class BiomeCrystal extends BasicItem {

    public BiomeCrystal(Properties p_i48487_1_) {
        super(p_i48487_1_);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip2, TooltipFlag flagIn) {
        if (stack.hasTag() && stack.getTag().contains("biome")) {
            tooltip2.add(Component.literal("Biome: " + stack.getTag().getString("biome")));
        }
        super.appendHoverText(stack, worldIn, tooltip2, flagIn);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (world instanceof ServerLevel) {

            boolean changed = false;

            String biome = "minecraft:ocean";
            BlockPos pos = player.blockPosition();

            Biome biome2 = world.getBiome(pos).value();

            if (biome2 != null) {
                //biome = biome2.getRegistryName().toString();
            }

            if (stack.hasTag() && stack.getTag().getString("biome") == biome) {
                changed = false;
            } else {
                //don't override crystal
                if(!stack.hasTag() || !stack.getTag().contains("biome") || stack.getTag().getString("biome") == "") {
                    changed = true;
                    stack.getOrCreateTag().putString("biome", biome);
                }
                else{
                    changed = false;
                    //not sure what do do here
                }
            }

            if (changed) {
                return InteractionResultHolder.success(stack);
            } else {
                return InteractionResultHolder.pass(stack);
            }
        }
        return InteractionResultHolder.pass(stack);
    }
}

