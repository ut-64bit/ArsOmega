package com.dkmk100.arsomega;

import com.dkmk100.arsomega.client.renderer.GenericBipedRenderer;
import com.dkmk100.arsomega.entities.EntityBossDemonKing;
import com.dkmk100.arsomega.entities.EntityDemonBasic;
import com.dkmk100.arsomega.util.ReflectionHandler;
import com.dkmk100.arsomega.util.RegistryHandler;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("arsomega")
public class ArsOmega
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "arsomega";
    public static ArrayList<Biome> biomes= new ArrayList<>();

    public ArsOmega() {
        RegistryHandler.registerGlyphs();
        RegistryHandler.registerRituals();

        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Block.class,RegistryHandler::registerBlocks);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::finalSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::RegisterEntityAttributes);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::RegisterMobRenderers);
        RegistryHandler.init();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }


    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Initializing Reflection Handler");
        try {
            ReflectionHandler.Initialize();
        }
        catch (Exception e){
            e.printStackTrace();
            LOGGER.log(Level.ERROR,e.getMessage());
            LOGGER.log(Level.ERROR,"Exception in reflection handler initialization, mod is likely now in a broken state");
        }

        //structures?
    }
    private void RegisterEntityAttributes(EntityAttributeCreationEvent event){
        event.put(RegistryHandler.BASIC_DEMON.get(), EntityDemonBasic.createAttributes().build());
        event.put(RegistryHandler.STRONG_DEMON.get(), EntityDemonBasic.createAttributes().build());
        event.put(RegistryHandler.BOSS_DEMON_KING.get(), EntityBossDemonKing.createAttributes().build());
    }
    private void clientSetup(final FMLClientSetupEvent event)
    {
        ItemBlockRenderTypes.setRenderLayer(RegistryHandler.POISON_FLOWER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(RegistryHandler.ARCANE_BLOOM.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(RegistryHandler.BRAMBLE_1.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(RegistryHandler.BRAMBLE_2.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(RegistryHandler.BRAMBLE_3.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(RegistryHandler.BRAMBLE_4.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(RegistryHandler.GORGON_FIRE.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(ItemsRegistry.INFINITY_JAR, RenderType.cutout());

        //RenderingRegistry.registerEntityRenderingHandler(RegistryHandler.VOID_BEAST.get(), VoidBeastRenderer::new);
    }
    private void RegisterMobRenderers(EntityRenderersEvent.RegisterRenderers event){
        RegisterMobRenderer(RegistryHandler.BASIC_DEMON.get(),"demon_basic",event);
        RegisterMobRenderer(RegistryHandler.STRONG_DEMON.get(),"demon_strong",event);
        RegisterMobRenderer(RegistryHandler.BOSS_DEMON_KING.get(),"boss_demon_king",event);
    }
    @OnlyIn(Dist.CLIENT)
    private void RegisterMobRenderer(EntityType<? extends Mob> entity, String registryName, EntityRenderersEvent.RegisterRenderers event){
        event.registerEntityRenderer(entity, (EntityRendererProvider.Context context) -> new GenericBipedRenderer(context, registryName));
    }
    private void finalSetup(final FMLLoadCompleteEvent event)
    {
        // some postinit code
    }

}
