package com.dkmk100.arsomega.glyphs;

import com.dkmk100.arsomega.potions.ModPotions;
import com.dkmk100.arsomega.util.RegistryHandler;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public class Heartstop extends AbstractEffect implements IDamageEffect {

    public static final DamageSource STATIC_HEARTSTOP_DAMAGE = new DamageSource("heartstop").bypassArmor().bypassMagic();
    public static final DamageSource HEARTSTOP_DAMAGE(Entity source){
        if(source == null){
            return STATIC_HEARTSTOP_DAMAGE;
        }
        return new EntityDamageSource("heartstop",source).bypassArmor().bypassMagic();
    }

    public static Heartstop INSTANCE = new Heartstop("heartstop", "Heartstop");

    public ForgeConfigSpec.DoubleValue BASE;
    public ForgeConfigSpec.DoubleValue AMP_DAMAGE;

    public Heartstop(String tag, String description) {
            super(RegistryHandler.getGlyphName(tag), description);
        }

    @Override
    public int getDefaultManaCost() {
        return 550;
    }
    @Override
    public void onResolveEntity(EntityHitResult rayTraceResult, Level world, @Nullable LivingEntity shooter, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver) {
        Entity entity = rayTraceResult.getEntity();
        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            float damage = (float)(this.DAMAGE.get() + this.AMP_DAMAGE.get() * spellStats.getAmpMultiplier());
            float mult = 0;
            float add = 0;
            int ticks = 120 + (int)Math.round(120 * spellStats.getDurationMultiplier());
            if(living.hasEffect(ModPotions.DEMONIC_CURSE.get())){
                mult += 3;
                add += 6;
            }
            if(living.hasEffect(ModPotions.ADRENALINE.get())){
                mult -= 3;
                add -= 3;
            }
            if(living.hasEffect(com.hollingsworth.arsnouveau.common.potions.ModPotions.SHOCKED_EFFECT.get())){
                mult += 3;
                add += 1;
                int amp = living.getEffect(com.hollingsworth.arsnouveau.common.potions.ModPotions.SHOCKED_EFFECT.get()).getAmplifier();
                mult += 1.75f * amp;
                add += 2 * amp;
            }
            if(living.hasEffect(com.hollingsworth.arsnouveau.common.potions.ModPotions.SNARE_EFFECT.get())){
                mult += 2f;
                add += 3;
            }

            //should go last always
            if(living.hasEffect(ModPotions.BLOOD_CLOT.get())){
                //lol, multiplying the multiplier so as to increase other effects, but not have the rest grow each other exponentially
                //yea this is kinda silly but oh well
                mult = mult*1.5f;
                mult += 2;
                add += 4;
                int amp = living.getEffect(ModPotions.BLOOD_CLOT.get()).getAmplifier();
                mult += 2f * amp;
                add += 2 * amp;

                ticks = ticks*2;//more nausea, why not
            }

            //multiply some of the addition lol
            damage += add*0.15f;
            damage = damage * (1.0f + 0.1f*mult);
            damage += add*0.85f;

            this.attemptDamage(world,shooter,spellStats,spellContext,resolver,living,HEARTSTOP_DAMAGE(shooter), damage);
            living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, ticks));
        }
    }

    @Override
    protected void addDefaultAugmentLimits(Map<ResourceLocation, Integer> defaults) {
        defaults.put(AugmentAmplify.INSTANCE.getRegistryName(), 2);
    }

    @Override
    @Nonnull
    public Set<AbstractAugment> getCompatibleAugments() {
        return this.augmentSetOf(new AbstractAugment[]{AugmentAmplify.INSTANCE, AugmentDampen.INSTANCE, AugmentFortune.INSTANCE, AugmentExtendTime.INSTANCE});
    }

    @Override
    public void buildConfig(ForgeConfigSpec.Builder builder) {
        super.buildConfig(builder);
        this.addDamageConfig(builder, 2.0D);
        this.AMP_DAMAGE = builder.comment("Additional damage per amplify").defineInRange("amp_damage", 0.5D, 0.0D, 2.147483647E9D);
    }

    @Override
    public SpellTier getTier() {
        return SpellTier.THREE;
    }

    @Override
    @Nonnull
    public Set<SpellSchool> getSchools() {
        return this.setOf(new SpellSchool[]{Schools.LIFE});
    }
}
