package best.lettuce.modules.impl.player;

import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.game.EventFlag;
import best.lettuce.event.impl.game.EventTick;
import best.lettuce.event.impl.game.EventWorld;
import best.lettuce.event.impl.player.*;
import best.lettuce.event.impl.render.EventRender2D;
import best.lettuce.gui.notification.Notification;
import best.lettuce.gui.notification.NotificationManager;
import best.lettuce.gui.notification.NotificationType;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.property.impl.BooleanProperty;
import best.lettuce.modules.property.impl.ModeProperty;
import best.lettuce.modules.property.impl.MultipleBoolProperty;
import best.lettuce.modules.property.impl.NumberProperty;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.animation.impl.DecelerateAnimation;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.player.InventoryUtils;
import best.lettuce.utils.player.MoveUtils;
import best.lettuce.utils.player.RotationUtils;
import best.lettuce.utils.render.RenderUtils;
import best.lettuce.utils.render.RoundedUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import org.apache.commons.lang3.RandomUtils;
import org.lwjgl.input.Keyboard;

public class Scaffold extends Module {
    private static final EnumFacing[] FACINGS = new EnumFacing[]{EnumFacing.EAST, EnumFacing.WEST, EnumFacing.SOUTH, EnumFacing.NORTH};
    // Tower
    private final ModeProperty towerMode = new ModeProperty("Tower Mode", "Watchdog", "Watchdog");
    private final BooleanProperty tower = new BooleanProperty("Tower", true);
    private final BooleanProperty towerMove = new BooleanProperty("Tower Move", true);
    // Draw
    private final MultipleBoolProperty drawOptions = new MultipleBoolProperty("Render Options",
            new BooleanProperty("Block Count", true),
            new BooleanProperty("ESP", true));
    // Placement
    private final BooleanProperty spoofHeldItem = new BooleanProperty("Spoof Held Item", true);
    private final BooleanProperty moveBlocks = new BooleanProperty("Move Blocks to hotbar", false);
    private final BooleanProperty biggestStack = new BooleanProperty("Biggest Stack", false);
    private final NumberProperty placeDelay = new NumberProperty("Place Delay", 0, 0, 10, 1);
    private final ModeProperty swing = new ModeProperty("Swing", "Silent", "Client", "Silent", "No Swing");
    private final BooleanProperty rayTraceCheck = new BooleanProperty("Ray Trace Check", false);
    private final BooleanProperty sneak = new BooleanProperty("Sneak", false);

    // Movement
    private final BooleanProperty safeWalk = new BooleanProperty("Safe Walk", false);
    private final BooleanProperty keepY = new BooleanProperty("Keep Y", true);
    public final BooleanProperty sprinting = new BooleanProperty("Sprint", true);

    //Auto disable
    public final MultipleBoolProperty autoDisable = new MultipleBoolProperty("Disable on",
            new BooleanProperty("Death", true),
            new BooleanProperty("Flag", true),
            new BooleanProperty("World Change", true));

    // For drawing only...
    private int totalBlockCount;
    // Counters
    public static final Animation anim = new DecelerateAnimation(100, 1);
    private int ticksSinceWindowClick;
    private int ticksSincePlace;
    // Block data
    private BlockData data;
    private BlockData lastPlacement;
    private float[] angles;
    // Tower
    private boolean towering;
    // Other...
    private int bestBlockStack;
    private double startPosY;
    private float randomSmoothingFactor;

    public Scaffold() {
        super("Scaffold", Category.PLAYER, "Automatically place blocks beneath you.");
        this.addProperties(this.spoofHeldItem, this.moveBlocks, this.biggestStack, this.placeDelay, this.swing, sneak,this.rayTraceCheck, this.towerMode, this.tower, this.towerMove,
                this.safeWalk, this.sprinting, this.keepY, this.drawOptions, this.autoDisable);
    }

