package net.minecraft.client.entity;

import best.lettuce.Lecture;
import best.lettuce.commands.CommandManager;
import best.lettuce.event.Event;
import best.lettuce.event.impl.player.*;
import best.lettuce.utils.math.TimerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSoundMinecartRiding;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiCommandBlock;
import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.client.gui.GuiHopper;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.inventory.GuiBeacon;
import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiDispenser;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.gui.inventory.GuiScreenHorseInventory;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C0CPacketInput;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C13PacketPlayerAbilities;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.potion.Potion;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.*;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;

public class EntityPlayerSP extends AbstractClientPlayer {
    public final NetHandlerPlayClient sendQueue;
    private final StatFileWriter statWriter;
    private double lastReportedPosX;
    private double lastReportedPosY;
    private double lastReportedPosZ;
    public float lastReportedYaw;
    public float lastReportedPitch;
    public boolean serverSneakState;
    public boolean serverSprintState;
    private int positionUpdateTicks;
    private boolean hasValidHealth;
    private String clientBrand;
    public MovementInput movementInput;
    protected Minecraft mc;
    public int sprintToggleTimer;
    public int sprintingTicksLeft;
    public float renderArmYaw;
    public float renderArmPitch;
    public float prevRenderArmYaw;
    public float prevRenderArmPitch;
    private int horseJumpPowerCounter;
    private float horseJumpPower;
    public float timeInPortal;
    public float prevTimeInPortal;

    public EntityPlayerSP(Minecraft mcIn, World worldIn, NetHandlerPlayClient netHandler, StatFileWriter statFile) {
        super(worldIn, netHandler.getGameProfile());
        this.sendQueue = netHandler;
        this.statWriter = statFile;
        this.mc = mcIn;
        this.dimension = 0;
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        return false;
    }

    public void heal(float healAmount) {
    }

    public void mountEntity(Entity entityIn) {
        super.mountEntity(entityIn);

        if (entityIn instanceof EntityMinecart) {
            this.mc.getSoundHandler().playSound(new MovingSoundMinecartRiding(this, (EntityMinecart) entityIn));
        }
    }

