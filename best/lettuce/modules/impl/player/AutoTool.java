package best.lettuce.modules.impl.player;

import best.lettuce.Lecture;
import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.player.EventMotion;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.impl.combat.KillAura;
import best.lettuce.modules.property.impl.BooleanProperty;
import best.lettuce.utils.player.InventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

public class AutoTool extends Module {

    private KillAura aura;

    private boolean switched;
    private int previousSlot;

    private final BooleanProperty autoWeapon = new BooleanProperty("Auto Weapon", true);
    private final BooleanProperty switchBack = new BooleanProperty("Switch back", true);

    public AutoTool() {
        super("Auto Tool", Category.PLAYER, "Automatically selects the best tool in your hotbar to mine blocks/ attack.");

        this.addProperties(this.autoWeapon, this.switchBack);
    }

    private final EventListener<EventMotion> onUpdatePosition = event -> {
        if (event.isPre() && !mc.thePlayer.isUsingItem()) {
            if (this.switchBack.isEnabled() && this.switched && this.previousSlot != -1) {
                this.mc.thePlayer.inventory.currentItem = previousSlot;
                this.previousSlot = -1;
                this.switched = false;
            }

            if (this.autoWeapon.isEnabled() && KillAura.targetmob != null || (this.isPointedEntity() && this.mc.gameSettings.keyBindAttack.isKeyDown())) {
                double bestDamage = 1;
                int bestWeaponSlot = -1;

                for (int i = InventoryUtils.ONLY_HOT_BAR_BEGIN; i < InventoryUtils.END; i++) {
                    final ItemStack stack = this.mc.thePlayer.inventoryContainer.getSlot(i).getStack();

                    if (stack != null) {
                        final double damage = InventoryUtils.getItemDamage(stack);

                        if (damage > bestDamage) {
                            bestDamage = damage;
                            bestWeaponSlot = i;
                        }
                    }
                }

                if (bestWeaponSlot != -1) {
                    this.mc.thePlayer.inventory.currentItem = bestWeaponSlot - 36;
                    this.previousSlot = this.mc.thePlayer.inventory.currentItem;
                    this.switched = false;
                }
            } else if (this.isPointedBlock() && this.mc.gameSettings.keyBindAttack.isKeyDown()) {
                final BlockPos blockPos = this.mc.objectMouseOver.getBlockPos();

                final Block block = this.mc.theWorld.getBlockState(blockPos).getBlock();

                double bestToolEfficiency = 1;
                int bestToolSlot = -1;

                for (int i = InventoryUtils.ONLY_HOT_BAR_BEGIN; i < InventoryUtils.END; i++) {
                    final ItemStack stack = this.mc.thePlayer.inventoryContainer.getSlot(i).getStack();

                    if (stack != null && stack.getItem() instanceof ItemTool) {
                        ItemTool tool = (ItemTool) stack.getItem();
                        final double eff = tool.getStrVsBlock(stack, block);

                        if (eff > bestToolEfficiency) {
                            bestToolEfficiency = eff;
                            bestToolSlot = i;
                        }
                    }
                }

                if (bestToolSlot != -1) {
                    this.previousSlot = this.mc.thePlayer.inventory.currentItem;
                    this.mc.thePlayer.inventory.currentItem = bestToolSlot - 36;
                    this.switched = true;
                }
            }
        }
    };

    private boolean isPointedEntity() {
        return this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && this.mc.objectMouseOver.entityHit != null;
    }

    private boolean isPointedBlock() {
        return this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK;
    }

    @Override
    public void onEnable() {
        if (this.aura == null) {
            this.aura = Lecture.INSTANCE.getModuleManager().getModule(KillAura.class);
        }

        this.switched = false;
        this.previousSlot = -1;
    }

    @Override
    public void onDisable() {
        if (this.switched && this.previousSlot != -1) {
            this.mc.thePlayer.inventory.currentItem = previousSlot;
            this.previousSlot = -1;
            this.switched = false;
        }
    }
}