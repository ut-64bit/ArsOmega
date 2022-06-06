package com.dkmk100.arsomega.events;

import com.dkmk100.arsomega.ArsOmega;
import com.dkmk100.arsomega.util.RegistryHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ArsOmega.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientRegisterEvents {

    @SubscribeEvent()
    public static void registerSpawnEggColors(ColorHandlerEvent.Block event)
    {
        event.getBlockColors().register((state, getter, pos, p1) -> RegistryHandler.CHALK_LINE_1.get().GetColor(state,pos, getter), RegistryHandler.CHALK_LINE_1.get());
        event.getBlockColors().register((state, getter, pos, p1) -> RegistryHandler.CHALK_LINE_2.get().GetColor(state,pos, getter), RegistryHandler.CHALK_LINE_2.get());
        event.getBlockColors().register((state, getter, pos, p1) -> RegistryHandler.CHALK_LINE_3.get().GetColor(state,pos, getter), RegistryHandler.CHALK_LINE_3.get());
        event.getBlockColors().register((state, getter, pos, p1) -> RegistryHandler.CHALK_LINE_4.get().GetColor(state,pos, getter), RegistryHandler.CHALK_LINE_4.get());

    }
}