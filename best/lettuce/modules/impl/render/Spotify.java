package best.lettuce.modules.impl.render;

import best.lettuce.Lettuce;
import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.render.EventRender2D;
import best.lettuce.event.impl.render.EventShader;
import best.lettuce.gui.notification.NotificationManager;
import best.lettuce.gui.notification.NotificationType;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.property.impl.ModeProperty;
import best.lettuce.modules.property.impl.StringProperty;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.animation.impl.DecelerateAnimation;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.fonts.CustomFont;
import best.lettuce.utils.fonts.FontUtils;
import best.lettuce.utils.misc.IOUtils;
import best.lettuce.utils.non_utils.drag.Dragging;
import best.lettuce.utils.non_utils.spotify.SpotifyAPI;
import best.lettuce.utils.render.GradientUtils;
import best.lettuce.utils.render.RenderUtils;
import best.lettuce.utils.render.RoundedUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlayingContext;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Track;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Spotify extends Module {

    private final StringProperty clientID = new StringProperty("Client ID");
    private final ModeProperty backgroundColor = new ModeProperty("Background", "Average", "Average", "Spotify Grey", "Sync");

    private final Dragging drag = createDrag(this, "spotify", 5, 150);
    public final float height = 50;
    public final float albumCoverSize = height;
    private final float playerWidth = 135;
    private final float width = albumCoverSize + playerWidth;

    private final Animation scrollTrack = new DecelerateAnimation(10000, 1, Direction.BACKWARDS);
    private final Animation scrollArtist = new DecelerateAnimation(10000, 1, Direction.BACKWARDS);
    public Animation playAnimation = new DecelerateAnimation(250, 1);

    public String[] buttons = {FontUtils.SKIP_LEFT, FontUtils.SKIP_RIGHT, FontUtils.SHUFFLE};
    public HashMap<String, Animation> buttonAnimations;

    public SpotifyAPI api;
    private CurrentlyPlayingContext currentPlayingContext;
    private Track currentTrack;
    public boolean playingMusic;
    public boolean hoveringPause;
    private boolean downloadedCover;
    private ResourceLocation currentAlbumCover;
    private Color imageColor = Color.WHITE;

    public Spotify() {
        super("Spotify", Category.RENDER, "UI for spotify");
        this.addProperties(clientID, backgroundColor);
    }

    private final Color greyColor = new Color(30, 30, 30);
    private final Color progressBackground = new Color(45, 45, 45);
    private final Color shuffleColor = new Color(50, 255, 100);
    private final Color hoveredColor = new Color(195, 195, 195);
    private final Color circleColor = new Color(50, 50, 50);

    public final EventListener<EventShader> onShader = e -> {
        if (api == null || api.currentPlayingContext == null) return;
        float x = drag.getX(), y = drag.getY();

        if (e.isBloom()) {
            if (e.getBloomOptions().getSetting("Spotify").isEnabled()) {

                Color color2 = ColorUtils.darker(imageColor, .65f);
                HUD hud = Lettuce.INSTANCE.getModuleManager().getModule(HUD.class);

                switch (backgroundColor.getMode()) {
                    case "Average" -> {
                        float[] hsb = Color.RGBtoHSB(imageColor.getRed(), imageColor.getGreen(), imageColor.getBlue(), null);
                        if (hsb[2] < .5f) {
                            color2 = ColorUtils.brighter(imageColor, .65f);
                        }
                        RoundedUtils.drawGradientVertical(x + (albumCoverSize - 15), y, playerWidth + 15, height, 6, color2, imageColor);
                    }
                    case "Spotify Grey" ->
                            RoundedUtils.drawRound(x + (albumCoverSize - 15), y, playerWidth + 15, height, 6, greyColor);
                    case "Sync" -> {
                        Color[] colors = {hud.color1.getColor(), hud.color2.getColor()};
                        RoundedUtils.drawGradientCornerLR(x + (albumCoverSize - 15), y, playerWidth + 15, height, 6, colors[0], colors[1]);
                    }
                }

                if (currentAlbumCover != null && downloadedCover) {
                    ColorUtils.resetColor();
                    mc.getTextureManager().bindTexture(currentAlbumCover);
                    RoundedUtils.drawRoundTextured(x, y, albumCoverSize, albumCoverSize, 7.5f, 1);
                }
            } else {
                RoundedUtils.drawRound(x, y, playerWidth + (albumCoverSize), height, 6, Color.BLACK);
            }
        }
    };

    public final EventListener<EventRender2D> on2d = e -> {
        if (api == null || api.currentTrack == null || api.currentPlayingContext == null) return;
        //Check if the song has changed
        if (currentTrack != api.currentTrack || currentPlayingContext != api.currentPlayingContext) {
            this.currentTrack = api.currentTrack;
            this.currentPlayingContext = api.currentPlayingContext;
        }
        playingMusic = currentPlayingContext.getIs_playing();


        float x = drag.getX(), y = drag.getY();
        drag.setWidth(width);
        drag.setHeight(height);

        Color color2 = ColorUtils.darker(imageColor, .65f);
        HUD hud = Lettuce.INSTANCE.getModuleManager().getModule(HUD.class);

        switch (backgroundColor.getMode()) {
            case "Average" -> {
                float[] hsb = Color.RGBtoHSB(imageColor.getRed(), imageColor.getGreen(), imageColor.getBlue(), null);
                if (hsb[2] < .5f) {
                    color2 = ColorUtils.brighter(imageColor, .65f);
                }
                RoundedUtils.drawRound(x + (albumCoverSize - 15), y, playerWidth + 15, height, 6, imageColor);
            }
            case "Spotify Grey" ->
                    RoundedUtils.drawRound(x + (albumCoverSize - 15), y, playerWidth + 15, height, 6, greyColor);
            case "Sync" -> {
                Color[] colors = {hud.color1.getColor(), hud.color2.getColor()};
                RoundedUtils.drawGradientCornerLR(x + (albumCoverSize - 15), y, playerWidth + 15, height, 6, colors[0], colors[1]);
            }
        }

        final int diff = currentTrack.getDurationMs() - currentPlayingContext.getProgress_ms();
        final long diffSeconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60;
        final long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;

        final String trackRemaining = String.format("-%s:%s", diffMinutes < 10 ? "0" + diffMinutes : diffMinutes, diffSeconds < 10 ? "0" + diffSeconds : diffSeconds);

        //Scroll and scissor the track name and artist if needed
        RenderUtils.scissor(x + albumCoverSize, y, playerWidth, height, () -> {
            final StringBuilder artistsDisplay = new StringBuilder();
            for (int artistIndex = 0; artistIndex < currentTrack.getArtists().length; artistIndex++) {
                final ArtistSimplified artist = currentTrack.getArtists()[artistIndex];
                artistsDisplay.append(artist.getName()).append(artistIndex + 1 == currentTrack.getArtists().length ? '.' : ", ");
            }
            if (scrollTrack.finished(Direction.BACKWARDS)) {
                scrollTrack.reset();
            }
            if (scrollArtist.finished(Direction.BACKWARDS)) {
                scrollArtist.reset();
            }
            boolean needsToScrollTrack = lettuceBoldFont26.getStringWidth(currentTrack.getName()) > playerWidth;
            boolean needsToScrollArtist = lettuceFont22.getStringWidth(artistsDisplay.toString()) > playerWidth;

            float trackX = (float) (((x + albumCoverSize) - lettuceBoldFont22.getStringWidth(currentTrack.getName())) + ((lettuceBoldFont22.getStringWidth(currentTrack.getName()) + playerWidth) * scrollTrack.getLinearOutput()));

            lettuceBoldFont22.drawString(currentTrack.getName(), needsToScrollTrack ? trackX : x + albumCoverSize + 3, y + 3, -1);

            float artistX = (float) (((x + albumCoverSize) - lettuceFont18.getStringWidth(artistsDisplay.toString())) + ((lettuceFont18.getStringWidth(artistsDisplay.toString()) + playerWidth) * scrollArtist.getLinearOutput()));

            lettuceFont18.drawString(artistsDisplay.toString(), needsToScrollArtist ? artistX : x + albumCoverSize + 4, y + 17, -1);
        });

        //Draw time left on song
        lettuceFont16.drawString(trackRemaining, x + width - (lettuceFont16.getStringWidth(trackRemaining) + 3), y + height - (lettuceFont16.getHeight() + 3), -1);

        float progressBarWidth = (playerWidth - 35);
        float progressBarHeight = 3;
        float progress = progressBarWidth * (currentPlayingContext.getProgress_ms() / (float) currentTrack.getDurationMs());
        Color progressColor = Color.GREEN;

        RoundedUtils.drawRound(x + albumCoverSize + 5, y + height - (progressBarHeight + 4.5f), progressBarWidth, progressBarHeight, 1.5f, progressBackground);
        RoundedUtils.drawRound(x + albumCoverSize + 5, y + height - (progressBarHeight + 4.5f), progress, progressBarHeight, 1.5f, progressColor);

        float spacing = 0;

        ColorUtils.resetColor();
        for (String button : buttons) {
            Color normalColor = button.equals(FontUtils.SHUFFLE) && currentPlayingContext.getShuffle_state() ? shuffleColor : Color.WHITE;
            ColorUtils.resetColor();

            icontestFont20.drawString(button, x + albumCoverSize + 6 + spacing, y + height - 19, ColorUtils.interpolateColor(normalColor, hoveredColor, buttonAnimations.get(button).getOutput().floatValue()));
            spacing += 15;
        }


        if (currentAlbumCover != null && downloadedCover) {
            mc.getTextureManager().bindTexture(currentAlbumCover);
            GlStateManager.color(1, 1, 1);
            GL11.glEnable(GL11.GL_BLEND);
            RoundedUtils.drawRoundTextured(x, y, albumCoverSize, albumCoverSize, 6, 1);
        }

        if ((currentAlbumCover == null || !currentAlbumCover.getResourcePath().contains(currentTrack.getAlbum().getId()))) {
            downloadedCover = false;
            final ThreadDownloadImageData albumCover = new ThreadDownloadImageData(null, currentTrack.getAlbum().getImages()[1].getUrl(), null, new IImageBuffer() {
                @Override
                public BufferedImage parseUserSkin(BufferedImage image) {
                    imageColor = ColorUtils.averageColor(image, image.getWidth(), image.getHeight(), 1);
                    downloadedCover = true;
                    return image;
                }

                @Override
                public void skinAvailable() {
                }
            });
            mc.getTextureManager().loadTexture(currentAlbumCover = new ResourceLocation("spotifyAlbums/" + currentTrack.getAlbum().getId()), albumCover);

        }

        playAnimation.setDirection(!playingMusic || hoveringPause ? Direction.FORWARDS : Direction.BACKWARDS);
        RoundedUtils.drawRound(x + albumCoverSize / 2f - 22.5f, y + albumCoverSize / 2f - 22.5f, 45, 45, 23, ColorUtils.applyOpacity(circleColor, (float) (.47 * playAnimation.getOutput().floatValue())));

        String playIcon = currentPlayingContext.getIs_playing() ? FontUtils.PAUSE : FontUtils.PLAY;
        icontestFont40.drawCenteredString(playIcon, x + albumCoverSize / 2f + (playIcon.equals(FontUtils.PLAY) ? 2 : 0), y + albumCoverSize / 2f - icontestFont40.getHeight() / 2f + 2, ColorUtils.applyOpacity(-1, playAnimation.getOutput().floatValue()));
        api.saveTokens();
    };


    @Override
    public void onEnable() {
        if (clientID.getString().equals("")) {
            toggle(ToggleType.AUTO);
            return;
        }
        if (buttonAnimations == null) {
            buttonAnimations = new HashMap<>();
            for (String button : buttons) {
                buttonAnimations.put(button, new DecelerateAnimation(250, 1, Direction.BACKWARDS));
            }
        }

        String clientID = this.clientID.getString();
        if (api == null) api = new SpotifyAPI();

        if (clientID.equals("")) {
            clientID = getClientIDFromJson();
            if (clientID.equals("")) {
                toggle(ToggleType.AUTO);
                return;
            }
        }

        api.build(clientID);
        setClientID(clientID);
        api.startConnection();
        super.onEnable();
    }

    public void setClientID(String clientID) {
        JsonObject keyObject = new JsonObject();
        keyObject.addProperty("clientID", clientID);
        try {
            Writer writer = new BufferedWriter(new FileWriter(SpotifyAPI.CLIENT_ID_DIR));
            SpotifyAPI.GSON.toJson(keyObject, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getClientIDFromJson() {
        JsonObject fileContent;
        try {
            fileContent = JsonParser.parseReader(new FileReader(SpotifyAPI.CLIENT_ID_DIR)).getAsJsonObject();
            if (fileContent.has("clientID")) {
                return fileContent.get("clientID").getAsString();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        openYoutubeTutorial();
        NotificationManager.post(NotificationType.WARNING, "Error", "No Client ID found");
        return "";
    }

    public void openYoutubeTutorial() {
        IOUtils.openLink("https://www.youtube.com/watch?v=3jOR29h1i40");
    }
}