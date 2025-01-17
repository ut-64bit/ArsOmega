package com.dkmk100.arsomega.rituals;

import com.dkmk100.arsomega.ArsOmega;
import com.dkmk100.arsomega.util.RegistryHandler;
import com.hollingsworth.arsnouveau.api.ANFakePlayer;
import com.hollingsworth.arsnouveau.api.ArsNouveauAPI;
import com.hollingsworth.arsnouveau.api.spell.EntitySpellResolver;
import com.hollingsworth.arsnouveau.api.spell.Spell;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import com.hollingsworth.arsnouveau.api.util.SourceUtil;
import com.hollingsworth.arsnouveau.client.particle.ParticleColor;
import com.hollingsworth.arsnouveau.client.particle.ParticleLineData;
import com.hollingsworth.arsnouveau.client.particle.ParticleUtil;
import com.hollingsworth.arsnouveau.common.block.tile.RuneTile;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentAOE;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentAccelerate;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentExtract;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentSensitive;
import com.hollingsworth.arsnouveau.setup.BlockRegistry;
import com.hollingsworth.arsnouveau.setup.ItemsRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.util.FakePlayer;

import java.util.ArrayList;
import java.util.List;

public class RitualAura extends BasicConfigRitual {
    boolean initialized = false;
    boolean sensitive = false;
    boolean extract = false;
    int discount = 0;
    int cap = 10;
    int aoe = 0;
    int accelerate = 0;

