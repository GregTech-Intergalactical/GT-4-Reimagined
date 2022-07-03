package trinsdar.gt4r.data;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import muramasa.antimatter.AntimatterAPI;
import muramasa.antimatter.Data;
import muramasa.antimatter.item.ItemBattery;
import muramasa.antimatter.material.Material;
import muramasa.antimatter.recipe.ingredient.PropertyIngredient;
import muramasa.antimatter.recipe.material.MaterialRecipe;
import muramasa.antimatter.tool.AntimatterToolType;
import muramasa.antimatter.tool.IAntimatterTool;
import muramasa.antimatter.util.TagUtils;
import muramasa.antimatter.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import tesseract.api.capability.TesseractGTCapability;
import tesseract.api.gt.IGTNode;
import trinsdar.gt4r.Ref;
import trinsdar.gt4r.items.ItemPowerUnit;
import trinsdar.gt4r.items.MaterialRockCutter;
import trinsdar.gt4r.items.MaterialSpear;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static muramasa.antimatter.Data.NULL;
import static net.minecraft.world.level.material.Material.*;
import static trinsdar.gt4r.data.GT4RData.*;

public class ToolTypes {
    public static final MaterialRecipe.Provider POWERED_TOOL_BUILDER = MaterialRecipe.registerProvider("powered-tool", Ref.ID, id -> new MaterialRecipe.ItemBuilder() {

        @Override
        public ItemStack build(CraftingContainer inv, MaterialRecipe.Result mats) {
            Material m = (Material) mats.mats.get("secondary");
            Tuple<Long, Long> battery = (Tuple<Long, Long>) mats.mats.get("battery");
            String domain = id.equals(ROCK_CUTTER.getId()) ? Ref.ID : Ref.ANTIMATTER;
            IAntimatterTool type = AntimatterAPI.get(IAntimatterTool.class, id.replace('-', '_'), domain);
            return type.resolveStack((Material) mats.mats.get("primary"), m == null ? NULL : m, battery.getA(), battery.getB());
        }

        @Override
        public Map<String, Object> getFromResult(@Nonnull ItemStack stack) {
            CompoundTag nbt = stack.getTag().getCompound(muramasa.antimatter.Ref.TAG_TOOL_DATA);
            Material primary = AntimatterAPI.get(Material.class, nbt.getString(muramasa.antimatter.Ref.KEY_TOOL_DATA_PRIMARY_MATERIAL));
            Material secondary = AntimatterAPI.get(Material.class, nbt.getString(muramasa.antimatter.Ref.KEY_TOOL_DATA_SECONDARY_MATERIAL));
            return ImmutableMap.of("primary", primary, "secondary", secondary, "energy", getEnergy(stack).getA(), "maxEnergy", getEnergy(stack).getB());
        }
    });

    public static final MaterialRecipe.Provider UNIT_POWERED_TOOL_BUILDER = MaterialRecipe.registerProvider("powered-tool-from-unit", Ref.ID, id -> new MaterialRecipe.ItemBuilder() {

        @Override
        public ItemStack build(CraftingContainer inv, MaterialRecipe.Result mats) {
            Tuple<Long, Tuple<Long, Material>> t = (Tuple<Long, Tuple<Long, Material>>) mats.mats.get("secondary");
            IAntimatterTool type = AntimatterAPI.get(IAntimatterTool.class, id.replace('-', '_'), Ref.ANTIMATTER);
            t.getB().getB();
            return type.resolveStack((Material) mats.mats.get("primary"), t.getB().getB(), t.getA(), t.getB().getA());
        }

        @Override
        public Map<String, Object> getFromResult(@Nonnull ItemStack stack) {
            return ImmutableMap.of();
        }
    });

    public static AntimatterToolType SPEAR = new SpearToolType(Ref.ID, "spear_gt", 2, 1, 10, 3.0F, -2.9F).setUseAction(UseAnim.SPEAR);
    public static AntimatterToolType ROCK_CUTTER = new RockCutterToolType(Ref.ID, "rock_cutter", 1, 1, 1, -1.0F, -3.0F).setPowered(100000L, 1).setRepairability(false).setOverlayLayers(2).addEffectiveMaterials(ICE_SOLID, METAL, STONE, HEAVY_METAL, PISTON).setBrokenItems(ImmutableMap.of("rock_cutter", i -> getBrokenItem(i, RockCutterPowerUnit)));

