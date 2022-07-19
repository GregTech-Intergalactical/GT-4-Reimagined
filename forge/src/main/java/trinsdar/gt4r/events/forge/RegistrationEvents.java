package trinsdar.gt4r.events.forge;

import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.IForgeRegistry;
import trinsdar.gt4r.GT4RConfig;
import trinsdar.gt4r.Ref;
import trinsdar.gt4r.data.Attributes;
import trinsdar.gt4r.data.GT4RData;
import trinsdar.gt4r.tree.RubberFoliagePlacer;
import trinsdar.gt4r.tree.RubberTree;

@Mod.EventBusSubscriber(modid = Ref.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistrationEvents {
    @SubscribeEvent
    public static void onRegisterEntities(final RegistryEvent.Register<EntityType<?>> e){
        IForgeRegistry<EntityType<?>> reg = e.getRegistry();
        reg.register(GT4RData.SPEAR_ENTITY_TYPE.setRegistryName(new ResourceLocation(Ref.ID, "spear")));
    }

    @SubscribeEvent
    public static void onRegisterFoilagePlacers(final RegistryEvent.Register<FoliagePlacerType<?>> e){
        e.getRegistry().register(RubberFoliagePlacer.RUBBER.setRegistryName(Ref.ID, "rubber_foilage_placer"));
    }

    @SubscribeEvent
    public static void onRegisterAttributes(final RegistryEvent.Register<Attribute> e){
        e.getRegistry().register(Attributes.ATTACK_REACH.setRegistryName(Ref.ID, "attack_reach"));
    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfigEvent e) {
        GT4RConfig.onModConfigEvent(e.getConfig());
    }
}
