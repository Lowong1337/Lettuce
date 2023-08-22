package best.lettuce.modules.impl.player;

import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.player.EventMotion;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.property.impl.BooleanProperty;
import best.lettuce.modules.property.impl.NumberProperty;
import best.lettuce.utils.player.InventoryUtils;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;

public class AutoEat extends Module {
    public AutoEat() {
        super("Auto Eat", Category.PLAYER, "Automatically eat when you are hungry or on low health.");
        addProperties(silent, autogap, health, food, hungeramound);
    }
    public final BooleanProperty silent = new BooleanProperty("Silent", true);
    public final BooleanProperty autogap = new BooleanProperty("Golden Apple", true);
    public final NumberProperty health = new NumberProperty("Health Level", 5, 0, 20, 0.5, autogap::isEnabled);
    public final BooleanProperty food = new BooleanProperty("Food", true);
    public final NumberProperty hungeramound = new NumberProperty("Food Level", 5, 0, 20, 1, food::isEnabled);

    public int slot;

    public final EventListener<EventMotion> onUpdate = e -> {
        if(mc.thePlayer.getHeldItem() != null) {
            if(!(mc.thePlayer.getHeldItem().getItem() instanceof ItemAppleGold) || !(mc.thePlayer.getHeldItem().getItem() instanceof ItemFood)){
                slot = mc.thePlayer.inventory.currentItem;
                //Lettuce.text(String.valueOf(slot));
            }
        }
        if (mc.thePlayer.getHealth() >= health.getValue().floatValue()) {
            //slot = mc.thePlayer.inventory.currentItem;
            mc.thePlayer.inventory.currentItem=slot;
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
        }
        int gapslot = getgappslot();
        int foodslot = getfoodslot();
        int healthnigga;
        if(mc.thePlayer.getHealth() <= health.getValue().floatValue() && autogap.isEnabled()){
            for (ItemStack stack : mc.thePlayer.inventory.mainInventory){
                if(stack != null){
                    if(!silent.isEnabled()){
                        mc.thePlayer.inventory.currentItem = gapslot - 36;
                        if(mc.thePlayer.getHeldItem() != null) {
                             healthnigga = mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem).stackSize;
                            //mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
                            mc.gameSettings.keyBindUseItem.setPressed(true);
                            //KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);
                        }
                    }
                }
            }
        }

        if(mc.thePlayer.getFoodStats().getFoodLevel() <= hungeramound.getValue().intValue()){
            if (mc.thePlayer.getHealth() >= health.getValue().floatValue()) {
                slot = mc.thePlayer.inventory.currentItem;
                mc.thePlayer.inventory.currentItem = slot;
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
            }
            for (ItemStack stack : mc.thePlayer.inventory.mainInventory) {
                if (stack.getItem() != null) {
                    if(!silent.isEnabled()){
                        mc.thePlayer.inventory.currentItem = foodslot - 36;
                        if(mc.thePlayer.getHeldItem() != null) {
                            mc.rightClickMouse();
                            if (mc.thePlayer.getFoodStats().getFoodLevel() >= hungeramound.getValue().intValue()) {
                                mc.thePlayer.inventory.currentItem = slot;
                            }
                        }
                    }
                }
            }
        }
    };

    public int getgappslot(){
        for (int i = InventoryUtils.ONLY_HOT_BAR_BEGIN; i < InventoryUtils.END; i++) {
            final ItemStack stack = this.mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if(stack != null && stack.getItem() instanceof ItemAppleGold){
                return i;
            }
        }
        return mc.thePlayer.inventory.currentItem;
    }

    public int getfoodslot(){
        for (int i = InventoryUtils.ONLY_HOT_BAR_BEGIN; i < InventoryUtils.END; i++) {
            final ItemStack stack = this.mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if(stack != null && stack.getItem() instanceof ItemFood){
                return i;
            }
        }
        return mc.thePlayer.inventory.currentItem;
    }
}
