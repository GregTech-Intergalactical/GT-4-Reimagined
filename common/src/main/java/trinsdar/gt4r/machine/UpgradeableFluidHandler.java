package trinsdar.gt4r.machine;

import muramasa.antimatter.capability.machine.MachineFluidHandler;
import muramasa.antimatter.tile.TileEntityMachine;
import muramasa.antimatter.util.Utils;
import net.minecraft.core.Direction;
import net.minecraftforge.fluids.FluidStack;
import tesseract.TesseractGraphWrappers;
import trinsdar.gt4r.data.CustomTags;
import trinsdar.gt4r.tile.single.TileEntitySteamMachine;

public class UpgradeableFluidHandler<T extends TileEntityMachine<T> & IUpgradeProvider> extends MachineFluidHandler<T> {

    public UpgradeableFluidHandler(T tile) {
        super(tile);
    }

    @Override
    public long fillDroplets(FluidStack stack, FluidAction action) {
        if (stack.getFluid().is(TileEntitySteamMachine.STEAM)){
            if (tile.getUpgrades().containsKey(CustomTags.STEAM_UPGRADES)){
                if (stack.getRealAmount() % TesseractGraphWrappers.dropletMultiplier == 0 && stack.getAmount() % 2 == 0){
                    long[] toDrain = new long[1];
                    toDrain[0] = 0;
                    tile.energyHandler.ifPresent(e -> {
                        long euToInject = Math.min(stack.getAmount() / 2, e.getCapacity() - e.getEnergy());
                        if (euToInject > 0){
                            if (action.execute()){
                                Utils.addEnergy(e, euToInject);
                            }
                            toDrain[0] = euToInject * 2;
                        }
                    });
                    if (toDrain[0] > 0){
                        return toDrain[0];
                    }

                }
            }
        }
        return super.fillDroplets(stack, action);
    }

    @Override
    public boolean canInput(Direction facing) {
        if (tile.getUpgrades().containsKey(CustomTags.STEAM_UPGRADES)) {
            return tile.getFacing().get3DDataValue() != facing.get3DDataValue() || tile.getMachineType().allowsFrontCovers();
        }
        return super.canInput(facing);
    }

    @Override
    public boolean canInput(FluidStack fluid, Direction direction) {
        if (fluid.getFluid().is(TileEntitySteamMachine.STEAM) && tile.getUpgrades().containsKey(CustomTags.STEAM_UPGRADES)) {
            return true;
        }
        return super.canInput(direction);
    }

    @Override
    public int getTanks() {
        int tanks = super.getTanks();
        if (tile.getUpgrades().containsKey(CustomTags.STEAM_UPGRADES) && tanks == 0) tanks = 1;
        return tanks;
    }
}