    @Override
    public void write(CompoundTag tag) {
        tag.putInt("aoe",aoe);
        tag.putInt("cap",cap);
        tag.putInt("discount",discount);
        tag.putInt("accelerate",accelerate);
        tag.putBoolean("initialized",initialized);
        tag.putBoolean("sensitive",sensitive);
        tag.putBoolean("extract",extract);
        super.write(tag);
    }
    @Override
    public void read(CompoundTag tag) {
        aoe = tag.getInt("aoe");
        cap = tag.getInt("cap");
        discount = tag.getInt("discount");
        accelerate = tag.getInt("accelerate");
        initialized = tag.getBoolean("initialized");
        sensitive = tag.getBoolean("sensitive");
        extract = tag.getBoolean("extract");
        super.read(tag);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return RegistryHandler.getRitualName("aura");
    }
    @Override
    protected void tick() {
        Level world = this.getWorld();
        if (world.isClientSide) {
            BlockPos pos = this.getPos();

            for (int i = 0; i < 100; ++i) {
                Vec3 particlePos = (new Vec3((double) pos.getX(), (double) pos.getY(), (double) pos.getZ())).add(0.5D, 0.0D, 0.5D);
                particlePos = particlePos.add(ParticleUtil.pointInSphere().multiply(3.0D, 3.0D, 3.0D));
                world.addParticle(ParticleLineData.createData(this.getCenterColor()), particlePos.x(), particlePos.y(), particlePos.z(), (double) pos.getX() + 0.5D, (double) (pos.getY() + 1), (double) pos.getZ() + 0.5D);
            }
        }

        if (!world.isClientSide && world.getGameTime() % 20L == 0L) {
            this.incrementProgress();
            if (this.getProgress() % (8-accelerate) == 0) {
                if(!initialized) {
                    sensitive = false;
                    extract = false;
                    discount = 0;
                    cap = 10;
                    aoe = 0;
                    List<ItemStack> items = this.getConsumedItems();
                    //if lag becomes an issue I can always make a static ritual manager to save this data like flight does, but that seems difficult
                    for (ItemStack stack : items) {
                        if (stack.getItem() == ArsNouveauAPI.getInstance().getGlyphItem(AugmentAOE.INSTANCE)) {
                            if (stack.getCount() <= 0) {
                                aoe += 1;
                            } else {
                                aoe += stack.getCount();
                            }
                        }
                        else if (stack.getItem() == ArsNouveauAPI.getInstance().getGlyphItem(AugmentAccelerate.INSTANCE)) {
                            if (stack.getCount() <= 0) {
                                accelerate += 1;
                            } else {
                                accelerate += stack.getCount();
                            }
                        } else if (stack.getItem() == ArsNouveauAPI.getInstance().getGlyphItem(AugmentSensitive.INSTANCE)) {
                            sensitive = true;
                        } else if (stack.getItem() == ArsNouveauAPI.getInstance().getGlyphItem(AugmentExtract.INSTANCE)) {
                            extract = true;
                        }
                    }
                    accelerate = Math.min(accelerate,7);//clamp accelerate to 7, so it doesn't cause weird math errors later.
                }

                BlockPos pos = getPos();
                FakePlayer fakePlayer = ANFakePlayer.getPlayer((ServerLevel)world);
                fakePlayer.setPos((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
                List<Spell> spells = new ArrayList<Spell>();
                for(Direction dir : Direction.values()){
                    if(dir==Direction.DOWN){
                        BlockPos pos2 = pos.relative(dir, 1);
                        Block block = world.getBlockState(pos2).getBlock();
                        if(block == Blocks.DIAMOND_BLOCK){
                            cap+=2;
                            aoe +=2;
                            discount +=15;
                        }
                        else if(block == Blocks.IRON_BLOCK){
                            cap+=2;
                        }
                        else if(block == Blocks.GOLD_BLOCK){
                            cap+=2;
                            aoe+=1;
                            discount+=7;
                        }
                        else if(block == Blocks.NETHERITE_BLOCK){
                            cap+=5;
                            aoe+=2;
                            discount+=30;
                        }
                        else if(block == RegistryHandler.GORGON_STONE.get()){
                            cap+=12;
                            aoe-=2;
                            discount-=30;
                        }

                    }
                    else if(dir==Direction.UP){
                        BlockPos pos2 = pos.relative(dir, 1);
                        Block block = world.getBlockState(pos2).getBlock();
                        if(block == Blocks.BEACON){
                            cap+=5;
                            aoe +=10;
                        }
                    }
                    else {
                        BlockPos pos2 = pos.relative(dir, 1);
                        if(world.getBlockState(pos2).getBlock() == BlockRegistry.RUNE_BLOCK){
                            RuneTile tile = (RuneTile) world.getBlockEntity(pos2);
                            if(tile.spell!=null){
                                spells.add(tile.spell);
                            }
                        }
                    }
                }
                aoe = Math.min(aoe,cap);//cap AOE

                //by having total cost, we only search for source jars once and thus everythign works much better.
                //initial value is extra/min cost
                int totalCost = 3;
                for(Spell spell : spells){
                    totalCost+= Math.max(spell.getDiscountedCost() - discount, 0);//discount is per-spell, but can never take it below 0
                }
                if(SourceUtil.takeSourceWithParticles(pos, world, 6, totalCost) != null) {
                    List<LivingEntity> entities = this.getWorld().getEntitiesOfClass(LivingEntity.class, (new AABB(this.getPos())).inflate(5.0D + aoe * 2).inflate(12, 0, 12));
                    for (LivingEntity entity : entities) {
                        boolean player = entity instanceof Player;
                        if ((!player && extract) || (!extract && (player || !sensitive))) {
                            for (Spell spell : spells) {
                                EntitySpellResolver resolver = new EntitySpellResolver((new SpellContext(getWorld(),spell, fakePlayer)).withCastingTile(world.getBlockEntity(pos)).withType(SpellContext.CasterType.TURRET).withColors(new ParticleColor(255, 255, 255)));
                                resolver.onCastOnEntity(entity);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public ParticleColor getCenterColor() {
        return new ParticleColor(255,255,230);
    }
    @Override
    public boolean canConsumeItem(ItemStack stack) {
        return stack.getItem() == ArsNouveauAPI.getInstance().getGlyphItem(AugmentAOE.INSTANCE) || stack.getItem() == ArsNouveauAPI.getInstance().getGlyphItem(AugmentSensitive.INSTANCE) || stack.getItem() == ArsNouveauAPI.getInstance().getGlyphItem(AugmentExtract.INSTANCE) || stack.getItem() == ArsNouveauAPI.getInstance().getGlyphItem(AugmentExtract.INSTANCE);
    }
}