    static {
        PropertyIngredient.addGetter(CustomTags.BATTERIES_RE.location(), ToolTypes::getEnergy);
        PropertyIngredient.addGetter(CustomTags.BATTERIES_SMALL.location(), ToolTypes::getEnergy);
        PropertyIngredient.addGetter(CustomTags.BATTERIES_MEDIUM.location(), ToolTypes::getEnergy);
        PropertyIngredient.addGetter(CustomTags.BATTERIES_LARGE.location(), ToolTypes::getEnergy);
        PropertyIngredient.addGetter(CustomTags.POWER_UNIT_LV.location(), ToolTypes::getEnergyAndMat);
        PropertyIngredient.addGetter(CustomTags.POWER_UNIT_MV.location(), ToolTypes::getEnergyAndMat);
        PropertyIngredient.addGetter(CustomTags.POWER_UNIT_HV.location(), ToolTypes::getEnergyAndMat);
        PropertyIngredient.addGetter(CustomTags.POWER_UNIT_SMALL.location(), ToolTypes::getEnergyAndMat);
        PropertyIngredient.addGetter(CustomTags.POWER_UNIT_ROCK_CUTTER.location(), ToolTypes::getEnergyAndMat);
    }

    public static void init(){
        //temp method till this behavior has a hotkey
        Data.DRILL.removeBehaviour("aoe_break");
        Data.DRILL.setBrokenItems(ImmutableMap.of("drill_lv", i -> getBrokenItem(i, PowerUnitLV), "drill_mv", i -> getBrokenItem(i, PowerUnitMV), "drill_hv", i -> getBrokenItem(i, PowerUnitHV)));
        Data.CHAINSAW.setBrokenItems(ImmutableMap.of("chainsaw_lv", i -> getBrokenItem(i, PowerUnitLV), "chainsaw_mv", i -> getBrokenItem(i, PowerUnitMV), "chainsaw_hv", i -> getBrokenItem(i, PowerUnitHV)));
        Data.ELECTRIC_WRENCH.setBrokenItems(ImmutableMap.of("electric_wrench_lv", i -> getBrokenItem(i, PowerUnitLV), "electric_wrench_mv", i -> getBrokenItem(i, PowerUnitMV), "electric_wrench_hv", i -> getBrokenItem(i, PowerUnitHV)));
        Data.BUZZSAW.setBrokenItems(ImmutableMap.of("buzzsaw_lv", i -> getBrokenItem(i, PowerUnitLV), "buzzsaw_mv", i -> getBrokenItem(i, PowerUnitMV), "buzzsaw_hv", i -> getBrokenItem(i, PowerUnitHV)));
        Data.ELECTRIC_SCREWDRIVER.setBrokenItems(ImmutableMap.of("electric_screwdriver_lv", i -> getBrokenItem(i, SmallPowerUnit)));
        Data.JACKHAMMER.setBrokenItems(ImmutableMap.of("jackhammer_lv", i -> getBrokenItem(i, SmallPowerUnit)));
        Data.WRENCH.addEffectiveBlocks(Blocks.HOPPER);
        Data.ELECTRIC_WRENCH.addEffectiveBlocks(Blocks.HOPPER);
    }

    private static ItemStack getBrokenItem(ItemStack tool, ItemLike broken){
        ItemStack powerUnit = new ItemStack(broken);
        Tuple<Long, Long> tuple = getEnergy(tool);
        CompoundTag dataTag = powerUnit.getOrCreateTagElement(muramasa.antimatter.Ref.TAG_TOOL_DATA);
        dataTag.putLong(muramasa.antimatter.Ref.KEY_TOOL_DATA_ENERGY, tuple.getA());
        dataTag.putLong(muramasa.antimatter.Ref.KEY_TOOL_DATA_MAX_ENERGY, tuple.getB());
        if (broken.asItem() == PowerUnitHV || broken.asItem() == SmallPowerUnit){
            PowerUnitHV.setMaterial(((IAntimatterTool)tool.getItem()).getSecondaryMaterial(tool), powerUnit);
        }
        return powerUnit;
    }

