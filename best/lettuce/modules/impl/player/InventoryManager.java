package best.lettuce.modules.impl.player;

import best.lettuce.Lecture;
import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.network.EventPacketReceive;
import best.lettuce.event.impl.network.EventPacketSend;
import best.lettuce.event.impl.player.EventMotion;
import best.lettuce.event.impl.player.EventWindowClick;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.impl.combat.KillAura;
import best.lettuce.modules.property.impl.*;
import best.lettuce.utils.player.InventoryUtils;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.server.S2DPacketOpenWindow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class InventoryManager extends Module {

    private final MultipleBoolProperty tasks = new MultipleBoolProperty("Tasks",
            new BooleanProperty("Cleaner", true),
            new BooleanProperty("Auto Armor", true));
    private final ModeProperty mode = new ModeProperty("Mode", "Open Inventory", "Open Inventory", "Spoof");
    private final NumberProperty delayProperty = new NumberProperty("Delay", 150, 0, 500, 50);
    private final BooleanProperty ignoreItemsWithCustomName = new BooleanProperty("Ignore Custom Name", true);

    private final BooleanProperty sortHotbar = new BooleanProperty("Sort Hotbar", true);
    private final NumberProperty swordSlot = new NumberProperty("Sword Slot", 1, 1, 9, 1, sortHotbar::isEnabled);
    private final NumberProperty gappleSlot = new NumberProperty("Gapple Slot", 2, 1, 9, 1, sortHotbar::isEnabled);
    private final NumberProperty pearlSlot = new NumberProperty("Pearl Slot", 3, 1, 9, 1, sortHotbar::isEnabled);
    private final NumberProperty bowSlot = new NumberProperty("Bow Slot", 4, 1, 9, 1, sortHotbar::isEnabled);
    private final NumberProperty shovelSlot = new NumberProperty("Shovel Slot", 7, 1, 9, 1, sortHotbar::isEnabled);
    private final NumberProperty pickaxeSlot = new NumberProperty("Pickaxe Slot", 8, 1, 9, 1, sortHotbar::isEnabled);
    private final NumberProperty axeSlot = new NumberProperty("Axe Slot", 9, 1, 9, 1, sortHotbar::isEnabled);

    private final int[] bestArmorPieces = new int[4], bestToolSlots = new int[3];
    private final List<Integer> trash = new ArrayList<>();
    private final List<Integer> gappleStackSlots = new ArrayList<>(), pearlStackSlots = new ArrayList<>();
    private int bestSwordSlot;
    private int bestBowSlot;

    private boolean serverOpen;
    private boolean clientOpen;

    private int ticksSinceLastClick;

    private boolean nextTickCloseInventory;

    private KillAura aura;

    public InventoryManager() {
        super("Inventory Manager", Category.PLAYER, "Cleans your inventory.");

        this.addProperties(this.tasks, this.mode, this.delayProperty, this.ignoreItemsWithCustomName,
                this.sortHotbar, this.swordSlot, this.gappleSlot, this.pearlSlot, this.bowSlot, this.shovelSlot, this.pickaxeSlot, this.axeSlot);
    }

    private final EventListener<EventPacketSend> onSendPacket = event -> {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof C16PacketClientStatus) {

            if (((C16PacketClientStatus) packet).getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT) {
                this.clientOpen = true;
                this.serverOpen = true;
            }
        } else if (packet instanceof C0DPacketCloseWindow) {

            if (packet.getID() == this.mc.thePlayer.inventoryContainer.windowId) {
                this.clientOpen = false;
                this.serverOpen = false;
            }
        }
    };

    private final EventListener<EventWindowClick> onWindowClick = event -> this.ticksSinceLastClick = 0;

    private boolean dropItem(final List<Integer> listOfSlots) {
        if (this.tasks.isEnabled("Cleaner")) {
            if (!listOfSlots.isEmpty()) {
                int slot = listOfSlots.remove(0);
                InventoryUtils.windowClick(this.mc, slot, 1, InventoryUtils.ClickType.DROP_ITEM);
                return true;
            }
        }
        return false;
    }

    private final EventListener<EventPacketReceive> onReceivePacket = event -> {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof S2DPacketOpenWindow) {
            this.clientOpen = false;
            this.serverOpen = false;
        }
    };

    private final EventListener<EventMotion> onUpdate = event -> {
        if (event.isPre()) {
            this.ticksSinceLastClick++;

            if (this.ticksSinceLastClick < Math.floor(this.delayProperty.getValue() / 50)) return;

            // TODO :: PLEASE
//            if (this.aura.getTarget() != null || this.aura.canAutoBlock()) {
//                if (this.nextTickCloseInventory) {
//                    this.nextTickCloseInventory = false;
//                }
//
//                this.close();
//                return;
//            }

            if ((this.mc.currentScreen == null && this.mode.is("Spoof")) || (this.mc.currentScreen instanceof GuiInventory && this.mode.is("Open Inventory"))) {
                this.clear();

                for (int slot = InventoryUtils.INCLUDE_ARMOR_BEGIN; slot < InventoryUtils.END; slot++) {
                    final ItemStack stack = this.mc.thePlayer.inventoryContainer.getSlot(slot).getStack();

                    if (stack != null) {
                        if (this.ignoreItemsWithCustomName.isEnabled() && stack.hasDisplayName()) continue;

                        if (stack.getItem() instanceof ItemSword && InventoryUtils.isBestSword(this.mc.thePlayer, stack)) {
                            this.bestSwordSlot = slot;
                        } else if (stack.getItem() instanceof ItemTool && InventoryUtils.isBestTool(this.mc.thePlayer, stack)) {
                            final int toolType = InventoryUtils.getToolType(stack);
                            if (toolType != -1 && slot != this.bestToolSlots[toolType])
                                this.bestToolSlots[toolType] = slot;
                        } else if (stack.getItem() instanceof ItemArmor && InventoryUtils.isBestArmor(this.mc.thePlayer, stack)) {
                            final int pieceSlot = this.bestArmorPieces[((ItemArmor) stack.getItem()).armorType];
                            if (pieceSlot == -1 || slot != pieceSlot) this.bestArmorPieces[((ItemArmor) stack.getItem()).armorType] = slot;
                        } else if (stack.getItem() instanceof ItemBow && InventoryUtils.isBestBow(this.mc.thePlayer, stack)) {
                            if (slot != this.bestBowSlot) this.bestBowSlot = slot;
                        } else if (stack.getItem() instanceof ItemAppleGold) {
                            this.gappleStackSlots.add(slot);
                        } else if (stack.getItem() instanceof ItemEnderPearl) {
                            this.pearlStackSlots.add(slot);
                        } else if (!this.trash.contains(slot) && !isValidStack(stack)) {
                            this.trash.add(slot);
                        }
                    }
                }

                final boolean busy = (!this.trash.isEmpty() && this.tasks.isEnabled("Cleaner")) || this.equipArmor(false) || this.sortItems(false);

                if (!busy) {
                    if (this.nextTickCloseInventory) {
                        this.close();
                        this.nextTickCloseInventory = false;
                    } else {
                        this.nextTickCloseInventory = true;
                    }
                    return;
                } else {
                    boolean waitUntilNextTick = !this.serverOpen;

                    this.open();

                    if (this.nextTickCloseInventory) this.nextTickCloseInventory = false;

                    if (waitUntilNextTick) return;
                }

                if (this.equipArmor(true)) return;
                if (this.dropItem(this.trash)) return;
                this.sortItems(true);
            }
        }
    };

    private boolean sortItems(final boolean moveItems) {
        if (this.sortHotbar.isEnabled()) {
            int goodSwordSlot = this.swordSlot.getValue().intValue() + 35;
            if (this.bestSwordSlot != -1) {
                if (this.bestSwordSlot != goodSwordSlot) {
                    if (moveItems) {
                        this.putItemInSlot(goodSwordSlot, this.bestSwordSlot);
                        this.bestSwordSlot = goodSwordSlot;
                    }
                    return true;
                }
            }

            int goodBowSlot = this.bowSlot.getValue().intValue() + 35;
            if (this.bestBowSlot != -1) {
                if (this.bestBowSlot != goodBowSlot) {
                    if (moveItems) {
                        this.putItemInSlot(goodBowSlot, this.bestBowSlot);
                        this.bestBowSlot = goodBowSlot;
                    }
                    return true;
                }
            }

            int goodGappleSlot = this.gappleSlot.getValue().intValue() + 35;
            if (!this.gappleStackSlots.isEmpty()) {
                this.gappleStackSlots.sort(Comparator.comparingInt(slot -> this.mc.thePlayer.inventoryContainer.getSlot(slot).getStack().stackSize));
                final int bestGappleSlot = this.gappleStackSlots.get(0);
                if (bestGappleSlot != goodGappleSlot) {
                    if (moveItems) {
                        this.putItemInSlot(goodGappleSlot, bestGappleSlot);
                        this.gappleStackSlots.set(0, goodGappleSlot);
                    }
                    return true;
                }
            }

            int goodPearlSlot = this.pearlSlot.getValue().intValue() + 35;
            if (!this.pearlStackSlots.isEmpty()) {
                this.pearlStackSlots.sort(Comparator.comparingInt(slot -> this.mc.thePlayer.inventoryContainer.getSlot(slot).getStack().stackSize));
                final int bestPearlSlot = this.pearlStackSlots.get(0);
                if (bestPearlSlot != goodPearlSlot) {
                    if (moveItems) {
                        this.putItemInSlot(goodPearlSlot, bestPearlSlot);
                        this.pearlStackSlots.set(0, goodPearlSlot);
                    }
                    return true;
                }
            }

            final int[] toolSlots = {
                    pickaxeSlot.getValue().intValue() + 35,
                    axeSlot.getValue().intValue() + 35,
                    shovelSlot.getValue().intValue() + 35};

            for (final int toolSlot : this.bestToolSlots) {
                if (toolSlot != -1) {
                    final int type = InventoryUtils.getToolType(this.mc.thePlayer.inventoryContainer.getSlot(toolSlot).getStack());

                    if (type != -1) {
                        if (toolSlot != toolSlots[type]) {
                            if (moveItems) {
                                this.putToolsInSlot(type, toolSlots);
                            }
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean equipArmor(boolean moveItems) {
        if (this.tasks.isEnabled("Auto Armor")) {
            for (int i = 0; i < this.bestArmorPieces.length; i++) {
                final int piece = this.bestArmorPieces[i];

                if (piece != -1) {
                    int armorPieceSlot = i + 5;
                    final ItemStack stack = this.mc.thePlayer.inventoryContainer.getSlot(armorPieceSlot).getStack();
                    if (stack != null) continue;

                    if (moveItems) InventoryUtils.windowClick(this.mc, piece, 0, InventoryUtils.ClickType.SHIFT_CLICK);

                    return true;
                }
            }
        }

        return false;
    }

    private void putItemInSlot(final int slot, final int slotIn) {
        InventoryUtils.windowClick(this.mc, slotIn, slot - 36, InventoryUtils.ClickType.SWAP_WITH_HOT_BAR_SLOT);
    }

    private void putToolsInSlot(final int tool, final int[] toolSlots) {
        final int toolSlot = toolSlots[tool];

        InventoryUtils.windowClick(this.mc, this.bestToolSlots[tool], toolSlot - 36, InventoryUtils.ClickType.SWAP_WITH_HOT_BAR_SLOT);
        this.bestToolSlots[tool] = toolSlot;
    }

    private static boolean isValidStack(final ItemStack stack) {
        if (stack.getItem() instanceof ItemBlock && InventoryUtils.isStackValidToPlace(stack)) {
            return true;
        } else if (stack.getItem() instanceof ItemPotion && InventoryUtils.isBuffPotion(stack)) {
            return true;
        } else if (stack.getItem() instanceof ItemFood && InventoryUtils.isGoodFood(stack)) {
            return true;
        } else {
            return InventoryUtils.isGoodItem(stack.getItem());
        }
    }

    @Override
    public void onEnable() {
        if (this.aura == null) {
            this.aura = Lecture.INSTANCE.getModuleManager().getModule(KillAura.class);
        }

        this.ticksSinceLastClick = 0;

        this.clientOpen = this.mc.currentScreen instanceof GuiInventory;
        this.serverOpen = this.clientOpen;
    }

    @Override
    public void onDisable() {
        this.close();
        this.clear();
    }

    private void open() {
        if (!this.clientOpen && !this.serverOpen) {
            this.mc.thePlayer.sendQueue.addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
            this.serverOpen = true;
        }
    }

    private void close() {
        if (!this.clientOpen && this.serverOpen) {
            this.mc.thePlayer.sendQueue.addToSendQueue(new C0DPacketCloseWindow(this.mc.thePlayer.inventoryContainer.windowId));
            this.serverOpen = false;
        }
    }

    private void clear() {
        this.trash.clear();
        this.bestBowSlot = -1;
        this.bestSwordSlot = -1;
        this.gappleStackSlots.clear();
        this.pearlStackSlots.clear();
        Arrays.fill(this.bestArmorPieces, -1);
        Arrays.fill(this.bestToolSlots, -1);
    }
}