    public final EventListener<EventTick> onTick = e -> {
        if (totalBlockCount == 0) {
            NotificationManager.post(NotificationType.DISABLE, "Scaffold Disabled", "Disabled Scaffold as you have no more blocks.");
            toggle(ToggleType.AUTO);
        }
    };

    public final EventListener<EventWorld> onWorld = e -> {
        if (autoDisable.getSetting("World Change").isEnabled()) {
            toggle(ToggleType.AUTO);
            NotificationManager.post(new Notification(NotificationType.WARNING, this.getName(), "Disabled " + this.getName().toLowerCase() + " due to world change"));
        }
    };

    public final EventListener<EventFlag> onFlag = e -> {
        if (autoDisable.getSetting("Flag").isEnabled()) {
            toggle(ToggleType.AUTO);
            NotificationManager.post(new Notification(NotificationType.WARNING, this.getName(), "Disabled " + this.getName().toLowerCase() + " to reduce flag"));
        }
    };

    public final EventListener<EventMotion> onUpdate = e -> {
        mc.thePlayer.setSprinting(sprinting.isEnabled() && MoveUtils.isMoving());
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).down()).getBlock() == Blocks.air && mc.thePlayer.onGround && sneak.isEnabled());
        if (mc.thePlayer.onGround && keepY.isEnabled() && MoveUtils.isMoving()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), mc.thePlayer.onGround);
        }
        if (mc.thePlayer != null) {
            if (mc.thePlayer.getHealth() <= 0.1 && autoDisable.getSetting("Death").isEnabled()) {
                toggle(ToggleType.AUTO);
                NotificationManager.post(new Notification(NotificationType.WARNING, this.getName(), "Disabled " + this.getName().toLowerCase() + " due to death"));
            }
        }
    };

    private final EventListener<EventCurrentItemHeld> onGetCurrentItem = event -> {
        if (this.spoofHeldItem.isEnabled() && this.bestBlockStack != -1 && this.bestBlockStack >= 36)
            event.setCurrentItem(this.bestBlockStack - InventoryUtils.ONLY_HOT_BAR_BEGIN);
    };

    private final EventListener<EventSafeWalk> onSafeWalkEvent = event -> {
        event.setCancelled(safeWalk.isEnabled());
        //mc.thePlayer.setSneaking(true);
    };

    private final EventListener<EventWindowClick> onWindowClick = event -> this.ticksSinceWindowClick = 0;

    private final EventListener<EventPlaceBlock> onBlockPlace = event -> this.ticksSincePlace = 0;

    private final EventListener<EventRender2D> onRenderGameOverlay = event -> {
        renderCounter();
    };

    public void renderCounter() {
        if (totalBlockCount == 0 || this.bestBlockStack == -1 || !this.drawOptions.isEnabled("Block Count")) return;
        ItemStack heldItem = mc.thePlayer.inventoryContainer.getSlot(this.bestBlockStack).getStack();
        if (heldItem != null) {
            ScaledResolution sr = new ScaledResolution();
            float x, y;
            float blockWH = 15;
            int spacing = 3;
            float output = anim.getOutput().floatValue();
            String text = totalBlockCount + " block" + (totalBlockCount != 1 ? "s" : "");
            float textWidth = lettuceFont18.getStringWidth(text);

            float totalWidth = ((textWidth + blockWH + spacing) + 6);
            x = (sr.getWidth() / 2f - (totalWidth / 2f));
            y = (sr.getHeight() - (sr.getHeight() / 2f - 20));
            float height = 20;

            RenderUtils.scissorStart(x - 1.5, y - 1.5, totalWidth + 3, height + 3);
            RoundedUtils.drawRound(x * output, y, totalWidth, height, 5, ColorUtils.tripleColor(20, .45f));
            lettuceFont18.drawString(text, x * output + 3 + blockWH + spacing, y + lettuceFont18.getMiddleOfBox(height) + .5f, -1);
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(heldItem, (int) x * output + 3, y + 10 - (blockWH / 2));
            RenderHelper.disableStandardItemLighting();
            RenderUtils.scissorEnd();
        }
    }

    private final EventListener<EventMotion> onUpdatePosition = event -> {
        if (event.isPre()) {
            // Increment tick counters
            this.ticksSinceWindowClick++;
            this.ticksSincePlace++;

            // Invalidate old data
            this.data = null;

            // Update towering state
            this.towering = this.tower.isEnabled() && this.mc.gameSettings.keyBindJump.isKeyDown();

            // Look for best block stack in hot bar
            this.bestBlockStack = this.getBestBlockStack(InventoryUtils.ONLY_HOT_BAR_BEGIN, InventoryUtils.END);

            this.calculateTotalBlockCount();
            this.moveBlocksIntoHotBar();

            // If best block stack is in hot bar
            if (this.bestBlockStack >= InventoryUtils.ONLY_HOT_BAR_BEGIN) {
                final BlockPos blockUnder = this.getBlockUnder();
                this.data = this.getBlockData(blockUnder);
                if (this.data == null) this.data = this.getBlockData(blockUnder.offset(EnumFacing.DOWN));

                this.randomSmoothingFactor += RandomUtils.nextFloat(0.f, 0.2f) - 0.1f;
                this.randomSmoothingFactor = Math.min(Math.max(0.f, this.randomSmoothingFactor), 1.f);

                if (this.data != null) {
                    // If ray trace fails hit vec will be null
                    if (this.validateReplaceable(this.data) && this.data.hitVec != null) {
                        // Calculate rotations to hit vec
                        this.angles = RotationUtils.getRotations(new float[]{event.getPrevYaw(), event.getPrevPitch()}, 12.f + this.randomSmoothingFactor * 2.f, RotationUtils.getHitOrigin(this.mc.thePlayer), this.data.hitVec);

                        if (this.towering) {
                            if (!this.towerMove.isEnabled()) {
                                this.mc.thePlayer.motionX = 0;
                                this.mc.thePlayer.motionZ = 0;

                                final double min = 9.0E-4D;

                                if (this.mc.thePlayer.ticksExisted % 2 == 0) event.setX(event.getX() + min);
                            }
                        }
                    } else {
                        this.data = null;
                    }
                }

                // If using no sprint & on ground
                if (!this.sprinting.isEnabled() && this.mc.thePlayer.onGround) {
                    // And has speed effect...
                    final PotionEffect speed = this.mc.thePlayer.getActivePotionEffect(Potion.moveSpeed);
                    final int moveSpeedAmp = speed == null ? 0 : speed.getAmplifier() + 1;
                    if (moveSpeedAmp > 0) {
                        final double multiplier = 1.0 + 0.2 * moveSpeedAmp + 0.1;
                        // Reduce motionX/Z based on speed amplifier
                        this.mc.thePlayer.motionX /= multiplier;
                        this.mc.thePlayer.motionZ /= multiplier;
                    }
                }

                // If has not set angles or has not yet placed a block
                if (this.angles == null || this.lastPlacement == null) {
                    // Get the last rotations (EntityPlayerSP#rotationYaw/rotationPitch)
                    final float[] lastAngles = this.angles != null ? this.angles : new float[]{event.getYaw(), event.getPitch()};
                    // Get the opposite direct that you are moving
                    final float moveDir = MoveUtils.getMovementDirection(this.mc.thePlayer.moveForward, this.mc.thePlayer.moveStrafing, this.mc.thePlayer.rotationYaw);
                    // Desired rotations
                    final float[] dstRotations = new float[]{moveDir + 180.f, 80f};
                    // Smooth to opposite
                    RotationUtils.applySmoothing(lastAngles, 12.f + this.randomSmoothingFactor * 2.f, dstRotations);
                    // Apply GCD fix (just for fun)
                    RotationUtils.applyGCD(dstRotations, lastAngles);
                    this.angles = dstRotations;
                }

                // Set rotations to persistent rotations
                event.setYaw(this.angles[0]);
                event.setPitch(this.angles[1]);
                RotationUtils.setVisualRotations(this.angles[0], this.angles[1], true);
            }
        } else {
            this.doPlace(event);
        }
    };

    private void doPlace(final EventMotion event) {
        if (this.bestBlockStack < 36 || this.data == null || this.ticksSincePlace <= this.placeDelay.getValue())
            return;

        final Vec3 hitVec;

        if (this.rayTraceCheck.isEnabled()) {
            // Perform ray trace with current angle stepped rotations
            final MovingObjectPosition rayTraceResult = RotationUtils.rayTraceBlocks(this.mc, event.isPre() ? event.getPrevYaw() : event.getYaw(), event.isPre() ? event.getPrevPitch() : event.getPitch());
            // If nothing is hit return
            if (rayTraceResult == null) return;
            // If did not hit block return
            if (rayTraceResult.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;
            // If side hit does not match block data return
            if (rayTraceResult.sideHit != this.data.face) return;
            // If block pos does not match block data return
            final BlockPos dstPos = this.data.pos;
            final BlockPos rayDstPos = rayTraceResult.getBlockPos();
            if (rayDstPos.getX() != dstPos.getX() || rayDstPos.getY() != dstPos.getY() || rayDstPos.getZ() != dstPos.getZ())
                return;

            hitVec = rayTraceResult.hitVec;
        } else {
            hitVec = this.data.hitVec;
        }

        final ItemStack heldItem;

        if (this.spoofHeldItem.isEnabled()) {
            heldItem = this.mc.thePlayer.inventoryContainer.getSlot(this.bestBlockStack).getStack();
        } else {
            // Switch item client side
            this.mc.thePlayer.inventory.currentItem = this.bestBlockStack - InventoryUtils.ONLY_HOT_BAR_BEGIN;
            heldItem = this.mc.thePlayer.getCurrentEquippedItem();
        }

        if (heldItem == null) return;

        // Attempt place using ray trace hit vec
        if (this.mc.playerController.onPlayerRightClick(this.mc.thePlayer, this.mc.theWorld, heldItem, this.data.pos, this.data.face, hitVec)) {
            this.lastPlacement = this.data;

            if (this.towering) this.mc.thePlayer.motionY = MoveUtils.getJumpHeight() - 0.000454352838557992;

            switch (this.swing.getMode()) {
                case "Client": this.mc.thePlayer.swingItem(); break;
                case "Silent": this.mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation()); break;
            }
        }
    }

    @Override
    public void onEnable() {
        anim.setDirection(Direction.FORWARDS);
        this.lastPlacement = null;
        this.towering = false;

        this.randomSmoothingFactor = 0.5f;

        if (this.mc.thePlayer != null) this.startPosY = this.mc.thePlayer.posY;
    }

    @Override
    public void onDisable() {
        anim.setDirection(Direction.BACKWARDS);
        this.angles = null;
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode()));
    }

    private BlockData getBlockData(final BlockPos pos) {
        final EnumFacing[] facings = FACINGS;

        // 1 of the 4 directions around player
        for (EnumFacing facing : facings) {
            final BlockPos blockPos = pos.add(facing.getOpposite().getDirectionVec());
            if (InventoryUtils.validateBlock(this.mc.theWorld.getBlockState(blockPos).getBlock(), InventoryUtils.BlockAction.PLACE_ON)) {
                final BlockData data = new BlockData(blockPos, facing);
                if (this.validateBlockRange(data)) return data;
            }
        }

        // 2 Blocks Under e.g. When jumping
        final BlockPos posBelow = pos.add(0, -1, 0);
        if (InventoryUtils.validateBlock(this.mc.theWorld.getBlockState(posBelow).getBlock(), InventoryUtils.BlockAction.PLACE_ON)) {
            final BlockData data = new BlockData(posBelow, EnumFacing.UP);
            if (this.validateBlockRange(data)) return data;
        }

        // 2 Block extension & diagonal
        for (EnumFacing facing : facings) {
            final BlockPos blockPos = pos.add(facing.getOpposite().getDirectionVec());
            for (EnumFacing facing1 : facings) {
                final BlockPos blockPos1 = blockPos.add(facing1.getOpposite().getDirectionVec());
                if (InventoryUtils.validateBlock(this.mc.theWorld.getBlockState(blockPos1).getBlock(), InventoryUtils.BlockAction.PLACE_ON)) {
                    final BlockData data = new BlockData(blockPos1, facing1);
                    if (this.validateBlockRange(data)) return data;
                }
            }
        }

        return null;

    }

    private boolean validateBlockRange(final BlockData data) {
        final Vec3 pos = data.hitVec;

        if (pos == null) return false;

        final EntityPlayerSP player = this.mc.thePlayer;

        final double x = (pos.xCoord - player.posX);
        final double y = (pos.yCoord - (player.posY + player.getEyeHeight()));
        final double z = (pos.zCoord - player.posZ);

        final float reach = this.mc.playerController.getBlockReachDistance();

        return Math.sqrt(x * x + y * y + z * z) <= reach;
    }

    private boolean validateReplaceable(final BlockData data) {
        final BlockPos pos = data.pos.offset(data.face);
        return this.mc.theWorld.getBlockState(pos).getBlock().isReplaceable(this.mc.theWorld, pos);
    }

    private BlockPos getBlockUnder() {
        if (this.keepY.isEnabled() && !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
            return new BlockPos(this.mc.thePlayer.posX, Math.min(this.startPosY, this.mc.thePlayer.posY) - 1, this.mc.thePlayer.posZ);
        } else {
            this.startPosY = this.mc.thePlayer.posY;

            return new BlockPos(this.mc.thePlayer.posX, this.mc.thePlayer.posY - 1, this.mc.thePlayer.posZ);
        }
    }

    private void moveBlocksIntoHotBar() {
        if (!moveBlocks.isEnabled()) return;
        // If no blocks in hot bar
        if (this.ticksSinceWindowClick > 4) {
            // Look for best block stack in inventory
            final int bestStackInInv = this.getBestBlockStack(InventoryUtils.EXCLUDE_ARMOR_BEGIN, InventoryUtils.ONLY_HOT_BAR_BEGIN);
            // If you have no blocks return
            if (bestStackInInv == -1) return;

            boolean foundEmptySlot = false;

            for (int i = InventoryUtils.END - 1; i >= InventoryUtils.ONLY_HOT_BAR_BEGIN; i--) {
                final ItemStack stack = this.mc.thePlayer.inventoryContainer.getSlot(i).getStack();

                if (stack == null) {
                    // Move blocks from inventory into free slot
                    InventoryUtils.windowClick(mc, bestStackInInv, i - InventoryUtils.ONLY_HOT_BAR_BEGIN, InventoryUtils.ClickType.SWAP_WITH_HOT_BAR_SLOT);

                    foundEmptySlot = true;
                }
            }

            if (!foundEmptySlot) {
                final int overrideSlot = 8;
                // Swap with item in last slot of hot bar
                InventoryUtils.windowClick(mc, bestStackInInv, overrideSlot, InventoryUtils.ClickType.SWAP_WITH_HOT_BAR_SLOT);
            }
        }
    }

    private int getBestBlockStack(final int start, final int end) {
        int bestSlot = -1;
        int bestSlotStackSize = 0;

        for (int i = start; i < end; i++) {
            final ItemStack stack = this.mc.thePlayer.inventoryContainer.getSlot(i).getStack();

            if (stack != null && stack.getItem() instanceof ItemBlock && InventoryUtils.isStackValidToPlace(stack)) {
                if (!biggestStack.isEnabled() || stack.stackSize > bestSlotStackSize) {
                    bestSlot = i;
                    bestSlotStackSize = stack.stackSize;
                }
            }
        }

        return bestSlot;
    }

    private void calculateTotalBlockCount() {
        this.totalBlockCount = 0;

        for (int i = (this.moveBlocks.isEnabled() ? InventoryUtils.EXCLUDE_ARMOR_BEGIN : InventoryUtils.ONLY_HOT_BAR_BEGIN); i < InventoryUtils.END; i++) {
            final ItemStack stack = this.mc.thePlayer.inventoryContainer.getSlot(i).getStack();

            if (stack != null && stack.stackSize >= 1 && stack.getItem() instanceof ItemBlock && InventoryUtils.isStackValidToPlace(stack)) {

                this.totalBlockCount += stack.stackSize;
            }
        }
    }

    private static class BlockData {

        private final BlockPos pos;
        private final EnumFacing face;
        private final Vec3 hitVec;

        public BlockData(BlockPos pos, EnumFacing face) {
            this.pos = pos;
            this.face = face;
            this.hitVec = this.calculateBlockData();
        }

        private Vec3 calculateBlockData() {
            final Vec3i directionVec = this.face.getDirectionVec();
            final Minecraft mc = Minecraft.getMinecraft();

            double x;
            double z;

            switch (this.face.getAxis()) {
                case Z: {
                    final double absX = Math.abs(mc.thePlayer.posX);
                    double xOffset = absX - (int) absX;
                    if (mc.thePlayer.posX < 0) {
                        xOffset = 1.0F - xOffset;
                    }
                    x = directionVec.getX() * xOffset;
                    z = directionVec.getZ() * xOffset;
                    break;
                }
                case X: {
                    final double absZ = Math.abs(mc.thePlayer.posZ);
                    double zOffset = absZ - (int) absZ;
                    if (mc.thePlayer.posZ < 0) {
                        zOffset = 1.0F - zOffset;
                    }
                    x = directionVec.getX() * zOffset;
                    z = directionVec.getZ() * zOffset;
                    break;
                }
                default: {
                    x = 0.25;
                    z = 0.25;
                    break;
                }
            }

            if (this.face.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
                x = -x;
                z = -z;
            }

            final Vec3 hitVec = new Vec3(this.pos).addVector(x + z, directionVec.getY() * 0.5, x + z);

            final Vec3 src = mc.thePlayer.getPositionEyes(1.0F);
            final MovingObjectPosition obj = mc.theWorld.rayTraceBlocks(src, hitVec, false, false, true);

            if (obj == null || obj.hitVec == null || obj.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
                return null;

            switch (this.face.getAxis()) {
                case Z: obj.hitVec = new Vec3(obj.hitVec.xCoord, obj.hitVec.yCoord, Math.round(obj.hitVec.zCoord)); break;
                case X: obj.hitVec = new Vec3(Math.round(obj.hitVec.xCoord), obj.hitVec.yCoord, obj.hitVec.zCoord); break;
            }

            if (this.face != EnumFacing.DOWN && this.face != EnumFacing.UP) {
                final IBlockState blockState = mc.theWorld.getBlockState(obj.getBlockPos());
                final Block blockAtPos = blockState.getBlock();

                double blockFaceOffset;

                blockFaceOffset = RandomUtils.nextDouble(0.1, 0.3);

                if (blockAtPos instanceof BlockSlab && !((BlockSlab) blockAtPos).isDouble()) {
                    final BlockSlab.EnumBlockHalf half = blockState.getValue(BlockSlab.HALF);

                    if (half != BlockSlab.EnumBlockHalf.TOP) {
                        blockFaceOffset += 0.5;
                    }
                }

                obj.hitVec = obj.hitVec.addVector(0.0D, -blockFaceOffset, 0.0D);
            }

            return obj.hitVec;
        }
    }
}