package trinsdar.gt4r;

import muramasa.antimatter.AntimatterAPI;
import muramasa.antimatter.AntimatterDynamics;
import muramasa.antimatter.AntimatterMod;
import muramasa.antimatter.datagen.providers.AntimatterBlockStateProvider;
import muramasa.antimatter.proxy.IProxyHandler;
import muramasa.antimatter.registration.RegistrationEvent;
import muramasa.antimatter.registration.Side;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import trinsdar.gt4r.client.BakedModels;
import trinsdar.gt4r.config.OreConfigHandler;
import trinsdar.gt4r.data.Attributes;
import trinsdar.gt4r.data.GT4RData;
import trinsdar.gt4r.data.Guis;
import trinsdar.gt4r.data.Machines;
import trinsdar.gt4r.data.Materials;
import trinsdar.gt4r.data.MenuHandlers;
import trinsdar.gt4r.data.Models;
import trinsdar.gt4r.data.RecipeMaps;
import trinsdar.gt4r.data.Structures;
import trinsdar.gt4r.data.ToolTypes;
import trinsdar.gt4r.datagen.GT4RItemModelProvider;
import trinsdar.gt4r.datagen.GT4RLocalizations;
import trinsdar.gt4r.datagen.GT4RRandomDropBonus;
import trinsdar.gt4r.tile.TileEntityTypes;
import trinsdar.gt4r.tree.RubberTreeWorldGen;
import trinsdar.gt4r.worldgen.GT4RPlacedFeatures;
import trinsdar.gt4r.worldgen.GT4RFeatures;


public class GT4Reimagined extends AntimatterMod {

    public static GT4Reimagined INSTANCE;
    public static IProxyHandler PROXY;
    public static Logger LOGGER = LogManager.getLogger(Ref.ID);

    public GT4Reimagined() {
        super();
        INSTANCE = this;
    }

    @Override
    public void onRegistrarInit() {
        super.onRegistrarInit();
        AntimatterDynamics.clientProvider(Ref.ID, g -> new AntimatterBlockStateProvider(Ref.ID, Ref.NAME + " BlockStates", g));
        AntimatterDynamics.clientProvider(Ref.ID, g -> new GT4RItemModelProvider(Ref.ID, Ref.NAME + " Item Models", g));
        AntimatterDynamics.clientProvider(Ref.ID, GT4RLocalizations.en_US::new);
    }

    @Override
    public void onRegistrationEvent(RegistrationEvent event, Side side) {
        switch (event) {
            case DATA_INIT:
                ToolTypes.init();
                Materials.init();
                Attributes.init();
                RecipeMaps.init();
                MenuHandlers.init();
                GT4RData.init(side);
                Machines.init();
                GT4RFeatures.init();
                RecipeMaps.postInit();
                TileEntityTypes.init();
                Guis.init(side);
                Models.init();
                if (side == Side.CLIENT){
                    BakedModels.init();
                }
                Registry.register(Registry.LOOT_FUNCTION_TYPE, new ResourceLocation(Ref.ID, "random_drop_bonus"), GT4RRandomDropBonus.RANDOM_DROP_BONUS);
                break;
            case DATA_READY:
                if (AntimatterAPI.isModLoaded(Ref.MOD_BLUEPOWER)){
                    //GEM.forceOverride(Amethyst, ForgeRegistries.ITEMS.getValue(new ResourceLocation("bluepower", "amethyst_gem")));
                }
                GT4RData.buildTierMaps();
                Structures.init();
                Structures.initPatterns();
                RubberTreeWorldGen.init();
                GT4RPlacedFeatures.init();
                OreConfigHandler.ORE_CONFIG_HANDLER.save();
                //GregTechAPI.registerFluidCell(Data.CellTin.get(1));
                //GregTechAPI.registerFluidCell(Data.CellSteel.get(1));
                //GregTechAPI.registerFluidCell(Data.CellTungstensteel.get(1));

//                AntimatterAPI.registerCover(Data.COVER_PLATE);
//                AntimatterAPI.registerCover(Data.COVER_CONVEYOR);
//                AntimatterAPI.registerCover(Data.COVER_PUMP);

//                AntimatterAPI.registerCoverStack(Data.ConveyorLV.get(1), new CoverConveyor(Tier.LV));
//                AntimatterAPI.registerCoverStack(Data.ConveyorMV.get(1), new CoverConveyor(Tier.MV));
//                AntimatterAPI.registerCoverStack(Data.ConveyorHV.get(1), new CoverConveyor(Tier.HV));
//                AntimatterAPI.registerCoverStack(Data.ConveyorEV.get(1), new CoverConveyor(Tier.EV));
//                AntimatterAPI.registerCoverStack(Data.ConveyorIV.get(1), new CoverConveyor(Tier.IV));
//                AntimatterAPI.registerCoverStack(Data.PumpLV.get(1), new CoverPump(Tier.LV));
//                AntimatterAPI.registerCoverStack(Data.PumpMV.get(1), new CoverPump(Tier.MV));
//                AntimatterAPI.registerCoverStack(Data.PumpHV.get(1), new CoverPump(Tier.HV));
//                AntimatterAPI.registerCoverStack(Data.PumpEV.get(1), new CoverPump(Tier.EV));
//                AntimatterAPI.registerCoverStack(Data.PumpIV.get(1), new CoverPump(Tier.IV));
//                MaterialType.PLATE.all().forEach(m -> AntimatterAPI.registerCoverStack(MaterialType.PLATE.get(m, 1), Data.COVER_PLATE));
                break;
            case WORLDGEN_INIT:
                //WorldGenLoader.init();
                break;
        }
    }

    @Override
    public int getPriority() {
        return 800;
    }

    @Override
    public String getId() {
        return Ref.ID;
    }
}