package best.lettuce.gui.altmanager.panels;

import best.lettuce.Lettuce;
import best.lettuce.gui.Screen;
import best.lettuce.gui.altmanager.Panel;
import best.lettuce.gui.altmanager.helpers.Alt;
import best.lettuce.gui.altmanager.helpers.AltManagerUtils;
import best.lettuce.gui.notification.NotificationManager;
import best.lettuce.gui.notification.NotificationType;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.animation.impl.DecelerateAnimation;
import best.lettuce.utils.ban.HypixelBan;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.fonts.FontUtils;
import best.lettuce.utils.math.MathUtils;
import best.lettuce.utils.math.TimerUtils;
import best.lettuce.utils.misc.IOUtils;
import best.lettuce.utils.misc.MouseUtils;
import best.lettuce.utils.non_utils.scroll.Scroll;
import best.lettuce.utils.non_utils.text.TextField;
import best.lettuce.utils.render.GLUtils;
import best.lettuce.utils.render.RenderUtils;
import best.lettuce.utils.render.RoundedUtils;
import best.lettuce.utils.render.StencilUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

public class AltPanel extends Panel {
    private final CopyOnWriteArrayList<AltRect> altRects = new CopyOnWriteArrayList<>(), visibleAltRects = new CopyOnWriteArrayList<>();
    private final Scroll scroll = new Scroll();
    private static boolean canDrag = false;
    private boolean altsSelected = false;
    private static boolean select = false;
    private static AltRect shiftClickStart;
    private static AltRect firstClickAlt;
    public static AltRect loadingAltRect;
    private static AltRect[] shiftClickRange;
    private static final TimerUtils doubleClickTimer = new TimerUtils();
    private boolean hoveringScrollBar = false;
    private boolean draggingScrollBar = false;
    public static boolean refreshing = false;

    @Override
    public void initGui() {
        refreshAlts();
    }


    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (Lettuce.INSTANCE.getAltManager().isTyping()) return;
        if (GuiScreen.isKeyComboCtrlA(keyCode)) {
            altsSelected = !altsSelected;
            visibleAltRects.forEach(altRect -> altRect.setSelected(altsSelected));
            return;
        }

        if (GuiScreen.isKeyComboCtrlV(keyCode)) {
            String[] lines = IOUtils.getClipboardString().split("\n");
            for (String line : lines) {
                String[] split = line.split(":");
                if (split.length != 2) continue;
                Alt alt = new Alt(split[0], split[1]);
                AltManagerUtils.getAlts().add(alt);
            }
            refreshAlts();
            return;
        }

        if (!altsSelected) return;

        if (GuiScreen.isKeyComboCtrlC(keyCode)) {
            StringBuilder stringBuilder = new StringBuilder();
            int count = 0;
            for (AltRect altRect : visibleAltRects) {
                if (!altRect.isSelected()) continue;
                Alt alt = altRect.alt;
                if (alt.email == null) continue;
                stringBuilder.append(alt.email).append(":").append(alt.password).append("\n");
                count++;
            }
            NotificationManager.post(NotificationType.SUCCESS, "Success", "Copied " + count + " alts to clipboard");
            IOUtils.copy(stringBuilder.toString());
            return;
        }

