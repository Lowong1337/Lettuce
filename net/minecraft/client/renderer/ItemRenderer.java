package net.minecraft.client.renderer;

import best.lettuce.Lettuce;
import best.lettuce.modules.impl.render.Animations;
import best.lettuce.modules.impl.render.Camera;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.src.OFConfig;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;
import net.optifine.DynamicLights;
import net.optifine.reflect.Reflector;
import net.optifine.shaders.Shaders;
import org.lwjgl.opengl.GL11;

public class ItemRenderer {
    private static final ResourceLocation RES_MAP_BACKGROUND = new ResourceLocation("textures/map/map_background.png");
    private static final ResourceLocation RES_UNDERWATER_OVERLAY = new ResourceLocation("textures/misc/underwater.png");
    private final Minecraft mc;
    private ItemStack itemToRender;
    private float equippedProgress;
    private float prevEquippedProgress;
    private final RenderManager renderManager;
    private final RenderItem itemRenderer;
    private int equippedItemSlot = -1;

    public ItemRenderer(Minecraft mcIn) {
        this.mc = mcIn;
        this.renderManager = mcIn.getRenderManager();
        this.itemRenderer = mcIn.getRenderItem();
    }

    public void renderItem(EntityLivingBase entityIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform) {
        if (heldStack != null) {
            Item item = heldStack.getItem();
            Block block = Block.getBlockFromItem(item);
            GlStateManager.pushMatrix();

            if (this.itemRenderer.shouldRenderItemIn3D(heldStack)) {
                GlStateManager.scale(2.0F, 2.0F, 2.0F);

                if (this.isBlockTranslucent(block) && (!OFConfig.isShaders() || !Shaders.renderItemKeepDepthMask)) {
                    GlStateManager.depthMask(false);
                }
            }

            this.itemRenderer.renderItemModelForEntity(heldStack, entityIn, transform);

            if (this.isBlockTranslucent(block)) {
                GlStateManager.depthMask(true);
            }

            GlStateManager.popMatrix();
        }
    }

    private boolean isBlockTranslucent(Block blockIn) {
        return blockIn != null && blockIn.getBlockLayer() == EnumWorldBlockLayer.TRANSLUCENT;
    }

