package best.lettuce.gui.altmanager.helpers;

import best.lettuce.Lecture;
import best.lettuce.gui.notification.NotificationManager;
import best.lettuce.gui.notification.NotificationType;
import best.lettuce.utils.MinecraftInstance;
import best.lettuce.utils.math.TimerUtils;
import best.lettuce.utils.non_utils.microsoft.MicrosoftLogin;
import best.lettuce.utils.non_utils.multithread.Multithreading;
import best.lettuce.utils.non_utils.text.TextField;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.Session;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AltManagerUtils implements MinecraftInstance {

    @Getter
    private static List<Alt> alts = new ArrayList<>();
    private final TimerUtils timerUtil = new TimerUtils();
    public static File altsFile = new File(Lecture.DIRECTORY, "alts.json");

    public static AltManagerUtils getInstance(){
        return new AltManagerUtils();
    }
    public AltManagerUtils() {
        if (!altsFile.exists()) {
            try {
                File parentDir = altsFile.getParentFile();
                if (parentDir != null && parentDir.mkdirs()) {
                    altsFile.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            byte[] content = Files.readAllBytes(altsFile.toPath());
            alts = new ArrayList<>(Arrays.asList(new Gson().fromJson(new String(content), Alt[].class)));
            alts.forEach(this::getHead);
        } catch (IOException ignored) {}
    }

    public void writeAltsToFile() {
        if (timerUtil.hasTimeElapsed(15000, true)) {
            new Thread(() -> {
                try {
                    if (!altsFile.exists()) {
                        if (altsFile.getParentFile().mkdirs()) {
                            altsFile.createNewFile();
                        }
                    }
                    Files.write(altsFile.toPath(), new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create().toJson(alts.toArray(new Alt[0])).getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public static void removeAlt(Alt alt) {
        if (alt != null) {
            alts.remove(alt);
        }
    }

    public static void writeAlts() {
        Multithreading.runAsync(() -> {
            try {
                Files.write(altsFile.toPath(), new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create().toJson(alts.toArray(new Alt[0])).getBytes(StandardCharsets.UTF_8));
                //Show success message
            } catch (IOException e) {
                e.printStackTrace();
                //    Notification.post(NotificationType.WARNING, "Failed to save", "Failed to save alt list due to an IOException");
            }
        });
    }

    public void login(TextField username, TextField password) {
        String usernameS;
        String passwordS;
        if (username.getText().contains(":")) {
            String[] combo = username.getText().split(":");
            usernameS = combo[0];
            passwordS = combo[1];
        } else {
            usernameS = username.getText();
            passwordS = password.getText();
        }

        if (usernameS.isEmpty() && passwordS.isEmpty()) return;

        loginWithString(usernameS, passwordS, false);
    }


    public void microsoftLoginAsync(String email, String password) {
        microsoftLoginAsync(null, email, password);
    }

    public void microsoftLoginAsync(Alt alt, String email, String password) {
        NotificationManager.post(NotificationType.INFO, "Alt Manager", "Opening browser to complete Microsoft authentication...", 12);
        if (alt == null) {
            alt = new Alt(email, password);
        }
        Alt finalAlt = alt;
        Multithreading.runAsync(() -> {
            CompletableFuture<Session> future = new CompletableFuture<>();
            MicrosoftLogin.getRefreshToken(refreshToken -> {
                if (refreshToken != null) {
                    MicrosoftLogin.LoginData login = MicrosoftLogin.login(refreshToken);
                    future.complete(new Session(login.username, login.uuid, login.mcToken, "microsoft"));
                }
            });
            Session auth = future.join();
            if (auth != null) {
                mc.session = auth;
                finalAlt.uuid = auth.getPlayerID();
                finalAlt.altType = Alt.AltType.MICROSOFT;
                finalAlt.username = auth.getUsername();
                if (auth.getUsername() == null) {
                    NotificationManager.post(NotificationType.WARNING, "Alt Manager", "Please set an username on your Minecraft account!", 12);
                }
                Alt.stage = 2;
                finalAlt.altState = Alt.AltState.LOGIN_SUCCESS;
                AltManagerUtils.getAlts().add(finalAlt);
                writeAlts();
                Lecture.INSTANCE.getAltManager().currentSessionAlt = finalAlt;
                Lecture.INSTANCE.getAltManager().getAltPanel().refreshAlts();
            } else {
                Alt.stage = 1;
                finalAlt.altState = Alt.AltState.LOGIN_FAIL;
            }
        });
    }

    public void loginWithString(String username, String password, boolean microsoft) {
        for (Alt alt : alts) {
            if (alt.email.equals(username) && alt.password.equals(password)) {
                Alt.stage = 0;
                alt.loginAsync(microsoft);
                return;
            }
        }
        Alt alt = new Alt(username, password);
        alts.add(alt);
        Alt.stage = 0;
        alt.loginAsync(microsoft);
    }

    public void getHead(Alt alt) {
        if (alt.uuid == null || alt.head != null || alt.headTexture || alt.headTries > 5) return;
        Multithreading.runAsync(() -> {
            alt.headTries++;
            try {
                BufferedImage image = ImageIO.read(new URL("https://visage.surgeplay.com/bust/160/" + alt.uuid));
                alt.headTexture = true;
                // run on main thread for OpenGL context
                mc.addScheduledTask(() -> {
                    DynamicTexture texture = new DynamicTexture(image);
                    alt.head = mc.getTextureManager().getDynamicTextureLocation("HEAD-" + alt.uuid, texture);
                });
            } catch (IOException e) {
                alt.headTexture = false;
            }
        });
    }
}