    public static Tuple<Long, Long> getEnergy(ItemStack stack){
        if (stack.getItem() instanceof ItemBattery){
            long energy = stack.getCapability(TesseractGTCapability.ENERGY_HANDLER_CAPABILITY).map(IGTNode::getEnergy).orElse((long)0);
            return new Tuple<>(energy, ((ItemBattery)stack.getItem()).getCapacity());
        }
        if (stack.getItem() instanceof IAntimatterTool){
            IAntimatterTool tool = (IAntimatterTool) stack.getItem();
            if (tool.getAntimatterToolType().isPowered()){
                long currentEnergy = tool.getCurrentEnergy(stack);
                long maxEnergy = tool.getMaxEnergy(stack);
                return new Tuple<>(currentEnergy, maxEnergy);
            }
        }
        return null;
    }

    public static Tuple<Long, Tuple<Long, Material>> getEnergyAndMat(ItemStack stack){
        if (stack.getItem() instanceof ItemPowerUnit){
            ItemPowerUnit tool = (ItemPowerUnit) stack.getItem();
            long currentEnergy = tool.getCurrentEnergy(stack);
            long maxEnergy = tool.getMaxEnergy(stack);
            return new Tuple<>(currentEnergy, new Tuple<>(maxEnergy, tool.getMaterial(stack)));
        }
        return null;
    }

    public static class SpearToolType extends AntimatterToolType{

        private TagKey<Item> tag, forgeTag; // Set?
        public SpearToolType(String domain, String id, int useDurability, int attackDurability, int craftingDurability, float baseAttackDamage, float baseAttackSpeed) {
            super(domain, id, useDurability, attackDurability, craftingDurability, baseAttackDamage, baseAttackSpeed, false);
            this.tag = TagUtils.getItemTag(new ResourceLocation(Ref.ANTIMATTER, "spear"));
            this.forgeTag = TagUtils.getForgelikeItemTag("tools/".concat("spear"));
        }

        @Override
        public IAntimatterTool instantiateTools(String domain) {
            return new MaterialSpear(domain, this, prepareInstantiation(domain));
        }

        @Override
        public TagKey<Item> getTag() {
            return tag;
        }

        @Override
        public TagKey<Item> getForgeTag() {
            return forgeTag;
        }

        @Override
        public ItemStack getToolStack(Material primary, Material secondary) {
            return Objects.requireNonNull(AntimatterAPI.get(IAntimatterTool.class, "spear", getDomain())).asItemStack(primary, secondary);
        }

        @Override
        public IAntimatterTool instantiateTools(String domain, Supplier<Item.Properties> properties) {
            return new MaterialSpear(domain, this, properties.get());
        }

        private Item.Properties prepareInstantiation(String domain) {
            if (domain.isEmpty())
                Utils.onInvalidData("An AntimatterToolType was instantiated with an empty domain name!");
            Item.Properties properties = new Item.Properties().tab(getItemGroup());
            if (!getRepairability()) properties.setNoRepair();
            return properties;
        }
    }

    public static class RockCutterToolType extends AntimatterToolType{

        public RockCutterToolType(String domain, String id, int useDurability, int attackDurability, int craftingDurability, float baseAttackDamage, float baseAttackSpeed) {
            super(domain, id, useDurability, attackDurability, craftingDurability, baseAttackDamage, baseAttackSpeed, false);
            setType(Data.PICKAXE);
        }

        @Override
        public List<IAntimatterTool> instantiatePoweredTools(String domain) {
            List<IAntimatterTool> poweredTools = new ObjectArrayList<>();
            Item.Properties properties = prepareInstantiation(domain);
            poweredTools.add(new MaterialRockCutter(domain, this, properties, 1));
            return poweredTools;
        }

        @Override
        public List<IAntimatterTool> instantiatePoweredTools(String domain, Supplier<Item.Properties> properties) {
            List<IAntimatterTool> poweredTools = new ObjectArrayList<>();
            poweredTools.add(new MaterialRockCutter(domain, this, properties.get(), 1));
            return poweredTools;
        }

        private Item.Properties prepareInstantiation(String domain) {
            if (domain.isEmpty())
                Utils.onInvalidData("An AntimatterToolType was instantiated with an empty domain name!");
            Item.Properties properties = new Item.Properties().tab(getItemGroup());
            if (!getRepairability()) properties.setNoRepair();
            return properties;
        }
    }
}