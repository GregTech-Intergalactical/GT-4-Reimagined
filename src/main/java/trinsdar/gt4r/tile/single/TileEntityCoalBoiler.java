package trinsdar.gt4r.tile.single;

import muramasa.antimatter.gui.event.IGuiEvent;
import muramasa.antimatter.machine.MachineState;
import muramasa.antimatter.machine.event.ContentEvent;
import muramasa.antimatter.machine.types.Machine;
import muramasa.antimatter.material.Material;
import muramasa.antimatter.tile.TileEntityMachine;
import muramasa.antimatter.util.LazyHolder;
import muramasa.antimatter.util.Utils;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.world.Explosion;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import trinsdar.gt4r.data.Materials;
import trinsdar.gt4r.machine.CoalBoilerFluidHandler;
import trinsdar.gt4r.machine.CoalBoilerRecipeHandler;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static muramasa.antimatter.Data.DUST;
import static muramasa.antimatter.Data.GEM;
import static muramasa.antimatter.machine.Tier.BRONZE;
import static trinsdar.gt4r.data.Materials.Steam;

public class TileEntityCoalBoiler extends TileEntityMachine {
    int maxHeat = 500, heat, fuel = 0, maxFuel, lossTimer = 0;
    boolean hadNoWater;
    public TileEntityCoalBoiler(Machine<?> type) {
        super(type);
        this.fluidHandler = LazyHolder.of(() -> new CoalBoilerFluidHandler(this));
        this.recipeHandler = LazyHolder.of(() -> new CoalBoilerRecipeHandler(this));
    }

    public int getFuel() {
        AtomicInteger v = new AtomicInteger();
        recipeHandler.ifPresent(r -> {
            v.set(((CoalBoilerRecipeHandler) r).getFuel());
        });
        return v.get();
    }

    public int getHeat() {
        AtomicInteger v = new AtomicInteger();
        recipeHandler.ifPresent(r -> {
            v.set(((CoalBoilerRecipeHandler) r).getHeat());
        });
        return v.get();
    }

    public int getMaxFuel() {
        AtomicInteger v = new AtomicInteger();
        recipeHandler.ifPresent(r -> {
            v.set(((CoalBoilerRecipeHandler) r).getMaxFuel());
        });
        return v.get();
    }

    public int getMaxHeat() {
        AtomicInteger v = new AtomicInteger();
        recipeHandler.ifPresent(r -> {
            v.set(((CoalBoilerRecipeHandler) r).getMaxHeat());
        });
        return v.get();
    }

    @Override
    public boolean setOutputFacing(Direction side) {
        return false;
    }

    @Override
    public void onGuiEvent(IGuiEvent event, int... data) {
        super.onGuiEvent(event, data);
    }

    //@Override
    public void onServerUpdate2() {
        super.onServerUpdate();
//        Antimatter.LOGGER.info("World Time: " + world.getGameTime());
//        Antimatter.LOGGER.info("Heat: " + heat);
//        Antimatter.LOGGER.info("Fuel: " + fuel);
        AtomicBoolean update = new AtomicBoolean(false);
        if (this.heat <= 20) {
            int oldHeat = heat;
            this.heat = 20;
            if (oldHeat != heat) update.set(true);
            this.lossTimer = 0;
        }
        if (++this.lossTimer > 45) {
            int oldHeat = heat;
            this.heat -= 1;
            if (oldHeat != heat) update.set(true);
            this.lossTimer = 0;
        }
        //Arrays.stream(Direction.values()).filter(f -> f != Direction.DOWN).collect(Collectors.toList()).forEach(facing -> GTUtility.exportFluidFromMachineToSide(this, steam, facing, steam.getFluidAmount()));
        if (this.getWorld().getGameTime() % 20 == 0){
            fluidHandler.ifPresent(f -> {
                FluidStack[] inputs = f.getInputs();
                if (this.heat > 100){
                    if (inputs[0].getAmount() == 0){
                        hadNoWater = true;
                    } else {
                        if (hadNoWater){
                            world.createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), 4.0F, Explosion.Mode.DESTROY);
                            this.world.setBlockState(pos, Blocks.AIR.getDefaultState());
                            return;
                        }
                        f.drainInput(new FluidStack(Fluids.WATER, 1), IFluidHandler.FluidAction.EXECUTE);
                        int room = 16000 - f.getOutputs()[0].getAmount();
                        int rate = tier == BRONZE ? 150 : 300;
                        int fill = Math.min(room, rate);
                        if (room > 0){
                            f.fillOutput(Steam.getGas(fill), IFluidHandler.FluidAction.EXECUTE);
                            update.set(true);
                        }
                        if (fill < rate){
                            //TODO:steam sounds
                            //getNetwork().initiateTileEntityEvent(this, 3, false);
                            f.drain(4000, IFluidHandler.FluidAction.EXECUTE);
                            update.set(true);
                        }
                    }
                } else {
                    this.hadNoWater = false;
                }
            });
        }
        itemHandler.ifPresent(i ->{
            boolean byproductStuffed = false;
            IItemHandlerModifiable outputs = i.getOutputHandler();
            IItemHandlerModifiable inputs = i.getInputHandler();
            ItemStack byproductStack = outputs.getStackInSlot(0);

            if (!byproductStack.isEmpty() && byproductStack.getCount() == byproductStack.getMaxStackSize()){
                byproductStuffed = true;
            }
            if (this.fuel <= 0 && !inputs.getStackInSlot(0).isEmpty() && !byproductStuffed){
                int fuelEnergy = 0;
                Material byproduct = Materials.Ash;
                boolean validFuel = false;
                ItemStack fuelStack = inputs.getStackInSlot(0);
                if (Utils.equals(fuelStack, new ItemStack(Items.COAL, 1))){
                    this.maxFuel = 160;
                    update.set(true);
                    byproduct = Materials.DarkAsh;
                    validFuel = true;
                } else if (Utils.equals(fuelStack, new ItemStack(Items.CHARCOAL, 1))){
                    this.maxFuel = 160;
                    update.set(true);
                    validFuel = true;
                } else if (Utils.equals(fuelStack, GEM.get(Materials.CoalCoke, 1))){
                    this.maxFuel = 320;
                    update.set(true);
                    validFuel = true;
                }

                if (validFuel){
                    this.fuel += maxFuel;
                    inputs.getStackInSlot(0).shrink(1);
                    if (outputs.getStackInSlot(0).isEmpty()){
                        i.addOutputs(DUST.get(byproduct, 1));
                    } else {
                        ItemStack toAdd = DUST.get(byproduct, 1);
                        if (Utils.equals(toAdd, outputs.getStackInSlot(0))){
                            i.addOutputs(DUST.get(byproduct, 1));
                        } else {
                            i.getOutputs()[0].shrink(outputs.getStackInSlot(0).getCount());
                            i.addOutputs(DUST.get(Materials.Ash, outputs.getStackInSlot(0).getCount() + 1));
                        }
                    }
                }
            }

        });
        if ((this.heat < 500) && (this.fuel > 0) && (world.getGameTime() % 12L == 0L)) {
            this.fuel -= 1;
            this.heat += 1;
            update.set(true);
        }
        if (update.get()){
            this.onMachineEvent(ContentEvent.FLUID_OUTPUT_CHANGED, "null");
        }
        this.setActive(this.fuel > 0);
    }

    public void setActive(boolean t){
        if (t && this.getMachineState() != MachineState.ACTIVE){
            this.setMachineState(MachineState.ACTIVE);
        } else if (!t && this.getMachineState() == MachineState.ACTIVE){
            this.setMachineState(MachineState.IDLE);
        }
    }
}