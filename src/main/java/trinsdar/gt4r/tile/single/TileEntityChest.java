package trinsdar.gt4r.tile.single;

import muramasa.antimatter.capability.item.ITrackedHandler;
import muramasa.antimatter.gui.SlotType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.IChestLid;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import trinsdar.gt4r.block.BlockMaterialChest;
import trinsdar.gt4r.gui.ContainerCabinet;
import trinsdar.gt4r.machine.MaterialMachine;

import java.util.List;

@OnlyIn(value = Dist.CLIENT, _interface = IChestLid.class)
public class TileEntityChest extends TileEntityCabinet implements IChestLid, IInventory {
    protected float lidAngle;
    protected float prevLidAngle;
    protected int numPlayersUsing;
    private int ticksSinceSync;

    public TileEntityChest(MaterialMachine type, int ySize) {
        super(type, ySize);
    }

    @Override
    public void tick() {
        super.tick();
        int i = this.getBlockPos().getX();
        int j = this.getBlockPos().getY();
        int k = this.getBlockPos().getZ();
        ++this.ticksSinceSync;
        this.numPlayersUsing = getNumberOfPlayersUsing(this.level, this, this.ticksSinceSync, i, j, k, this.numPlayersUsing);
        this.prevLidAngle = this.lidAngle;
        float f = 0.1F;
        if (this.numPlayersUsing > 0 && this.lidAngle == 0.0F) {
            this.playSound(SoundEvents.CHEST_OPEN);
        }

        if (this.numPlayersUsing == 0 && this.lidAngle > 0.0F || this.numPlayersUsing > 0 && this.lidAngle < 1.0F) {
            float f1 = this.lidAngle;
            if (this.numPlayersUsing > 0) {
                this.lidAngle += 0.1F;
            } else {
                this.lidAngle -= 0.1F;
            }

            if (this.lidAngle > 1.0F) {
                this.lidAngle = 1.0F;
            }

            float f2 = 0.5F;
            if (this.lidAngle < 0.5F && f1 >= 0.5F) {
                this.playSound(SoundEvents.CHEST_CLOSE);
            }

            if (this.lidAngle < 0.0F) {
                this.lidAngle = 0.0F;
            }
        }
    }

    @Override
    public boolean canPlayerOpenGui(PlayerEntity playerEntity) {
        return super.canPlayerOpenGui(playerEntity) && !isChestBlockedAt(playerEntity.level, this.getBlockPos());
    }

    public static boolean isChestBlockedAt(IWorld p_220108_0_, BlockPos p_220108_1_) {
        return isBlockedChestByBlock(p_220108_0_, p_220108_1_) || isCatSittingOnChest(p_220108_0_, p_220108_1_);
    }

    private static boolean isBlockedChestByBlock(IBlockReader pLevel, BlockPos pPos) {
        BlockPos blockpos = pPos.above();
        return pLevel.getBlockState(blockpos).isRedstoneConductor(pLevel, blockpos);
    }

    private static boolean isCatSittingOnChest(IWorld pLevel, BlockPos pPos) {
        List<CatEntity> list = pLevel.getEntitiesOfClass(CatEntity.class, new AxisAlignedBB((double)pPos.getX(), (double)(pPos.getY() + 1), (double)pPos.getZ(), (double)(pPos.getX() + 1), (double)(pPos.getY() + 2), (double)(pPos.getZ() + 1)));
        if (!list.isEmpty()) {
            for(CatEntity catentity : list) {
                if (catentity.isInSittingPose()) {
                    return true;
                }
            }
        }

        return false;
    }

    public static int getNumberOfPlayersUsing(World worldIn, TileEntityChest lockableTileEntity, int ticksSinceSync, int x, int y, int z, int numPlayersUsing) {
        if (!worldIn.isClientSide && numPlayersUsing != 0 && (ticksSinceSync + x + y + z) % 200 == 0) {
            numPlayersUsing = getNumberOfPlayersUsing(worldIn, lockableTileEntity, x, y, z);
        }

        return numPlayersUsing;
    }