    private void rotateArroundXAndY(float angle, float angleY) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(angle, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(angleY, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    private void setLightMapFromPlayer(AbstractClientPlayer clientPlayer) {
        int i = this.mc.theWorld.getCombinedLight(new BlockPos(clientPlayer.posX, clientPlayer.posY + (double) clientPlayer.getEyeHeight(), clientPlayer.posZ), 0);

        if (OFConfig.isDynamicLights()) {
            i = DynamicLights.getCombinedLight(this.mc.getRenderViewEntity(), i);
        }

        float f = (float) (i & 65535);
        float f1 = (float) (i >> 16);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, f, f1);
    }

    private void rotateWithPlayerRotations(EntityPlayerSP entityplayerspIn, float partialTicks) {
        float f = entityplayerspIn.prevRenderArmPitch + (entityplayerspIn.renderArmPitch - entityplayerspIn.prevRenderArmPitch) * partialTicks;
        float f1 = entityplayerspIn.prevRenderArmYaw + (entityplayerspIn.renderArmYaw - entityplayerspIn.prevRenderArmYaw) * partialTicks;
        GlStateManager.rotate((entityplayerspIn.rotationPitch - f) * 0.1F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate((entityplayerspIn.rotationYaw - f1) * 0.1F, 0.0F, 1.0F, 0.0F);
    }

    private float getMapAngleFromPitch(float pitch) {
        float f = 1.0F - pitch / 45.0F + 0.1F;
        f = MathHelper.clamp_float(f, 0.0F, 1.0F);
        f = -MathHelper.cos(f * (float) Math.PI) * 0.5F + 0.5F;
        return f;
    }

    private void renderRightArm(RenderPlayer renderPlayerIn) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(54.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(64.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(-62.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.translate(0.25F, -0.85F, 0.75F);
        renderPlayerIn.renderRightArm(this.mc.thePlayer);
        GlStateManager.popMatrix();
    }

    private void renderLeftArm(RenderPlayer renderPlayerIn) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(92.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(45.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(41.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.translate(-0.3F, -1.1F, 0.45F);
        renderPlayerIn.renderLeftArm(this.mc.thePlayer);
        GlStateManager.popMatrix();
    }

    private void renderPlayerArms(AbstractClientPlayer clientPlayer) {
        this.mc.getTextureManager().bindTexture(clientPlayer.getLocationSkin());
        Render<AbstractClientPlayer> render = this.renderManager.getEntityRenderObject(this.mc.thePlayer);
        RenderPlayer renderplayer = (RenderPlayer) render;

        if (!clientPlayer.isInvisible()) {
            GlStateManager.disableCull();
            this.renderRightArm(renderplayer);
            this.renderLeftArm(renderplayer);
            GlStateManager.enableCull();
        }
    }

    private void renderItemMap(AbstractClientPlayer clientPlayer, float pitch, float equipmentProgress, float swingProgress) {
        float f = -0.4F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
        float f1 = 0.2F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI * 2.0F);
        float f2 = -0.2F * MathHelper.sin(swingProgress * (float) Math.PI);
        GlStateManager.translate(f, f1, f2);
        float f3 = this.getMapAngleFromPitch(pitch);
        GlStateManager.translate(0.0F, 0.04F, -0.72F);
        GlStateManager.translate(0.0F, equipmentProgress * -1.2F, 0.0F);
        GlStateManager.translate(0.0F, f3 * -0.5F, 0.0F);
        GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f3 * -85.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(0.0F, 1.0F, 0.0F, 0.0F);
        this.renderPlayerArms(clientPlayer);
        float f4 = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float f5 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
        GlStateManager.rotate(f4 * -20.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f5 * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(f5 * -80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(0.38F, 0.38F, 0.38F);
        GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(0.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(-1.0F, -1.0F, 0.0F);
        GlStateManager.scale(0.015625F, 0.015625F, 0.015625F);
        this.mc.getTextureManager().bindTexture(RES_MAP_BACKGROUND);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GL11.glNormal3f(0.0F, 0.0F, -1.0F);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(-7.0D, 135.0D, 0.0D).tex(0.0D, 1.0D).endVertex();
        worldrenderer.pos(135.0D, 135.0D, 0.0D).tex(1.0D, 1.0D).endVertex();
        worldrenderer.pos(135.0D, -7.0D, 0.0D).tex(1.0D, 0.0D).endVertex();
        worldrenderer.pos(-7.0D, -7.0D, 0.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
        MapData mapdata = Items.filled_map.getMapData(this.itemToRender, this.mc.theWorld);

        if (mapdata != null) {
            this.mc.entityRenderer.getMapItemRenderer().renderMap(mapdata, false);
        }
    }

    private void renderPlayerArm(AbstractClientPlayer clientPlayer, float equipProgress, float swingProgress) {
        float f = -0.3F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
        float f1 = 0.4F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI * 2.0F);
        float f2 = -0.4F * MathHelper.sin(swingProgress * (float) Math.PI);
        GlStateManager.translate(f, f1, f2);
        GlStateManager.translate(0.64000005F, -0.6F, -0.71999997F);
        GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float f3 = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float f4 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
        GlStateManager.rotate(f4 * 70.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f3 * -20.0F, 0.0F, 0.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(clientPlayer.getLocationSkin());
        GlStateManager.translate(-1.0F, 3.6F, 3.5F);
        GlStateManager.rotate(120.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(200.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(1.0F, 1.0F, 1.0F);
        GlStateManager.translate(5.6F, 0.0F, 0.0F);
        Render<AbstractClientPlayer> render = this.renderManager.getEntityRenderObject(this.mc.thePlayer);
        GlStateManager.disableCull();
        RenderPlayer renderplayer = (RenderPlayer) render;
        renderplayer.renderRightArm(this.mc.thePlayer);
        GlStateManager.enableCull();
    }

    private void doItemUsedTransformations(float swingProgress) {
        float f = -0.4F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
        float f1 = 0.2F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI * 2.0F);
        float f2 = -0.2F * MathHelper.sin(swingProgress * (float) Math.PI);
        GlStateManager.translate(f, f1, f2);
    }

    private void performDrinking(AbstractClientPlayer clientPlayer, float partialTicks) {
        float f = (float) clientPlayer.getItemInUseCount() - partialTicks + 1.0F;
        float f1 = f / (float) this.itemToRender.getMaxItemUseDuration();
        float f2 = MathHelper.abs(MathHelper.cos(f / 4.0F * (float) Math.PI) * 0.1F);

        if (f1 >= 0.8F) {
            f2 = 0.0F;
        }

        GlStateManager.translate(0.0F, f2, 0.0F);
        float f3 = 1.0F - (float) Math.pow(f1, 27.0D);
        GlStateManager.translate(f3 * 0.6F, f3 * -0.5F, f3 * 0.0F);
        GlStateManager.rotate(f3 * 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f3 * 10.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(f3 * 30.0F, 0.0F, 0.0F, 1.0F);
    }

    private void transformFirstPersonItem(float equipProgress, float swingProgress) {
        Animations animations = Lettuce.INSTANCE.getModuleManager().getModule(Animations.class);
        GL11.glTranslatef(0.56f, -0.52f, -0.72f);
        GL11.glTranslatef(0.0f, equipProgress * -0.6f, 0.0f);
        GL11.glRotatef(45.0f, 0.0f, 1.0f, 0.0f);
        if (swingProgress > 0.0) {
            final float f = MathHelper.sin(swingProgress * swingProgress * 3.1415927f);
            final float f2 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927f);
            GL11.glRotatef(f * -20.0f, 0.0f, 1.0f, 0.0f);
            GL11.glRotatef(f2 * -20.0f, 0.0f, 0.0f, 1.0f);
            GL11.glRotatef(f2 * -80.0f, 1.0f, 0.0f, 0.0f);
        }
        float scale = 0.4f;
        if (animations.isEnabled()) {
            scale *= animations.itemSize.getValue();
        }
        GL11.glScalef(scale, scale, scale);
    }

    private void doBowTransformations(float partialTicks, AbstractClientPlayer clientPlayer) {
        GlStateManager.rotate(-18.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(-12.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-8.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(-0.9F, 0.2F, 0.0F);
        float f = (float) this.itemToRender.getMaxItemUseDuration() - ((float) clientPlayer.getItemInUseCount() - partialTicks + 1.0F);
        float f1 = f / 20.0F;
        f1 = (f1 * f1 + f1 * 2.0F) / 3.0F;

        if (f1 > 1.0F) {
            f1 = 1.0F;
        }

        if (f1 > 0.1F) {
            float f2 = MathHelper.sin((f - 0.1F) * 1.3F);
            float f3 = f1 - 0.1F;
            float f4 = f2 * f3;
            GlStateManager.translate(f4 * 0.0F, f4 * 0.01F, f4 * 0.0F);
        }

        GlStateManager.translate(f1 * 0.0F, f1 * 0.0F, f1 * 0.1F);
        GlStateManager.scale(1.0F, 1.0F, 1.0F + f1 * 0.2F);
    }

    private void doBlockTransformations() {
        GlStateManager.translate(-0.5F, 0.2F, 0.0F);
        GlStateManager.rotate(30.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
    }

    public void renderblocking(float partialTicks) {
        Animations animations = Lettuce.INSTANCE.getModuleManager().getModule(Animations.class);
        float f = 1.0F - (this.prevEquippedProgress + (this.equippedProgress - this.prevEquippedProgress) * partialTicks);
        EntityPlayerSP abstractclientplayer = this.mc.thePlayer;
        float f1 = abstractclientplayer.getSwingProgress(partialTicks);
        float f2 = abstractclientplayer.prevRotationPitch + (abstractclientplayer.rotationPitch - abstractclientplayer.prevRotationPitch) * partialTicks;
        float f3 = abstractclientplayer.prevRotationYaw + (abstractclientplayer.rotationYaw - abstractclientplayer.prevRotationYaw) * partialTicks;
        this.rotateArroundXAndY(f2, f3);
        this.setLightMapFromPlayer(abstractclientplayer);
        this.rotateWithPlayerRotations(abstractclientplayer, partialTicks);
        GlStateManager.enableRescaleNormal();
        GlStateManager.pushMatrix();

        if (this.itemToRender != null) {
            float var15;
            if (animations.isEnabled()) {
                GlStateManager.scale(1, 1, animations.itemDistance.getValue());
                GlStateManager.translate(0, animations.itemHeight.getValue(), 0);
                switch (animations.mode.getMode()) {
                    case "Exhibition" -> {
                        var15 = MathHelper.sin(MathHelper.sqrt_float(f1) * MathHelper.PI);
                        GL11.glTranslated(-0.04D, 0.13D, 0.0D);
                        transformFirstPersonItem(f / 2.5F, 0.0f);
                        GlStateManager.rotate(-var15 * 40.0F / 2.0F, var15 / 2.0F, 1.0F, 4.0F);
                        GlStateManager.rotate(-var15 * 30.0F, 1.0F, var15 / 3.0F, -0.0F);
                        doBlockTransformations();
                    }
                    case "Swang" -> {
                        GL11.glTranslated(-0.1F, 0.15F, 0.0F);
                        this.transformFirstPersonItem(f / 2.0F, f1);
                        var15 = MathHelper.sin(MathHelper.sqrt_float(f1) * 3.1415927F);
                        GlStateManager.rotate(var15 * 30.0F / 2.0F, -var15, -0.0F, 9.0F);
                        GlStateManager.rotate(var15 * 40.0F, 1.0F, -var15 / 2.0F, -0.0F);
                        this.doBlockTransformations();
                    }
                    case "Swank" -> {
                        GL11.glTranslated(-0.1F, 0.15F, 0.0F);
                        this.transformFirstPersonItem(f / 2.0F, f1);
                        var15 = MathHelper.sin(MathHelper.sqrt_float(f1) * 3.1415927F);
                        GlStateManager.rotate(var15 * 30.0F, -var15, -0.0F, 9.0F);
                        GlStateManager.rotate(var15 * 40.0F, 1.0F, -var15, -0.0F);
                        this.doBlockTransformations();
                    }
                    case "1.8" -> {
                        GL11.glTranslated(-0.1F, 0.15F, 0.0F);
                        GL11.glTranslated(0.1F, -0.15F, 0.0F);
                        this.transformFirstPersonItem(f / 2.0F, f1);
                        this.doBlockTransformations();
                    }
                    case "1.7" -> {
                        transformFirstPersonItem(f, f1);
                        doBlockTransformations();
                        GL11.glTranslated(-0.25D, 0.2D, 0.0D);
                        GL11.glTranslatef(-0.05F, mc.thePlayer.isSneaking() ? -0.2F : 0.0F, 0.1F);
                    }
                    case "Remix" -> {
                        transformFirstPersonItem(f, f1 / 40.0F);
                        doBlockTransformations();
                        GlStateManager.translate(-0.1F, 0.0F, 0.0F);
                    }
                    case "Slide" -> {
                        var15 = MathHelper.sin((float) (MathHelper.sqrt_float(f1) * Math.PI));
                        transformFirstPersonItem(0.0F, 0.0F);
                        doBlockTransformations();
                        GlStateManager.translate(-0.05F, -0.0F, 0.35F);
                        GlStateManager.rotate(-var15 * 60.0F / 2.0F, -15.0F, -0.0F, 9.0F);
                        GlStateManager.rotate(-var15 * 70.0F, 1.0F, -0.4F, -0.0F);
                    }
                    case "Swing" -> {
                        GlStateManager.translate(0.0f, 0.15f, 0.0f);
                        this.transformFirstPersonItem(f / 2.0F, f1);
                        this.doBlockTransformations();
                    }
                    case "Stella" -> {
                        transformFirstPersonItem(-0.1f, f1);
                        GlStateManager.translate(-0.5F, 0.4F, -0.2F);
                        GlStateManager.rotate(30.0F, 0.0F, 1.0F, 0.0F);
                        GlStateManager.rotate(-70.0F, 1.0F, 0.0F, 0.0F);
                        GlStateManager.rotate(40.0F, 0.0F, 1.0F, 0.0F);
                    }
                    case "Smooth" -> {
                        var15 = MathHelper.sin(MathHelper.sqrt_float(f1) * MathHelper.PI);
                        transformFirstPersonItem(f / 1.5F, 0.0f);
                        doBlockTransformations();
                        GlStateManager.translate(-0.05f, 0.3f, 0.3f);
                        GlStateManager.rotate(-var15 * 140.0f, 8.0f, 0.0f, 8.0f);
                        GlStateManager.rotate(var15 * 90.0f, 8.0f, 0.0f, 8.0f);
                    }
                    case "Swong" -> {
                        var15 = MathHelper.sin(MathHelper.sqrt_float(f1) * 3.1415927F);
                        GL11.glTranslated(this.mc.thePlayer.isSneaking() ? -0.034D : 0.03D, this.mc.thePlayer.isSneaking() ? -0.045D : -0.055D, 0.05D);
                        transformFirstPersonItem(f, 0.0F);
                        GL11.glTranslatef(0.1F, 0.4F, -0.1F);
                        GL11.glRotated((-var15 * 42.0F), (var15 / 2.0F), 0.0D, 9.0D);
                        GL11.glRotated((-var15 * 50.0F), 0.800000011920929D, (var15 / 2.0F), 0.0D);
                        doBlockTransformations();
                    }
                    case "Smoke" -> {
                        float PI = MathHelper.sin((float) (MathHelper.sqrt_float(f1) * Math.PI));
                        transformFirstPersonItem(f, 0.0F);
                        float var1 = -0.4F * MathHelper.sin(MathHelper.sqrt_float(f1) * 3.1415927F);
                        float var2 = 0.2F * MathHelper.sin(MathHelper.sqrt_float(f1) * 3.1415927F * 2.0F);
                        float var3 = -0.2F * MathHelper.sin(f1 * 3.1415927F);
                        GlStateManager.translate(var1, var2, var3);
                        doBlockTransformations();
                        GL11.glRotatef(-PI * 70.0F / 2.0F, -8.0F, 0.0F, 9.0F);
                        GL11.glRotatef(-PI * 70.0F, 1.0F, -0.4F, -0.0F);
                    }
                    case "Push" -> {
                        var15 = MathHelper.sin((float) (MathHelper.sqrt_float(f1) * Math.PI));
                        transformFirstPersonItem(f, 0.0F);
                        GlStateManager.translate(0.1F, 0.4F, -0.1F);
                        GL11.glRotated((-var15 * 30.0F), (var15 / 2.0F), 0.0D, 9.0D);
                        GL11.glRotated((-var15 * 50.0F), 0.800000011920929D, (var15 / 2.0F), 0.0D);
                        doBlockTransformations();
                    }
                }
            } else {
                this.transformFirstPersonItem(f, 0.0F);
                this.doBlockTransformations();
            }
            this.renderItem(abstractclientplayer, this.itemToRender, ItemCameraTransforms.TransformType.FIRST_PERSON);
        }

        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
    }

    public void renderItemInFirstPerson(float partialTicks) {
        Animations animations = Lettuce.INSTANCE.getModuleManager().getModule(Animations.class);
        if (!OFConfig.isShaders() || !Shaders.isSkipRenderHand()) {
            float f = 1.0F - (this.prevEquippedProgress + (this.equippedProgress - this.prevEquippedProgress) * partialTicks);
            EntityPlayerSP abstractclientplayer = this.mc.thePlayer;
            float f1 = abstractclientplayer.getSwingProgress(partialTicks);
            float f2 = abstractclientplayer.prevRotationPitch + (abstractclientplayer.rotationPitch - abstractclientplayer.prevRotationPitch) * partialTicks;
            float f3 = abstractclientplayer.prevRotationYaw + (abstractclientplayer.rotationYaw - abstractclientplayer.prevRotationYaw) * partialTicks;
            this.rotateArroundXAndY(f2, f3);
            this.setLightMapFromPlayer(abstractclientplayer);
            this.rotateWithPlayerRotations(abstractclientplayer, partialTicks);
            if (animations.isEnabled()) {
                GlStateManager.scale(1, 1, animations.itemDistance.getValue());
                GlStateManager.translate(0, animations.itemHeight.getValue(), 0);
            }
            GlStateManager.enableRescaleNormal();
            GlStateManager.pushMatrix();

            if (this.itemToRender != null) {
                if (this.itemToRender.getItem() instanceof ItemMap) {
                    this.renderItemMap(abstractclientplayer, f2, f, f1);
                } else if (abstractclientplayer.getItemInUseCount() > 0) {
                    EnumAction enumaction = this.itemToRender.getItemUseAction();
                    switch (enumaction) {
                        case NONE -> this.transformFirstPersonItem(f, 0.0F);
                        case EAT, DRINK -> {
                            this.performDrinking(abstractclientplayer, partialTicks);
                            this.transformFirstPersonItem(f, 0.0F);
                        }
                        case BLOCK -> {
                            float var15;
                            if (animations.isEnabled()) {
                                switch (animations.mode.getMode()) {
                                    case "Exhibition" -> {
                                        var15 = MathHelper.sin(MathHelper.sqrt_float(f1) * MathHelper.PI);
                                        GL11.glTranslated(-0.04D, 0.13D, 0.0D);
                                        transformFirstPersonItem(f / 2.5F, 0.0f);
                                        GlStateManager.rotate(-var15 * 40.0F / 2.0F, var15 / 2.0F, 1.0F, 4.0F);
                                        GlStateManager.rotate(-var15 * 30.0F, 1.0F, var15 / 3.0F, -0.0F);
                                        doBlockTransformations();
                                    }
                                    case "Swang" -> {
                                        GL11.glTranslated(-0.1F, 0.15F, 0.0F);
                                        this.transformFirstPersonItem(f / 2.0F, f1);
                                        var15 = MathHelper.sin(MathHelper.sqrt_float(f1) * 3.1415927F);
                                        GlStateManager.rotate(var15 * 30.0F / 2.0F, -var15, -0.0F, 9.0F);
                                        GlStateManager.rotate(var15 * 40.0F, 1.0F, -var15 / 2.0F, -0.0F);
                                        this.doBlockTransformations();
                                    }
                                    case "Swank" -> {
                                        GL11.glTranslated(-0.1F, 0.15F, 0.0F);
                                        this.transformFirstPersonItem(f / 2.0F, f1);
                                        var15 = MathHelper.sin(MathHelper.sqrt_float(f1) * 3.1415927F);
                                        GlStateManager.rotate(var15 * 30.0F, -var15, -0.0F, 9.0F);
                                        GlStateManager.rotate(var15 * 40.0F, 1.0F, -var15, -0.0F);
                                        this.doBlockTransformations();
                                    }
                                    case "1.8" -> {
                                        GL11.glTranslated(-0.1F, 0.15F, 0.0F);
                                        GL11.glTranslated(0.1F, -0.15F, 0.0F);
                                        this.transformFirstPersonItem(f / 2.0F, f1);
                                        this.doBlockTransformations();
                                    }
                                    case "1.7" -> {
                                        transformFirstPersonItem(f, f1);
                                        doBlockTransformations();
                                        GL11.glTranslated(-0.25D, 0.2D, 0.0D);
                                        GL11.glTranslatef(-0.05F, mc.thePlayer.isSneaking() ? -0.2F : 0.0F, 0.1F);
                                    }
                                    case "Remix" -> {
                                        transformFirstPersonItem(f, f1 / 40.0F);
                                        doBlockTransformations();
                                        GlStateManager.translate(-0.1F, 0.0F, 0.0F);
                                    }
                                    case "Slide" -> {
                                        var15 = MathHelper.sin((float) (MathHelper.sqrt_float(f1) * Math.PI));
                                        transformFirstPersonItem(0.0F, 0.0F);
                                        doBlockTransformations();
                                        GlStateManager.translate(-0.05F, -0.0F, 0.35F);
                                        GlStateManager.rotate(-var15 * 60.0F / 2.0F, -15.0F, -0.0F, 9.0F);
                                        GlStateManager.rotate(-var15 * 70.0F, 1.0F, -0.4F, -0.0F);
                                    }
                                    case "Swing" -> {
                                        GlStateManager.translate(0.0f, 0.15f, 0.0f);
                                        this.transformFirstPersonItem(f / 2.0F, f1);
                                        this.doBlockTransformations();
                                    }
                                    case "Stella" -> {
                                        transformFirstPersonItem(-0.1f, f1);
                                        GlStateManager.translate(-0.5F, 0.4F, -0.2F);
                                        GlStateManager.rotate(30.0F, 0.0F, 1.0F, 0.0F);
                                        GlStateManager.rotate(-70.0F, 1.0F, 0.0F, 0.0F);
                                        GlStateManager.rotate(40.0F, 0.0F, 1.0F, 0.0F);
                                    }
                                    case "Smooth" -> {
                                        var15 = MathHelper.sin(MathHelper.sqrt_float(f1) * MathHelper.PI);
                                        transformFirstPersonItem(f / 1.5F, 0.0f);
                                        doBlockTransformations();
                                        GlStateManager.translate(-0.05f, 0.3f, 0.3f);
                                        GlStateManager.rotate(-var15 * 140.0f, 8.0f, 0.0f, 8.0f);
                                        GlStateManager.rotate(var15 * 90.0f, 8.0f, 0.0f, 8.0f);
                                    }
                                    case "Swong" -> {
                                        var15 = MathHelper.sin(MathHelper.sqrt_float(f1) * 3.1415927F);
                                        GL11.glTranslated(this.mc.thePlayer.isSneaking() ? -0.034D : 0.03D, this.mc.thePlayer.isSneaking() ? -0.045D : -0.055D, 0.05D);
                                        transformFirstPersonItem(f, 0.0F);
                                        GL11.glTranslatef(0.1F, 0.4F, -0.1F);
                                        GL11.glRotated((-var15 * 42.0F), (var15 / 2.0F), 0.0D, 9.0D);
                                        GL11.glRotated((-var15 * 50.0F), 0.800000011920929D, (var15 / 2.0F), 0.0D);
                                        doBlockTransformations();
                                    }
                                    case "Smoke" -> {
                                        float PI = MathHelper.sin((float) (MathHelper.sqrt_float(f1) * Math.PI));
                                        transformFirstPersonItem(f, 0.0F);
                                        float var1 = -0.4F * MathHelper.sin(MathHelper.sqrt_float(f1) * 3.1415927F);
                                        float var2 = 0.2F * MathHelper.sin(MathHelper.sqrt_float(f1) * 3.1415927F * 2.0F);
                                        float var3 = -0.2F * MathHelper.sin(f1 * 3.1415927F);
                                        GlStateManager.translate(var1, var2, var3);
                                        doBlockTransformations();
                                        GL11.glRotatef(-PI * 70.0F / 2.0F, -8.0F, 0.0F, 9.0F);
                                        GL11.glRotatef(-PI * 70.0F, 1.0F, -0.4F, -0.0F);
                                    }
                                    case "Push" -> {
                                        var15 = MathHelper.sin((float) (MathHelper.sqrt_float(f1) * Math.PI));
                                        transformFirstPersonItem(f, 0.0F);
                                        GlStateManager.translate(0.1F, 0.4F, -0.1F);
                                        GL11.glRotated((-var15 * 30.0F), (var15 / 2.0F), 0.0D, 9.0D);
                                        GL11.glRotated((-var15 * 50.0F), 0.800000011920929D, (var15 / 2.0F), 0.0D);
                                        doBlockTransformations();
                                    }
                                }
                            } else {
                                this.transformFirstPersonItem(f, 0.0F);
                                this.doBlockTransformations();
                            }
                        }
                        case BOW -> {
                            this.transformFirstPersonItem(f, 0.0F);
                            this.doBowTransformations(partialTicks, abstractclientplayer);
                        }
                    }
                } else {
                    this.doItemUsedTransformations(f1);
                    this.transformFirstPersonItem(f, f1);
                }

                this.renderItem(abstractclientplayer, this.itemToRender, ItemCameraTransforms.TransformType.FIRST_PERSON);
            } else if (!abstractclientplayer.isInvisible()) {
                this.renderPlayerArm(abstractclientplayer, f, f1);
            }

            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
            RenderHelper.disableStandardItemLighting();
        }
    }

    public void renderOverlays(float partialTicks) {
        GlStateManager.disableAlpha();

        if (this.mc.thePlayer.isEntityInsideOpaqueBlock()) {
            IBlockState iblockstate = this.mc.theWorld.getBlockState(new BlockPos(this.mc.thePlayer));
            BlockPos blockpos = new BlockPos(this.mc.thePlayer);
            EntityPlayer entityplayer = this.mc.thePlayer;

            for (int i = 0; i < 8; ++i) {
                double d0 = entityplayer.posX + (double) (((float) ((i) % 2) - 0.5F) * entityplayer.width * 0.8F);
                double d1 = entityplayer.posY + (double) (((float) ((i >> 1) % 2) - 0.5F) * 0.1F);
                double d2 = entityplayer.posZ + (double) (((float) ((i >> 2) % 2) - 0.5F) * entityplayer.width * 0.8F);
                BlockPos blockpos1 = new BlockPos(d0, d1 + (double) entityplayer.getEyeHeight(), d2);
                IBlockState iblockstate1 = this.mc.theWorld.getBlockState(blockpos1);

                if (iblockstate1.getBlock().isVisuallyOpaque()) {
                    iblockstate = iblockstate1;
                    blockpos = blockpos1;
                }
            }

            if (iblockstate.getBlock().getRenderType() != -1) {
                Object object = Reflector.getFieldValue(Reflector.RenderBlockOverlayEvent_OverlayType_BLOCK);

                if (!Reflector.callBoolean(Reflector.ForgeEventFactory_renderBlockOverlay, this.mc.thePlayer, partialTicks, object, iblockstate, blockpos)) {
                    this.renderBlockInHand(this.mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(iblockstate));
                }
            }
        }

        if (!this.mc.thePlayer.isSpectator()) {
            if (this.mc.thePlayer.isInsideOfMaterial(Material.water) && !Reflector.callBoolean(Reflector.ForgeEventFactory_renderWaterOverlay, this.mc.thePlayer, partialTicks)) {
                this.renderWaterOverlayTexture(partialTicks);
            }

            if (this.mc.thePlayer.isBurning() && !Reflector.callBoolean(Reflector.ForgeEventFactory_renderFireOverlay, this.mc.thePlayer, partialTicks)) {
                this.renderFireInFirstPerson();
            }
        }

        GlStateManager.enableAlpha();
    }

    private void renderBlockInHand(TextureAtlasSprite atlas) {
        this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.color(0.1F, 0.1F, 0.1F, 0.5F);
        GlStateManager.pushMatrix();
        float f6 = atlas.getMinU();
        float f7 = atlas.getMaxU();
        float f8 = atlas.getMinV();
        float f9 = atlas.getMaxV();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(-1.0D, -1.0D, -0.5D).tex(f7, f9).endVertex();
        worldrenderer.pos(1.0D, -1.0D, -0.5D).tex(f6, f9).endVertex();
        worldrenderer.pos(1.0D, 1.0D, -0.5D).tex(f6, f8).endVertex();
        worldrenderer.pos(-1.0D, 1.0D, -0.5D).tex(f7, f8).endVertex();
        tessellator.draw();
        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderWaterOverlayTexture(float partialTicks) {
        if (!OFConfig.isShaders() || Shaders.isUnderwaterOverlay()) {
            this.mc.getTextureManager().bindTexture(RES_UNDERWATER_OVERLAY);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            float f = this.mc.thePlayer.getBrightness(partialTicks);
            GlStateManager.color(f, f, f, 0.5F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.pushMatrix();
            float f7 = -this.mc.thePlayer.rotationYaw / 64.0F;
            float f8 = this.mc.thePlayer.rotationPitch / 64.0F;
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.pos(-1.0D, -1.0D, -0.5D).tex(4.0F + f7, 4.0F + f8).endVertex();
            worldrenderer.pos(1.0D, -1.0D, -0.5D).tex(0.0F + f7, 4.0F + f8).endVertex();
            worldrenderer.pos(1.0D, 1.0D, -0.5D).tex(0.0F + f7, 0.0F + f8).endVertex();
            worldrenderer.pos(-1.0D, 1.0D, -0.5D).tex(4.0F + f7, 0.0F + f8).endVertex();
            tessellator.draw();
            GlStateManager.popMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
        }
    }

    private void renderFireInFirstPerson() {
        Camera camera = Lettuce.INSTANCE.getModuleManager().getModule(Camera.class);
        if (!camera.isEnabled() || !camera.options.isEnabled("Remove Fire Overlay")) {
            fire();
        }
    }

    private void fire() {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.9F);
        GlStateManager.depthFunc(519);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        float f = 1.0F;

        for (int i = 0; i < 2; ++i) {
            GlStateManager.pushMatrix();
            TextureAtlasSprite textureatlassprite = this.mc.getTextureMapBlocks().getAtlasSprite("minecraft:blocks/fire_layer_1");
            this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
            float f1 = textureatlassprite.getMinU();
            float f2 = textureatlassprite.getMaxU();
            float f3 = textureatlassprite.getMinV();
            float f4 = textureatlassprite.getMaxV();
            float f5 = (0.0F - f) / 2.0F;
            float f6 = f5 + f;
            float f7 = 0.0F - f / 2.0F;
            float f8 = f7 + f;
            float f9 = -0.5F;
            GlStateManager.translate((float) (-(i * 2 - 1)) * 0.24F, -0.3F, 0.0F);
            GlStateManager.rotate((float) (i * 2 - 1) * 10.0F, 0.0F, 1.0F, 0.0F);
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.setSprite(textureatlassprite);
            worldrenderer.pos(f5, f7, f9).tex(f2, f4).endVertex();
            worldrenderer.pos(f6, f7, f9).tex(f1, f4).endVertex();
            worldrenderer.pos(f6, f8, f9).tex(f1, f3).endVertex();
            worldrenderer.pos(f5, f8, f9).tex(f2, f3).endVertex();
            tessellator.draw();
            GlStateManager.popMatrix();
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.depthFunc(515);
    }

    public void updateEquippedItem() {
        this.prevEquippedProgress = this.equippedProgress;
        EntityPlayer entityplayer = this.mc.thePlayer;
        ItemStack itemstack = entityplayer.inventory.getCurrentItem();
        boolean flag = false;

        if (this.itemToRender != null && itemstack != null) {
            if (!this.itemToRender.getIsItemStackEqual(itemstack)) {
                if (Reflector.ForgeItem_shouldCauseReequipAnimation.exists()) {
                    boolean flag1 = Reflector.callBoolean(this.itemToRender.getItem(), Reflector.ForgeItem_shouldCauseReequipAnimation, this.itemToRender, itemstack, this.equippedItemSlot != entityplayer.inventory.currentItem);

                    if (!flag1) {
                        this.itemToRender = itemstack;
                        this.equippedItemSlot = entityplayer.inventory.currentItem;
                        return;
                    }
                }

                flag = true;
            }
        } else flag = this.itemToRender != null || itemstack != null;

        float f2 = 0.4F;
        float f = flag ? 0.0F : 1.0F;
        float f1 = MathHelper.clamp_float(f - this.equippedProgress, -f2, f2);
        this.equippedProgress += f1;

        if (this.equippedProgress < 0.1F) {
            this.itemToRender = itemstack;
            this.equippedItemSlot = entityplayer.inventory.currentItem;

            if (OFConfig.isShaders()) {
                Shaders.setItemToRenderMain(itemstack);
            }
        }
    }

    public void resetEquippedProgress() {
        this.equippedProgress = 0.0F;
    }

    public void resetEquippedProgress2() {
        this.equippedProgress = 0.0F;
    }
}
