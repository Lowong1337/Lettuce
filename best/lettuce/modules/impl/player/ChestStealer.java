package best.lettuce.modules.impl.player;

import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.network.EventPacketReceive;
import best.lettuce.event.impl.player.EventMotion;
import best.lettuce.event.impl.player.EventWindowClick;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.property.impl.BooleanProperty;
import best.lettuce.modules.property.impl.NumberProperty;
import best.lettuce.utils.player.InventoryUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S2DPacketOpenWindow;

public class ChestStealer extends Module {

    //    private final BooleanProperty auraProperty = new BooleanProperty("Aura", true);
    private final BooleanProperty autoDelayProperty = new BooleanProperty("Auto Delay", true);
    private final NumberProperty delayProperty = new NumberProperty("Delay", 100, 0, 500, 50, () -> !this.autoDelayProperty.isEnabled());

    public final BooleanProperty silent = new BooleanProperty("Silent", true);

    private long lastClickTime;
    private int lastColumn, lastRow;

    private long nextDelay = 100L;

    public ChestStealer() {
        super("Chest Stealer", Category.PLAYER, "Automatically steals item from a container.");

        this.addProperties(this.autoDelayProperty, this.delayProperty, this.silent);
    }

    private final EventListener<EventPacketReceive> onReceivePacket = event -> {
        final Packet<?> packet = event.getPacket();
        // Open delay (wait before you steal)
        if (packet instanceof S2DPacketOpenWindow openWindow) { // When you open a window
            if (openWindow.getGuiId().equals("minecraft:container")) // Check it's a chest
                this.reset();
        }
    };

    private void reset() {
        // Reset last click & do open delay
        this.lastClickTime = System.currentTimeMillis();
        this.nextDelay = 100L;
        // Reset cursor pos
        this.lastColumn = 0;
        this.lastRow = 0;
    }

    private final EventListener<EventWindowClick> onWindowClick = event -> this.lastClickTime = System.currentTimeMillis();

    private final EventListener<EventMotion> onUpdate = event -> {
        if (event.isPre()) {
            final long timeSinceLastClick = System.currentTimeMillis() - this.lastClickTime;
            if (timeSinceLastClick < this.nextDelay) return;

            final GuiScreen current = this.mc.currentScreen;

            if (current instanceof GuiChest guiChest) {

                final IInventory lowerChestInventory = guiChest.getLowerChestInventory();

                final String chestName = lowerChestInventory.getDisplayName().getUnformattedText();

                if (!chestName.equals(I18n.format("container.chest")) && !chestName.equals(I18n.format("container.chestDouble")))
                    return;
                if (!InventoryUtils.hasFreeSlots(this.mc.thePlayer)) {
                    // Close delay
                    if (timeSinceLastClick > 100L) this.mc.thePlayer.closeScreen();
                    return;
                }

                final int nSlots = lowerChestInventory.getSizeInventory();
                for (int i = 0; i < nSlots; i++) {
                    final ItemStack stack = lowerChestInventory.getStackInSlot(i);

                    if (InventoryUtils.isValidStack(this.mc.thePlayer, stack)) {
                        final int column = i % 9;
                        final int row = i % (nSlots / 9);

                        final int columnDif = this.lastColumn - column;
                        final int rowDif = this.lastRow - row;

                        this.nextDelay = this.autoDelayProperty.isEnabled() ? (long) Math.ceil(50.0 * Math.max(1.0, Math.sqrt(columnDif * columnDif + rowDif * rowDif))) : this.delayProperty.getValue().longValue();

                        if (timeSinceLastClick < this.nextDelay) return;

                        InventoryUtils.windowClick(this.mc, this.mc.thePlayer.openContainer.windowId, i, 0, InventoryUtils.ClickType.SHIFT_CLICK);

                        this.lastColumn = column;
                        this.lastRow = row;
                        return;
                    }
                }

                // Close delay
                if (timeSinceLastClick > 100L) this.mc.thePlayer.closeScreen();
            }
        }
    };

    @Override
    public void onEnable() {
        this.reset();
    }
}