    public static int getNumberOfPlayersUsing(World world, TileEntityChest lockableTileEntity, int x, int y, int z) {
        int i = 0;

        for (PlayerEntity playerentity : world.getEntitiesOfClass(PlayerEntity.class, new AxisAlignedBB((double) ((float) x - 5.0F), (double) ((float) y - 5.0F), (double) ((float) z - 5.0F), (double) ((float) (x + 1) + 5.0F), (double) ((float) (y + 1) + 5.0F), (double) ((float) (z + 1) + 5.0F)))) {
            if (playerentity.containerMenu instanceof ContainerCabinet) {
                ContainerCabinet<?> chest = (ContainerCabinet<?>)playerentity.containerMenu;
                if (chest.handler.handler instanceof TileEntityChest){
                    if (((TileEntityChest)chest.handler.handler).getBlockPos().equals(lockableTileEntity.getBlockPos())){
                        ++i;
                    }
                }
            }
        }

        return i;
    }

    private void playSound(SoundEvent soundIn) {
        double d0 = (double) this.getBlockPos().getX() + 0.5D;
        double d1 = (double) this.getBlockPos().getY() + 0.5D;
        double d2 = (double) this.getBlockPos().getZ() + 0.5D;

        this.getLevel().playSound((PlayerEntity) null, d0, d1, d2, soundIn, SoundCategory.BLOCKS, 0.5F, this.getLevel().random.nextFloat() * 0.1F + 0.9F);
    }


    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            this.numPlayersUsing = type;
            return true;
        }
        else return super.triggerEvent(id, type);
    }

    protected void onOpenOrClose() {
        Block block = this.getBlockState().getBlock();

        if (block instanceof BlockMaterialChest) {
            this.getLevel().blockEvent(this.getBlockPos(), block, 1, this.numPlayersUsing);
            this.getLevel().updateNeighborsAt(this.getBlockPos(), block);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public float getOpenNess(float partialTicks) {
        return MathHelper.lerp(partialTicks, this.prevLidAngle, this.lidAngle);
    }

    public static int getPlayersUsing(IBlockReader reader, BlockPos posIn) {
        BlockState blockstate = reader.getBlockState(posIn);
        if (blockstate.hasTileEntity()) {
            TileEntity tileentity = reader.getBlockEntity(posIn);
            if (tileentity instanceof TileEntityChest) {
                return ((TileEntityChest) tileentity).numPlayersUsing;
            }
        }

        return 0;
    }

    @Override
    public void startOpen(PlayerEntity player) {
        if (!player.isSpectator()) {
            if (this.numPlayersUsing < 0) {
                this.numPlayersUsing = 0;
            }

            ++this.numPlayersUsing;
            this.onOpenOrClose();
            player.awardStat(this.getOpenChestStat());
        }
    }

    protected Stat<ResourceLocation> getOpenChestStat() {
        return Stats.CUSTOM.get(Stats.OPEN_CHEST);
    }


    @Override
    public void stopOpen(PlayerEntity player) {
        if (!player.isSpectator()) {
            --this.numPlayersUsing;
            this.onOpenOrClose();
        }
    }

    @Override
    public int getContainerSize() {
        return itemHandler.map(i -> i.getHandler(SlotType.STORAGE).getSlots()).orElse(0);
    }

    @Override
    public boolean isEmpty() {
        return itemHandler.map(i -> {
            int fill = 0;
            ITrackedHandler trackedHandler = i.getHandler(SlotType.STORAGE);
            for (int slot = 0; slot < trackedHandler.getSlots(); slot ++){
                if (!trackedHandler.getStackInSlot(slot).isEmpty()){
                    fill ++;
                }
            }
            return fill == 0;
        }).orElse(true);
    }

    @Override
    public ItemStack getItem(int pIndex) {
        return itemHandler.map(i -> i.getHandler(SlotType.STORAGE).getStackInSlot(pIndex)).orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack removeItem(int pIndex, int pCount) {
        return itemHandler.map(i -> i.getHandler(SlotType.STORAGE).extractItem(pIndex, pCount, false)).orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack removeItemNoUpdate(int pIndex) {
        return itemHandler.map(i -> i.getHandler(SlotType.STORAGE).extractItem(pIndex, 1, true)).orElse(ItemStack.EMPTY);
    }

    @Override
    public void setItem(int pIndex, ItemStack pStack) {
        itemHandler.ifPresent(i -> i.getHandler(SlotType.STORAGE).setStackInSlot(pIndex, pStack));
    }

    @Override
    public void setChanged() {

    }

    @Override
    public boolean stillValid(PlayerEntity pPlayer) {
        return true;
    }

    @Override
    public void clearContent() {
    }
}
