package best.lettuce.utils.player;

import best.lettuce.utils.MinecraftInstance;
import com.google.common.collect.Multimap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

@UtilityClass
public class InventoryUtils implements MinecraftInstance {

    public final int INCLUDE_ARMOR_BEGIN = 5, EXCLUDE_ARMOR_BEGIN = 9, ONLY_HOT_BAR_BEGIN = 36, END = 45;

    public int getBlockSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack is = mc.thePlayer.inventory.mainInventory[i];
            if (is != null && is.getItem() instanceof ItemBlock) return i;
        }
        return -1;
    }

    public ItemStack getStackInSlot(int slot) {
        return mc.thePlayer.inventoryContainer.getSlot(slot).getStack();
    }

    public int getItemSlot(Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack is = mc.thePlayer.inventory.mainInventory[i];
            if (is != null && is.getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    public int findItemInInventory(Item item) {
        for (int i = 0; i < 36; i++) {
            ItemStack is = mc.thePlayer.inventory.mainInventory[i];
            if (is != null && is.getItem() == item) return i;
        }
        return -1;
    }

    public int findSlotMatching(final EntityPlayerSP player, final Predicate<ItemStack> cond) {
        for (int i = END - 1; i >= EXCLUDE_ARMOR_BEGIN; i--) {
            final ItemStack stack = player.inventoryContainer.getSlot(i).getStack();
            if (cond.test(stack)) return i;
        }

        return -1;
    }

    public boolean hasFreeSlots(final EntityPlayerSP player) {
        for (int i = EXCLUDE_ARMOR_BEGIN; i < END; i++) {
            if (!player.inventoryContainer.getSlot(i).getHasStack()) return true;
        }

        return false;
    }

    public boolean isValidStack(final EntityPlayerSP player, final ItemStack stack) {
        if (stack == null) return false;

        final Item item = stack.getItem();

        if (item instanceof ItemSword) return isBestSword(player, stack);
        else if (item instanceof ItemArmor) return isBestArmor(player, stack);
        else if (item instanceof ItemTool) return isBestTool(player, stack);
        else if (item instanceof ItemBow) return isBestBow(player, stack);
        else if (item instanceof ItemFood) return isGoodFood(stack);
        else if (item instanceof ItemBlock) return isStackValidToPlace(stack);
        else if (item instanceof ItemPotion) return isBuffPotion(stack);
        else return isGoodItem(item);
    }

    public boolean isGoodItem(final Item item) {
        return item instanceof ItemEnderPearl || item == Items.arrow;
    }

    public boolean isBestSword(final EntityPlayerSP player, final ItemStack itemStack) {
        double damage = 0.0;
        ItemStack bestStack = null;

        for (int i = EXCLUDE_ARMOR_BEGIN; i < END; i++) {
            final ItemStack stack = player.inventoryContainer.getSlot(i).getStack();

            if (stack != null && stack.getItem() instanceof ItemSword) {
                double newDamage = getItemDamage(stack);

                if (newDamage > damage) {
                    damage = newDamage;
                    bestStack = stack;
                }
            }
        }

        return bestStack == itemStack || getItemDamage(itemStack) > damage;
    }

    public boolean isBestArmor(final EntityPlayerSP player, final ItemStack itemStack) {
        final ItemArmor itemArmor = (ItemArmor) itemStack.getItem();

        double reduction = 0.0;
        ItemStack bestStack = null;

        for (int i = INCLUDE_ARMOR_BEGIN; i < END; i++) {
            final ItemStack stack = player.inventoryContainer.getSlot(i).getStack();

            if (stack != null && stack.getItem() instanceof ItemArmor stackArmor) {
                if (stackArmor.armorType == itemArmor.armorType) {
                    final double newReduction = getDamageReduction(stack);

                    if (newReduction > reduction) {
                        reduction = newReduction;
                        bestStack = stack;
                    }
                }
            }
        }

        return bestStack == itemStack || getDamageReduction(itemStack) > reduction;
    }

    public int getToolType(final ItemStack stack) {
        final ItemTool tool = (ItemTool) stack.getItem();

        if (tool instanceof ItemPickaxe) return 0;
        else if (tool instanceof ItemAxe) return 1;
        else if (tool instanceof ItemSpade) return 2;
        else return -1;
    }

    public boolean isBestTool(final EntityPlayerSP player, final ItemStack itemStack) {
        final int type = getToolType(itemStack);

        Tool bestTool = new Tool(-1, -1, null);

        for (int i = EXCLUDE_ARMOR_BEGIN; i < END; i++) {
            final ItemStack stack = player.inventoryContainer.getSlot(i).getStack();

            if (stack != null && stack.getItem() instanceof ItemTool && type == getToolType(stack)) {
                final double efficiency = getToolEfficiency(stack);
                if (efficiency > bestTool.getEfficiency()) bestTool = new Tool(i, efficiency, stack);
            }
        }

        return bestTool.getStack() == itemStack || getToolEfficiency(itemStack) > bestTool.getEfficiency();
    }

    public boolean isBestBow(final EntityPlayerSP player, final ItemStack itemStack) {
        double bestBowDmg = -1.0;
        ItemStack bestBow = null;

        for (int i = EXCLUDE_ARMOR_BEGIN; i < END; i++) {
            final ItemStack stack = player.inventoryContainer.getSlot(i).getStack();
            if (stack != null && stack.getItem() instanceof ItemBow) {
                final double damage = getBowDamage(stack);

                if (damage > bestBowDmg) {
                    bestBow = stack;
                    bestBowDmg = damage;
                }
            }
        }

        return itemStack == bestBow || getBowDamage(itemStack) > bestBowDmg;
    }

    public double getDamageReduction(final ItemStack stack) {
        double reduction = 0.0;

        final ItemArmor armor = (ItemArmor) stack.getItem();

        reduction += armor.damageReduceAmount;

        if (stack.isItemEnchanted())
            reduction += EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack) * 0.25;

        return reduction;
    }

    public boolean isBuffPotion(final ItemStack stack) {
        final ItemPotion potion = (ItemPotion) stack.getItem();
        final List<PotionEffect> effects = potion.getEffects(stack);

        for (final PotionEffect effect : effects)
            if (Potion.potionTypes[effect.getPotionID()].isBadEffect()) return false;

        return true;
    }

    public double getBowDamage(ItemStack stack) {
        double damage = 0.0;

        if (stack.getItem() instanceof ItemBow && stack.isItemEnchanted())
            damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);

        return damage;
    }

    public boolean isGoodFood(final ItemStack stack) {
        final ItemFood food = (ItemFood) stack.getItem();

        if (food instanceof ItemAppleGold) return true;

        return food.getHealAmount(stack) >= 4 && food.getSaturationModifier(stack) >= 0.3F;
    }

    public float getToolEfficiency(final ItemStack itemStack) {
        final ItemTool tool = (ItemTool) itemStack.getItem();

        float efficiency = tool.getEfficiencyOnProperMaterial();

        final int lvl = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack);

        if (efficiency > 1.0F && lvl > 0) efficiency += lvl * lvl + 1;

        return efficiency;
    }

    public double getItemDamage(final ItemStack stack) {
        double damage = 0.0;

        final Multimap<String, AttributeModifier> attributeModifierMap = stack.getAttributeModifiers();

        for (final String attributeName : attributeModifierMap.keySet()) {
            if (attributeName.equals("generic.attackDamage")) {
                final Iterator<AttributeModifier> attributeModifiers = attributeModifierMap.get(attributeName).iterator();
                if (attributeModifiers.hasNext()) damage += attributeModifiers.next().getAmount();
                break;
            }
        }

        if (stack.isItemEnchanted()) {
            damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack);
            damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 1.25;
        }

        return damage;
    }

    /**
     * @param slotId             The inventory slot you are clicking.
     *                           Armor slots:
     *                           Helmet is 5 and chest plate is 8
     *                           First slot of inventory is 9 (top left)
     *                           Last slot of inventory is 44 (bottom right)
     * @param mouseButtonClicked Hot bar slot
     * @param mode               The type of click
     */
    public void windowClick(Minecraft mc, int windowId, int slotId, int mouseButtonClicked, ClickType mode) {
        mc.playerController.windowClick(windowId, slotId, mouseButtonClicked, mode.ordinal(), mc.thePlayer);
    }

    public void windowClick(Minecraft mc, int slotId, int mouseButtonClicked, ClickType mode) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slotId, mouseButtonClicked, mode.ordinal(), mc.thePlayer);
    }

    public boolean isStackValidToPlace(final ItemStack stack) {
        return stack.stackSize >= 1 && validateBlock(Block.getBlockFromItem(stack.getItem()), BlockAction.PLACE);
    }

    public enum BlockAction {
        PLACE, REPLACE, PLACE_ON
    }

    public boolean validateBlock(final Block block, final BlockAction action) {
        if (block instanceof BlockContainer) return false;
        final Material material = block.getMaterial();

        return switch (action) {
            case PLACE -> !(block instanceof BlockFalling) && block.isFullBlock() && block.isFullCube();
            case REPLACE -> material.isReplaceable();
            case PLACE_ON -> block.isFullBlock() && block.isFullCube();
        };

    }

    public enum ClickType {
        // if mouseButtonClicked is 0 `DROP_ITEM` will drop 1
        // item from the stack else if it is 1 it will drop the entire stack
        CLICK, SHIFT_CLICK, SWAP_WITH_HOT_BAR_SLOT, PLACEHOLDER, DROP_ITEM
    }

    @Getter @Setter @AllArgsConstructor
    private static class Tool {
        private final int slot;
        private final double efficiency;
        private final ItemStack stack;
    }
}