    public void onUpdate() {
        if (this.worldObj.isBlockLoaded(new BlockPos(this.posX, 0.0D, this.posZ))) {
            Lecture.INSTANCE.getEventManager().dispatch(new EventUpdate());

            super.onUpdate();

            if (this.isRiding()) {
                this.sendQueue.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(this.rotationYaw, this.rotationPitch, this.onGround));
                this.sendQueue.addToSendQueue(new C0CPacketInput(this.moveStrafing, this.moveForward, this.movementInput.jump, this.movementInput.sneak));
            } else {
                this.onUpdateWalkingPlayer();
            }
        }
    }

    public void onUpdateWalkingPlayer() {
        EventMotion updatePositionEvent = new EventMotion(this.posX, (getEntityBoundingBox()).minY, this.posZ, this.rotationYaw, this.rotationPitch, this.onGround, this.lastReportedYaw, this.lastReportedPitch);
        updatePositionEvent.setType(Event.Type.PRE);
        Lecture.INSTANCE.getEventManager().dispatch(updatePositionEvent);

        boolean flag = this.isSprinting();

        if (flag != this.serverSprintState) {
            if (flag) {
                this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.START_SPRINTING));
            } else {
                this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.STOP_SPRINTING));
            }

            this.serverSprintState = flag;
        }

        boolean flag1 = this.isSneaking();

        if (flag1 != this.serverSneakState) {
            if (flag1) {
                this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.START_SNEAKING));
            } else {
                this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.STOP_SNEAKING));
            }

            this.serverSneakState = flag1;
        }

        if (isCurrentViewEntity()) {
            double eventX = updatePositionEvent.getX();
            double eventY = updatePositionEvent.getY();
            double eventZ = updatePositionEvent.getZ();
            float eventYaw = updatePositionEvent.getYaw();
            float eventPitch = updatePositionEvent.getPitch();
            boolean eventOnGround = updatePositionEvent.isOnGround();
            double xDif = eventX - this.lastReportedPosX;
            double yDif = eventY - this.lastReportedPosY;
            double zDif = eventZ - this.lastReportedPosZ;
            float yawDif = eventYaw - this.lastReportedYaw;
            float pitchDif = eventPitch - this.lastReportedPitch;
            boolean updateXYZ = (xDif * xDif + yDif * yDif + zDif * zDif > 9.0E-4D || this.positionUpdateTicks >= 20);
            boolean updateYawPitch = (yawDif != 0.0D || pitchDif != 0.0D);
            boolean cancelled = updatePositionEvent.isCancelled();
            if (this.isRiding()) {
                if (!cancelled) {
                    this.sendQueue.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(eventYaw, eventPitch, eventOnGround));
                    this.sendQueue.addToSendQueue(new C0CPacketInput(this.moveStrafing, this.moveForward, this.movementInput.jump, this.movementInput.sneak));
                }
            } else {
                if (!cancelled) if (updateXYZ && updateYawPitch) {
                    this.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(eventX, eventY, eventZ, eventYaw, eventPitch, eventOnGround));
                } else if (updateXYZ) {
                    this.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(eventX, eventY, eventZ, eventOnGround));
                } else if (updateYawPitch) {
                    this.sendQueue.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(eventYaw, eventPitch, eventOnGround));
                } else {
                    this.sendQueue.addToSendQueue(new C03PacketPlayer(eventOnGround));
                }
                this.positionUpdateTicks++;
                if (updateXYZ) {
                    this.lastReportedPosX = eventX;
                    this.lastReportedPosY = eventY;
                    this.lastReportedPosZ = eventZ;
                    this.positionUpdateTicks = 0;
                }
                if (updateYawPitch) {
                    this.lastReportedYaw = eventYaw;
                    this.lastReportedPitch = eventPitch;
                }
                updatePositionEvent.setType(Event.Type.POST);
                Lecture.INSTANCE.getEventManager().dispatch(updatePositionEvent);
            }
        }
    }

    public void dropOneItem(boolean dropAll) {
        C07PacketPlayerDigging.Action c07packetplayerdigging$action = dropAll ? C07PacketPlayerDigging.Action.DROP_ALL_ITEMS : C07PacketPlayerDigging.Action.DROP_ITEM;
        this.sendQueue.addToSendQueue(new C07PacketPlayerDigging(c07packetplayerdigging$action, BlockPos.ORIGIN, EnumFacing.DOWN));
    }

    protected void joinEntityItemWithWorld(EntityItem itemIn) {}

    public void sendChatMessage(String message) {
        if (Lecture.INSTANCE.getCommandManager().execute(message) || message.startsWith(CommandManager.CHAT_PREFIX)) {
            return;
        }

        this.sendQueue.addToSendQueue(new C01PacketChatMessage(message));
    }

    TimerUtils tenacityTimer = new TimerUtils();
    public void swingItem() {
        super.swingItem();
        this.sendQueue.addToSendQueue(new C0APacketAnimation());
        if (tenacityTimer.hasTimeElapsed(500)) {
            Lecture.text("[tsukasa.tokyo]");
            tenacityTimer.reset();
        }
    }

    public void respawnPlayer() {
        this.sendQueue.addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.PERFORM_RESPAWN));
    }

    protected void damageEntity(DamageSource damageSrc, float damageAmount) {
        if (!this.isEntityInvulnerable(damageSrc)) {
            this.setHealth(this.getHealth() - damageAmount);
        }
    }

    public void closeScreen() {
        this.sendQueue.addToSendQueue(new C0DPacketCloseWindow(this.openContainer.windowId));
        this.closeScreenAndDropStack();
    }

    public void closeScreenAndDropStack() {
        this.inventory.setItemStack(null);
        super.closeScreen();
        this.mc.displayGuiScreen(null);
    }

    public void setPlayerSPHealth(float health) {
        if (this.hasValidHealth) {
            float f = this.getHealth() - health;

            if (f <= 0.0F) {
                this.setHealth(health);

                if (f < 0.0F) {
                    this.hurtResistantTime = this.maxHurtResistantTime / 2;
                }
            } else {
                this.lastDamage = f;
                this.setHealth(this.getHealth());
                this.hurtResistantTime = this.maxHurtResistantTime;
                this.damageEntity(DamageSource.generic, f);
                this.hurtTime = this.maxHurtTime = 10;
            }
        } else {
            this.setHealth(health);
            this.hasValidHealth = true;
        }
    }

    public void addStat(StatBase stat, int amount) {
        if (stat != null) {
            if (stat.isIndependent) {
                super.addStat(stat, amount);
            }
        }
    }

    public void sendPlayerAbilities() {
        this.sendQueue.addToSendQueue(new C13PacketPlayerAbilities(this.capabilities));
    }

    public boolean isUser() {
        return true;
    }

    protected void sendHorseJump() {
        this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.RIDING_JUMP, (int) (this.getHorseJumpPower() * 100.0F)));
    }

    public void sendHorseInventory() {
        this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.OPEN_INVENTORY));
    }

    public void setClientBrand(String brand) {
        this.clientBrand = brand;
    }

    public String getClientBrand() {
        return this.clientBrand;
    }

    public StatFileWriter getStatFileWriter() {
        return this.statWriter;
    }

    public void addChatComponentMessage(IChatComponent chatComponent) {
        this.mc.ingameGUI.getChatGUI().printChatMessage(chatComponent);
    }

    protected boolean pushOutOfBlocks(double x, double y, double z) {
        if (this.noClip) {
            return false;
        } else {
            BlockPos blockpos = new BlockPos(x, y, z);
            double d0 = x - (double) blockpos.getX();
            double d1 = z - (double) blockpos.getZ();

            if (!this.isOpenBlockSpace(blockpos)) {
                int i = -1;
                double d2 = 9999.0D;

                if (this.isOpenBlockSpace(blockpos.west()) && d0 < d2) {
                    d2 = d0;
                    i = 0;
                }

                if (this.isOpenBlockSpace(blockpos.east()) && 1.0D - d0 < d2) {
                    d2 = 1.0D - d0;
                    i = 1;
                }

                if (this.isOpenBlockSpace(blockpos.north()) && d1 < d2) {
                    d2 = d1;
                    i = 4;
                }

                if (this.isOpenBlockSpace(blockpos.south()) && 1.0D - d1 < d2) {
                    d2 = 1.0D - d1;
                    i = 5;
                }

                float f = 0.1F;

                if (i == 0) {
                    this.motionX = -f;
                }

                if (i == 1) {
                    this.motionX = f;
                }

                if (i == 4) {
                    this.motionZ = -f;
                }

                if (i == 5) {
                    this.motionZ = f;
                }
            }

            return false;
        }
    }

    private boolean isOpenBlockSpace(BlockPos pos) {
        return !this.worldObj.getBlockState(pos).getBlock().isNormalCube() && !this.worldObj.getBlockState(pos.up()).getBlock().isNormalCube();
    }

    public void setSprinting(boolean sprinting) {
        super.setSprinting(sprinting);
        this.sprintingTicksLeft = sprinting ? 600 : 0;
    }

    public void setXPStats(float currentXP, int maxXP, int level) {
        this.experience = currentXP;
        this.experienceTotal = maxXP;
        this.experienceLevel = level;
    }

    public void addChatMessage(IChatComponent component) {
        this.mc.ingameGUI.getChatGUI().printChatMessage(component);
    }

    public boolean canCommandSenderUseCommand(int permLevel, String commandName) {
        return permLevel <= 0;
    }

    public BlockPos getPosition() {
        return new BlockPos(this.posX + 0.5D, this.posY + 0.5D, this.posZ + 0.5D);
    }

    public void playSound(String name, float volume, float pitch) {
        this.worldObj.playSound(this.posX, this.posY, this.posZ, name, volume, pitch, false);
    }

    public boolean isServerWorld() {
        return true;
    }

    public boolean isRidingHorse() {
        return this.ridingEntity != null && this.ridingEntity instanceof EntityHorse && ((EntityHorse) this.ridingEntity).isHorseSaddled();
    }

    public float getHorseJumpPower() {
        return this.horseJumpPower;
    }

    public void openEditSign(TileEntitySign signTile) {
        this.mc.displayGuiScreen(new GuiEditSign(signTile));
    }

    public void openEditCommandBlock(CommandBlockLogic cmdBlockLogic) {
        this.mc.displayGuiScreen(new GuiCommandBlock(cmdBlockLogic));
    }

    public void displayGUIBook(ItemStack bookStack) {
        Item item = bookStack.getItem();

        if (item == Items.writable_book) {
            this.mc.displayGuiScreen(new GuiScreenBook(this, bookStack, true));
        }
    }

    public void displayGUIChest(IInventory chestInventory) {
        String s = chestInventory instanceof IInteractionObject ? ((IInteractionObject) chestInventory).getGuiID() : "minecraft:container";

        if ("minecraft:chest".equals(s)) {
            this.mc.displayGuiScreen(new GuiChest(this.inventory, chestInventory));
        } else if ("minecraft:hopper".equals(s)) {
            this.mc.displayGuiScreen(new GuiHopper(this.inventory, chestInventory));
        } else if ("minecraft:furnace".equals(s)) {
            this.mc.displayGuiScreen(new GuiFurnace(this.inventory, chestInventory));
        } else if ("minecraft:brewing_stand".equals(s)) {
            this.mc.displayGuiScreen(new GuiBrewingStand(this.inventory, chestInventory));
        } else if ("minecraft:beacon".equals(s)) {
            this.mc.displayGuiScreen(new GuiBeacon(this.inventory, chestInventory));
        } else if (!"minecraft:dispenser".equals(s) && !"minecraft:dropper".equals(s)) {
            this.mc.displayGuiScreen(new GuiChest(this.inventory, chestInventory));
        } else {
            this.mc.displayGuiScreen(new GuiDispenser(this.inventory, chestInventory));
        }
    }

    public void displayGUIHorse(EntityHorse horse, IInventory horseInventory) {
        this.mc.displayGuiScreen(new GuiScreenHorseInventory(this.inventory, horseInventory, horse));
    }

    public void displayGui(IInteractionObject guiOwner) {
        String s = guiOwner.getGuiID();

        if ("minecraft:crafting_table".equals(s)) {
            this.mc.displayGuiScreen(new GuiCrafting(this.inventory, this.worldObj));
        } else if ("minecraft:enchanting_table".equals(s)) {
            this.mc.displayGuiScreen(new GuiEnchantment(this.inventory, this.worldObj, guiOwner));
        } else if ("minecraft:anvil".equals(s)) {
            this.mc.displayGuiScreen(new GuiRepair(this.inventory, this.worldObj));
        }
    }

    public void displayVillagerTradeGui(IMerchant villager) {
        this.mc.displayGuiScreen(new GuiMerchant(this.inventory, villager, this.worldObj));
    }

    public void onCriticalHit(Entity entityHit) {
        this.mc.effectRenderer.emitParticleAtEntity(entityHit, EnumParticleTypes.CRIT);
    }

    public void onEnchantmentCritical(Entity entityHit) {
        this.mc.effectRenderer.emitParticleAtEntity(entityHit, EnumParticleTypes.CRIT_MAGIC);
    }

    public boolean isSneaking() {
        boolean flag = this.movementInput != null && this.movementInput.sneak;
        return flag && !this.sleeping;
    }

    public void updateEntityActionState() {
        super.updateEntityActionState();

        if (this.isCurrentViewEntity()) {
            this.moveStrafing = this.movementInput.moveStrafe;
            this.moveForward = this.movementInput.moveForward;
            this.isJumping = this.movementInput.jump;
            this.prevRenderArmYaw = this.renderArmYaw;
            this.prevRenderArmPitch = this.renderArmPitch;
            this.renderArmPitch = (float) ((double) this.renderArmPitch + (double) (this.rotationPitch - this.renderArmPitch) * 0.5D);
            this.renderArmYaw = (float) ((double) this.renderArmYaw + (double) (this.rotationYaw - this.renderArmYaw) * 0.5D);
        }
    }

    protected boolean isCurrentViewEntity() {
        return this.mc.getRenderViewEntity() == this;
    }

    public void onLivingUpdate() {
        if (this.sprintingTicksLeft > 0) {
            --this.sprintingTicksLeft;

            if (this.sprintingTicksLeft == 0) {
                this.setSprinting(false);
            }
        }

        if (this.sprintToggleTimer > 0) {
            --this.sprintToggleTimer;
        }

        this.prevTimeInPortal = this.timeInPortal;

        if (this.inPortal) {
            if (this.mc.currentScreen != null && !this.mc.currentScreen.doesGuiPauseGame()) {
                this.mc.displayGuiScreen(null);
            }

            if (this.timeInPortal == 0.0F) {
                this.mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("portal.trigger"), this.rand.nextFloat() * 0.4F + 0.8F));
            }

            this.timeInPortal += 0.0125F;

            if (this.timeInPortal >= 1.0F) {
                this.timeInPortal = 1.0F;
            }

            this.inPortal = false;
        } else if (this.isPotionActive(Potion.confusion) && this.getActivePotionEffect(Potion.confusion).getDuration() > 60) {
            this.timeInPortal += 0.006666667F;

            if (this.timeInPortal > 1.0F) {
                this.timeInPortal = 1.0F;
            }
        } else {
            if (this.timeInPortal > 0.0F) {
                this.timeInPortal -= 0.05F;
            }

            if (this.timeInPortal < 0.0F) {
                this.timeInPortal = 0.0F;
            }
        }

        if (this.timeUntilPortal > 0) {
            --this.timeUntilPortal;
        }

        boolean flag = this.movementInput.jump;
        boolean flag1 = this.movementInput.sneak;
        float f = 0.8F;
        boolean flag2 = this.movementInput.moveForward >= f;
        this.movementInput.updatePlayerMoveState();

        EventNoSlow event = new EventNoSlow();
        Lecture.INSTANCE.getEventManager().dispatch(event);
        if (this.isUsingItem() && !this.isRiding() && !event.isCancelled()) {
            this.movementInput.moveStrafe *= .2f;
            this.movementInput.moveForward *= .2f;
            this.sprintToggleTimer = 0;
        }

        this.pushOutOfBlocks(this.posX - (double) this.width * 0.35D, this.getEntityBoundingBox().minY + 0.5D, this.posZ + (double) this.width * 0.35D);
        this.pushOutOfBlocks(this.posX - (double) this.width * 0.35D, this.getEntityBoundingBox().minY + 0.5D, this.posZ - (double) this.width * 0.35D);
        this.pushOutOfBlocks(this.posX + (double) this.width * 0.35D, this.getEntityBoundingBox().minY + 0.5D, this.posZ - (double) this.width * 0.35D);
        this.pushOutOfBlocks(this.posX + (double) this.width * 0.35D, this.getEntityBoundingBox().minY + 0.5D, this.posZ + (double) this.width * 0.35D);
        boolean flag3 = (float) this.getFoodStats().getFoodLevel() > 6.0F || this.capabilities.allowFlying;

        if (this.onGround && !flag1 && !flag2 && this.movementInput.moveForward >= f && !this.isSprinting() && flag3 && !this.isUsingItem() && !this.isPotionActive(Potion.blindness)) {
            if (this.sprintToggleTimer <= 0 && !this.mc.gameSettings.keyBindSprint.isKeyDown()) {
                this.sprintToggleTimer = 7;
            } else {
                this.setSprinting(true);
            }
        }

        if (!this.isSprinting() && this.movementInput.moveForward >= f && flag3 && !this.isUsingItem() && !this.isPotionActive(Potion.blindness) && this.mc.gameSettings.keyBindSprint.isKeyDown()) {
            this.setSprinting(true);
        }

        if (this.isSprinting() && (this.movementInput.moveForward < f || this.isCollidedHorizontally || !flag3)) {
            this.setSprinting(false);
        }

        if (this.capabilities.allowFlying) {
            if (this.mc.playerController.isSpectatorMode()) {
                if (!this.capabilities.isFlying) {
                    this.capabilities.isFlying = true;
                    this.sendPlayerAbilities();
                }
            } else if (!flag && this.movementInput.jump) {
                if (this.flyToggleTimer == 0) {
                    this.flyToggleTimer = 7;
                } else {
                    this.capabilities.isFlying = !this.capabilities.isFlying;
                    this.sendPlayerAbilities();
                    this.flyToggleTimer = 0;
                }
            }
        }

        if (this.capabilities.isFlying && this.isCurrentViewEntity()) {
            if (this.movementInput.sneak) {
                this.motionY -= this.capabilities.getFlySpeed() * 3.0F;
            }

            if (this.movementInput.jump) {
                this.motionY += this.capabilities.getFlySpeed() * 3.0F;
            }
        }

        if (this.isRidingHorse()) {
            if (this.horseJumpPowerCounter < 0) {
                ++this.horseJumpPowerCounter;

                if (this.horseJumpPowerCounter == 0) {
                    this.horseJumpPower = 0.0F;
                }
            }

            if (flag && !this.movementInput.jump) {
                this.horseJumpPowerCounter = -10;
                this.sendHorseJump();
            } else if (!flag && this.movementInput.jump) {
                this.horseJumpPowerCounter = 0;
                this.horseJumpPower = 0.0F;
            } else if (flag) {
                ++this.horseJumpPowerCounter;

                if (this.horseJumpPowerCounter < 10) {
                    this.horseJumpPower = (float) this.horseJumpPowerCounter * 0.1F;
                } else {
                    this.horseJumpPower = 0.8F + 2.0F / (float) (this.horseJumpPowerCounter - 9) * 0.1F;
                }
            }
        } else {
            this.horseJumpPower = 0.0F;
        }

        super.onLivingUpdate();

        if (this.onGround && this.capabilities.isFlying && !this.mc.playerController.isSpectatorMode()) {
            this.capabilities.isFlying = false;
            this.sendPlayerAbilities();
        }
    }

    @Override
    public void moveEntity(double x, double y, double z) {
        EventMove event = new EventMove(x, y, z);
        Lecture.INSTANCE.getEventManager().dispatch(event);
        if (!event.isCancelled()) {
            x = event.getX();
            y = event.getY();
            z = event.getZ();
            super.moveEntity(x, y, z);
        }
    }

    @Override
    public void jump() {
        EventJump eventJump = new EventJump(0.2F, this.getJumpUpwardsMotion(), this.rotationYaw);
        Lecture.INSTANCE.getEventManager().dispatch(eventJump);
        if (eventJump.isCancelled()) return;
        this.motionY = eventJump.getJumpMotion();

        if (this.isPotionActive(Potion.jump))
            this.motionY += ((float) (this.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F);


        if (this.isSprinting()) {
            float f = eventJump.getYaw() * 0.017453292F;
            this.motionX -= (MathHelper.sin(f) * eventJump.getSpeed());
            this.motionZ += (MathHelper.cos(f) * eventJump.getSpeed());
        }

        this.isAirBorne = true;
        this.triggerAchievement(StatList.jumpStat);

        if (this.isSprinting()) {
            this.addExhaustion(0.8F);
        } else {
            this.addExhaustion(0.2F);
        }
    }

    @Override
    public void moveFlying(float strafe, float forward, float friction) {
        EventStrafe event = new EventStrafe(forward, strafe, this.rotationYaw, friction);
        Lecture.INSTANCE.getEventManager().dispatch(event);
        forward = event.forward;
        strafe = event.strafe;
        friction = event.friction;

        float f = strafe * strafe + forward * forward;
        if (event.isCancelled()) {
            return;
        }

        if (f >= 1.0E-4F) {
            f = MathHelper.sqrt_float(f);

            if (f < 1.0F) {
                f = 1.0F;
            }

            f = friction / f;
            strafe = strafe * f;
            forward = forward * f;
            float f1 = MathHelper.sin(event.yaw * (float) Math.PI / 180.0F);
            float f2 = MathHelper.cos(event.yaw * (float) Math.PI / 180.0F);
            this.motionX += (strafe * f2 - forward * f1);
            this.motionZ += (forward * f2 + strafe * f1);
        }
    }

    public float getDirection() {
        float yaw = this.rotationYaw;
        if (this.moveForward < 0)
            yaw += 180;
        float forward = 1;
        if (this.moveForward < 0)
            forward = -.5F;
        else if (moveForward > 0)
            forward = .5F;
        if (moveStrafing > 0)
            yaw -= 90 * forward;
        if (moveStrafing < 0)
            yaw += 90 * forward;
        yaw *= .017453292;
        return yaw;
    }

    public int[] getPlayerAimInfo(float yaw, float pitch, double range) {
        int[] aimInfo = new int[8];

        Minecraft mc = Minecraft.getMinecraft();
        EntityLivingBase player = mc.thePlayer;

        float partialTicks = mc.timer.renderPartialTicks;
        Vec3 playerPos = getPlayerPosition(player, partialTicks);
        Vec3 playerLook = getPlayerLook(yaw, pitch);

        MovingObjectPosition blockHit = rayTraceBlocks(playerPos, playerLook, false);
        MovingObjectPosition entityHit = rayTraceEntities(playerPos, playerLook, range);

        if (blockHit == null) {
            aimInfo[0] = 1; // Not aiming at anything
        } else if (blockHit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            aimInfo[0] = 2; // Aiming at a block
            aimInfo[1] = blockHit.sideHit.getIndex(); // Side hit index
            aimInfo[2] = blockHit.getBlockPos().getX(); // Block X coordinate
            aimInfo[3] = blockHit.getBlockPos().getY(); // Block Y coordinate
            aimInfo[4] = blockHit.getBlockPos().getZ(); // Block Z coordinate
            aimInfo[5] = (int) (blockHit.hitVec.xCoord * 16.0D); // Hit X coordinate
            aimInfo[6] = (int) (blockHit.hitVec.yCoord * 16.0D); // Hit Y coordinate
            aimInfo[7] = (int) (blockHit.hitVec.zCoord * 16.0D); // Hit Z coordinate
        } else if (entityHit != null) {
            aimInfo[0] = 3; // Aiming at an entity
            aimInfo[1] = entityHit.entityHit.getEntityId(); // Entity ID
        }

        return aimInfo;
    }

    public Vec3 getPlayerPosition(EntityLivingBase entity, float partialTicks) {
        double x = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks;
        double y = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks;
        double z = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks;

        return new Vec3(x, y, z);
    }

    public Vec3 getPlayerLook(float yaw, float pitch) {
        float f1 = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f3 = -MathHelper.cos(-pitch * 0.017453292F);
        float f4 = MathHelper.sin(-pitch * 0.017453292F);

        return new Vec3(f2 * f3, f4, f1 * f3);
    }

    public MovingObjectPosition rayTraceBlocks(Vec3 start, Vec3 end, boolean stopOnLiquid) {
        return Minecraft.getMinecraft().theWorld.rayTraceBlocks(start, end, stopOnLiquid);
    }

    public MovingObjectPosition rayTraceEntities(Vec3 start, Vec3 end, double range) {
        Entity pointedEntity = null;
        Vec3 playerPos = Minecraft.getMinecraft().thePlayer.getPositionVector();
        Vec3 lookVec = end.subtract(start).normalize();
        Vec3 extendedLookVec = playerPos.addVector(lookVec.xCoord * range, lookVec.yCoord * range, lookVec.zCoord * range);

        double minDistance = range;

        for (int i = 0; i < Minecraft.getMinecraft().theWorld.loadedEntityList.size(); ++i) {
            Entity entity = Minecraft.getMinecraft().theWorld.loadedEntityList.get(i);

            if (entity.canBeCollidedWith() && (entity != Minecraft.getMinecraft().thePlayer || Minecraft.getMinecraft().gameSettings.thirdPersonView != 0)) {
                float collisionBorderSize = entity.getCollisionBorderSize();
                AxisAlignedBB entityBoundingBox = entity.getEntityBoundingBox().expand(collisionBorderSize, collisionBorderSize, collisionBorderSize);
                MovingObjectPosition interceptPosition = entityBoundingBox.calculateIntercept(start, extendedLookVec);

                if (interceptPosition != null) {
                    double distanceToEntity = start.distanceTo(interceptPosition.hitVec);

                    if (distanceToEntity < minDistance) {
                        pointedEntity = entity;
                        minDistance = distanceToEntity;
                    }
                }
            }
        }

        if (pointedEntity != null) {
            return new MovingObjectPosition(pointedEntity);
        }

        return null;
    }

    public int[] getRightMouseOver() {
        Minecraft mc = Minecraft.getMinecraft();
        MovingObjectPosition objectMouseOver = mc.objectMouseOver;
        if (objectMouseOver == null || objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.MISS) {
            // Not aiming at anything
            return new int[]{1};
        } else if (objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            // Aiming at an entity
            int entityId = objectMouseOver.entityHit.getEntityId();
            return new int[]{3, entityId};
        } else if (objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            // Aiming at a block
            int sideHit = objectMouseOver.sideHit.getIndex();
            int blockX = objectMouseOver.getBlockPos().getX();
            int blockY = objectMouseOver.getBlockPos().getY();
            int blockZ = objectMouseOver.getBlockPos().getZ();
            double hitX = objectMouseOver.hitVec.xCoord;
            double hitY = objectMouseOver.hitVec.yCoord;
            double hitZ = objectMouseOver.hitVec.zCoord;
            return new int[]{2, sideHit, blockX, blockY, blockZ, (int) hitX, (int) hitY, (int) hitZ};
        }

        return null;
    }
}