        if (keyCode == Keyboard.KEY_DELETE) {
            int count = 0;
            for (AltRect altRect : visibleAltRects) {
                if (!altRect.isSelected()) continue;
                Alt alt = altRect.alt;
                AltManagerUtils.removeAlt(alt);
                count++;
            }
            AltManagerUtils.writeAlts();
            refreshAlts();
            NotificationManager.post(NotificationType.SUCCESS, "Success", "Removed " + count + " alts");
        }
    }

    private boolean needsScrollBar = false;

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        ScaledResolution sr = new ScaledResolution();
        setWidth(sr.getWidth() - (getX() + 16));
        setHeight(sr.getHeight() - (16 + 75));
        setY(75);

        super.drawScreen(mouseX, mouseY);

        performShiftClick();

        TextField searchField = Lettuce.INSTANCE.getAltManager().searchField;
        boolean filterBanned = Lettuce.INSTANCE.getAltManager().filterBanned.isEnabled();
        boolean searching = searchField.isFocused() || !searchField.getText().trim().isEmpty();
        String text = searchField.getText().toLowerCase();

        visibleAltRects.clear();
        altRects.stream().filter(altRect -> {
            Alt alt = altRect.getAlt();
            if (filterBanned && (alt.hypixelBan != null && alt.hypixelBan.getUnbanDate() > System.currentTimeMillis()))
                return false;
            if (searching) {
                return (alt.username != null && alt.username.toLowerCase().contains(text)) || (alt.email != null && alt.email.toLowerCase().contains(text)) || (alt.uuid != null && alt.uuid.toLowerCase().contains(text));
            } else {
                return true;
            }
        }).forEach(visibleAltRects::add);

        altsSelected = visibleAltRects.stream().anyMatch(AltRect::isSelected);

        if (refreshing) {
            return;
        }

        if (MouseUtils.isHovering(getX(), getY(), getWidth(), getHeight(), mouseX, mouseY)) {
            scroll.onScroll(35);
        }

        float testWidth = getWidth() - 30;
        int minWidth = 150;
        int startX = 10;
        int altRowCount = -1;
        for (int i = startX; i <= testWidth; i += minWidth) {
            altRowCount++;
        }

        // add 10 cause spacing
        float spaceLeft = testWidth - (altRowCount * (minWidth + 10));

        float altRectWidth = minWidth + (spaceLeft / altRowCount);
        float altRectHeight = 40;

        float altX = getX() + 10;
        float altY = getY() + 10;

        if (needsScrollBar) {
            float scrollHeight = (getHeight() - (10));
            float scrollX = getX() + getWidth() - 15;
            float scrollWidth = 7;
            RoundedUtils.drawRound(scrollX, getY() + 5, scrollWidth, scrollHeight, 3.5f, ColorUtils.tripleColor(37));
            float percent = scroll.getScroll() / scroll.getMaxScroll();

            hoveringScrollBar = MouseUtils.isHovering(scrollX, getY() + 5 - ((scrollHeight - 60) * percent), scrollWidth, 60, mouseX, mouseY);

            if (draggingScrollBar) {
                float dragPercent = Math.min(1, Math.max(0, (mouseY - (getY() + 5)) / scrollHeight));
                scroll.setRawScroll(-scroll.getMaxScroll() * dragPercent);
            }

            RoundedUtils.drawRound(scrollX, getY() + 5 - ((scrollHeight - 60) * percent), scrollWidth, 60, 3.5f, ColorUtils.tripleColor(hoveringScrollBar ? 65 : 55));
        }

        StencilUtils.initStencilToWrite();
        RoundedUtils.drawRound(getX(), getY(), getWidth(), getHeight(), 6, Color.BLACK);
        StencilUtils.readStencilBuffer(1);

        for (AltRect altRect : visibleAltRects) {
            altRect.setWidth(altRectWidth);
            altRect.setHeight(altRectHeight);

            if (altX + altRectWidth > getX() + (getWidth() - 10)) {
                altX = getX() + 10;
                altY += altRect.getHeight() + 10;
            }

            altRect.setX(altX);
            altRect.setY((float) (altY + MathUtils.roundToHalf(scroll.getScroll())));

            if (altRect.getY() + altRect.getHeight() > getY() && altRect.getY() < getY() + getHeight()) {
                altRect.setClickable(true);
                altRect.drawScreen(mouseX, mouseY);
            } else {
                altRect.setClickable(false);
            }

            altX += altRectWidth + 10;
        }

        scroll.setMaxScroll(Math.max(0, altY - getY() - getHeight() + altRectHeight + 10));

        needsScrollBar = altY + altRectHeight > getY() + getHeight();

        StencilUtils.uninitStencilBuffer();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (!MouseUtils.isHovering(getX(), getY(), getWidth(), getHeight(), mouseX, mouseY) && altsSelected) {
            visibleAltRects.forEach(altRect -> altRect.setSelected(false));
            altsSelected = false;
            return;
        }
        if (hoveringScrollBar) {
            draggingScrollBar = true;
        }

        visibleAltRects.forEach(altRect -> altRect.mouseClicked(mouseX, mouseY, button));
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        draggingScrollBar = false;
    }

    public void refreshAlts() {
        refreshing = true;
        altRects.clear();
        visibleAltRects.clear();
        for (Alt alt : AltManagerUtils.getAlts()) {
            altRects.add(new AltRect(alt));
        }
        Collections.reverse(altRects);
        //altRects.sort(Comparator.<AltRect, Boolean>comparing(altRect -> altRect.getAlt().favorite).reversed());
        refreshing = false;
    }

    @Getter
    @Setter
    public static class AltRect implements Screen {
        private float x, y, width, height;
        private Color backgroundColor = ColorUtils.tripleColor(37);
        private boolean selected, currentAccount, hovering, clickable = true, removeShit;
        private Alt alt;

        //private final DoubleIconButton favoriteButton = new DoubleIconButton(FontUtils.STAR_OUTLINE, FontUtils.STAR);
        private final Animation hoverAnimation = new DecelerateAnimation(250, 1);
        private final Animation selectAnimation = new DecelerateAnimation(100, 1);

        private boolean hoveringCreds = false, showCreds = false;
        private Animation credsAnimation = new DecelerateAnimation(200, 1);

        public AltRect(Alt alt) {
            this.alt = alt;
            if (alt != null) {
                //favoriteButton.setEnabled(alt.favorite);
            }
        }

        @Override
        public void initGui() {

        }

        @Override
        public void keyTyped(char typedChar, int keyCode) {

        }

        @Override
        public void drawScreen(int mouseX, int mouseY) {
            hovering = MouseUtils.isHovering(x, y, width, height, mouseX, mouseY);
            hoverAnimation.setDirection(hovering ? Direction.FORWARDS : Direction.BACKWARDS);
            hoverAnimation.setDuration(hovering ? 200 : 350);

            dragSelection(this, hovering);

            Color rectColor = backgroundColor;
            selectAnimation.setDirection(selected ? Direction.FORWARDS : Direction.BACKWARDS);
            if (selected || !selectAnimation.isDone()) {
                float outlineWidth = selectAnimation.getOutput().floatValue();
                Color outlineColor = ColorUtils.interpolateColorC(rectColor.brighter().brighter().brighter(), rectColor.brighter().brighter().brighter(), hoverAnimation.getOutput().floatValue());
                RoundedUtils.drawRound(x - outlineWidth, y - outlineWidth, width + (outlineWidth * 2), height + (outlineWidth * 2), 6, outlineColor);
            }
            //RoundedUtils.drawRound(x, y, width, height, 5, ColorUtils.interpolateColorC(rectColor, rectColor.brighter(), hoverAnimation.getOutput().floatValue()));
            Color[] colors = new Color[]{new Color(30, 30, 30, 120), new Color(30, 30, 30, 60)};
            RoundedUtils.drawRound(x, y, width, height, 5, ColorUtils.interpolateColorC(colors[0], colors[1], hoverAnimation.getOutput().floatValue()));

            float credsAnim = credsAnimation.getOutput().floatValue();
            float altSize = height - 10;
            drawAltHead(x + 3, y + height / 2f - altSize / 2f, altSize);

            RenderUtils.scaleStart(x + width / 2f, y + height / 2f, 1 - (.5f * credsAnim));

            /*if (!removeShit) {
                favoriteButton.setX(x + width - (favoriteButton.getWidth() + 5));
                favoriteButton.setY(y + 6);
                favoriteButton.setAccentColor(new Color(255, 186, 0));
                float hoverShow = favoriteButton.isEnabled() ? 1 : favoriteButton.getHoverAnimation().getOutput().floatValue();
                favoriteButton.setAlpha((1 - credsAnim) * hoverShow);
                favoriteButton.drawScreen(mouseX, mouseY);
            }*/

            int textColor = ColorUtils.applyOpacity(-1, 1 - credsAnim);
            String usernameText = "No username";
            if (alt.username != null) {
                usernameText = lettuceBoldFont16.trimStringToWidth(alt.username, (int) (width - (altSize + 10)));
            }
            float totalHeight = lettuceBoldFont16.getHeight() + 6 + lettuceFont14.getHeight();
            boolean hasBan = false;
            if (alt.hypixelBan != null) {
                HypixelBan ban = alt.hypixelBan;
                long diff = ban.getUnbanDate() - System.currentTimeMillis();
                if (diff > 0 || ban.getUnbanDate() == 0) {
                    hasBan = true;
                    totalHeight += lettuceBoldFont14.getHeight() + 4;
                }
            }

            float usernameY = y + getHeight() / 2f - totalHeight / 2f;
            ColorUtils.resetColor();
            lettuceBoldFont16.drawString(usernameText, x + 3 + altSize + 3.5f, usernameY, textColor);

            float typeX = x + 7 + altSize + lettuceBoldFont16.getStringWidth(usernameText);
            float size = lettuceBoldFont16.getHeight() + 2;
            switch (alt.getType()) {
                case "Microsoft" -> RenderUtils.drawMicrosoftLogo(typeX + 2, usernameY - 1, size, 1, 1 - credsAnim);
                case "Cracked" -> {
                    Color red = new Color(209, 56, 56);
                    lettuceBoldFont14.drawString(" Cracked", typeX, usernameY + 1, ColorUtils.applyOpacity(red, 1 - credsAnim));
                }
                case "Not logged in" ->
                        lettuceBoldFont14.drawString("§bNot logged in", typeX, usernameY + 1, ColorUtils.applyOpacity(-1, 1 - credsAnim));
            }

            drawBan(x + altSize + 6.5f, usernameY + lettuceBoldFont16.getHeight() + 6, 1 - credsAnim);
            if (hasBan) {
                usernameY += lettuceBoldFont14.getHeight() + 6;
            }

            RenderUtils.scaleEnd();

            String credsText = (showCreds ? "Hide" : "Show") + " credentials";

            hoveringCreds = MouseUtils.isHovering(x + 3 + altSize + 1.5f, (usernameY + lettuceBoldFont16.getHeight() + 6) - 2, lettuceFont14.getStringWidth("§n" + credsText) + 4, lettuceFont14.getHeight() + 4, mouseX, mouseY);

            float opacity = showCreds ? (hoveringCreds ? .75f : 0.5f) : hoveringCreds ? .5f : .35f;
            lettuceFont14.drawString("§n" + credsText, x + 3 + altSize + 3.5f, usernameY + lettuceBoldFont16.getHeight() + 6, ColorUtils.applyOpacity(-1, opacity));

            credsAnimation.setDirection(showCreds ? Direction.FORWARDS : Direction.BACKWARDS);

            if (showCreds || !credsAnimation.isDone()) {
                float yVal = y + 6;
                float xVal = x + 6 + altSize;

                RenderUtils.scaleStart(x + width / 2f, y + height / 2f, 1.3f - (.3f * credsAnim));
                if (alt.email != null) {
                    lettuceFont14.drawString("Email: " + alt.email, xVal, yVal, ColorUtils.applyOpacity(-1, credsAnim));
                    yVal += lettuceFont14.getHeight() + 4;
                }

                lettuceFont14.drawString("Password: " + alt.password, xVal, yVal, ColorUtils.applyOpacity(-1, credsAnim));
                RenderUtils.scaleEnd();
            }


            if (removeShit) return;
            if (Lettuce.INSTANCE.getAltManager().currentSessionAlt == alt) {
                Color green = new Color(70, 220, 130);
                lettuceBoldFont14.drawString("Logged in", x + width - (lettuceBoldFont14.getStringWidth("Logged in") + 5), y + height - (lettuceBoldFont14.getHeight() + 5), green);
            } else {
                if (loadingAltRect == this) {
                    float iconWidth = iconFont16.getStringWidth(FontUtils.REFRESH);
                    float iconHeight = iconFont16.getHeight();
                    float iconX = x + width - (iconWidth + 5);
                    float iconY = y + height - (iconHeight + 5);
                    RenderUtils.rotateStartReal(iconX + iconWidth / 2f, iconY + iconHeight / 2f - 1, (System.currentTimeMillis() % 1080) / 3f);
                    iconFont16.drawString(FontUtils.REFRESH, iconX, iconY, ColorUtils.applyOpacity(-1, .5f));
                    RenderUtils.rotateEnd();
                } else {
                    Alt.AltState altState = alt.altState;
                    if (altState == null) return;
                    String text = altState.getIcon();
                    iconFont16.drawString(text, x + width - (iconFont16.getStringWidth(text) + 5), y + height - (iconFont16.getHeight() + 5), ColorUtils.applyOpacity(-1, .5f));
                }
            }
        }

        @Override
        public void mouseClicked(int mouseX, int mouseY, int button) {
            if (clickable) {
                if (!showCreds) {
                    /*if (!removeShit) {
                        if (alt == null) return;
                        favoriteButton.mouseClicked(mouseX, mouseY, button);
                        alt.favorite = favoriteButton.isEnabled();
                    }*/
                }

                if (hoveringCreds) {
                    showCreds = !showCreds;
                    return;
                }

                if (/*!favoriteButton.isHovering() && */hovering && button == 0) {
                    if (!doubleClickTimer.hasTimeElapsed(400) && firstClickAlt == this) {
                        firstClickAlt.getAlt().loginAsync();
                        loadingAltRect = this;
                        firstClickAlt = null;
                        return;
                    }
                    firstClickAlt = this;
                    doubleClickTimer.reset();
                    selected = !selected;
                    canDrag = true;
                    hoveringAlt = this;
                    if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && shiftClickStart != null) {
                        shiftClickRange = new AltRect[]{shiftClickStart, this};
                    }

                    select = selected;
                    shiftClickStart = (shiftClickStart == this) ? null : this;
                }
            }
        }

        @Override
        public void mouseReleased(int mouseX, int mouseY, int state) {

        }

        public void drawAltHead(float x, float y, float size) {
            Lettuce.INSTANCE.getAltManager().getUtils().getHead(alt);
            GLUtils.startBlend();
            RenderUtils.setAlphaLimit(0);
            mc.getTextureManager().bindTexture(alt.head != null ? alt.head : new ResourceLocation("Lettuce/X-Steve.png"));
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, size, size, size, size);
            GLUtils.endBlend();
        }

        public void drawBan(float x, float y, float alpha) {
            if (alt.hypixelBan != null) {
                HypixelBan ban = alt.hypixelBan;
                long diff = ban.getUnbanDate() - System.currentTimeMillis();
                if (diff > 0) {
                    long diffSeconds = diff / 1000 % 60;
                    long diffMinutes = diff / (60 * 1000) % 60;
                    long diffHours = diff / (60 * 60 * 1000) % 24;
                    long diffDays = diff / (24 * 60 * 60 * 1000);
                    String str = diffSeconds + "s";
                    if (diffMinutes > 0) str = diffMinutes + "m " + str;
                    if (diffHours > 0) str = diffHours + "h " + str;
                    if (diffDays > 0) str = diffDays + "d " + str;
                    lettuceFont14.drawString("§lBanned §ron Hypixel for §l" + str, x, y, ColorUtils.applyOpacity(-1, alpha * .5f));
                } else if (ban.getUnbanDate() == 0) {
                    lettuceFont14.drawString("§lBanned §ron Hypixel permanently", x, y, ColorUtils.applyOpacity(-1, alpha * .5f));
                }
            }
        }
    }


    private static AltRect hoveringAlt;

    private static void dragSelection(AltRect altRect, boolean hovering) {
        boolean dragging = Mouse.isButtonDown(0) && canDrag;

        if (!Mouse.isButtonDown(0)) {
            canDrag = false;
        }

        if (dragging && hoveringAlt != altRect && hovering) {
            altRect.selected = !altRect.selected;
            hoveringAlt = altRect;
        }
    }

    private void performShiftClick() {
        if (shiftClickRange != null) {
            AltRect start = shiftClickRange[0];
            AltRect end = shiftClickRange[1];
            int startIndex = visibleAltRects.indexOf(start);
            int endIndex = visibleAltRects.indexOf(end);

            for (int i = Math.min(startIndex, endIndex); i <= Math.max(startIndex, endIndex); i++) {
                if (i != -1) {
                    visibleAltRects.get(i).setSelected(select);
                }
            }

            shiftClickRange = null;
        }
    }

}
