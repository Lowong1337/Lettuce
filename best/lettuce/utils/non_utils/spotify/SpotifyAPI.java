package best.lettuce.utils.non_utils.spotify;

import best.lettuce.Lecture;
import best.lettuce.utils.MinecraftInstance;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlayingContext;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import org.apache.hc.core5.http.ParseException;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

interface SpotifyCallBack {
    void codeCallback(final String code);
}

public class SpotifyAPI implements MinecraftInstance {
    public final static String CODE_CHALLENGE = "w6iZIj99vHGtEx_NVl9u3sthTN646vvkiP8OMCGfPmo";
    private final static String CODE_VERIFIER = "NlJx4kD4opk4HY7zBM6WfUHxX7HoF8A2TUhOIPGA74w";
    public final static Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().create();
    public final static File CLIENT_ID_DIR = new File(Lecture.DIRECTORY, "Spotify_ID.json");
    public final static File ACCESS_TOKEN = new File(Lecture.DIRECTORY, "Spotify_TOKEN.json");

    private int tokenRefreshInterval = 2;
    public SpotifyApi spotifyApi;
    public AuthorizationCodeUriRequest authCodeUriRequest;
    public Track currentTrack;
    public CurrentlyPlayingContext currentPlayingContext;
    public boolean authenticated;
    private HttpServer callbackServer;

    private final SpotifyCallBack callback = code -> {
        Lecture.text("Spotify: " + "Connecting to Spotify...");
//        AuthorizationCodePKCERequest authCodePKCERequest = spotifyApi.authorizationCodePKCE(code, CODE_VERIFIER).build();
//        try {
//            final AuthorizationCodeCredentials authCredentials = authCodePKCERequest.execute();
//            spotifyApi.setAccessToken(authCredentials.getAccessToken());
//            spotifyApi.setRefreshToken(authCredentials.getRefreshToken());
//            tokenRefreshInterval = authCredentials.getExpiresIn();
//            authenticated = true;
//            scheduleTokenRefresh();
//            scheduleCurrentlyPlayingUpdate();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    };

    private void scheduleTokenRefresh() {
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    TimeUnit.SECONDS.sleep(tokenRefreshInterval - 2);
                    System.out.println("Refreshing token...");
                    final AuthorizationCodeCredentials refreshRequest = spotifyApi.authorizationCodePKCERefresh().build().execute();
                    spotifyApi.setAccessToken(refreshRequest.getAccessToken());
                    spotifyApi.setRefreshToken(refreshRequest.getRefreshToken());
                    tokenRefreshInterval = refreshRequest.getExpiresIn();
                    saveTokens();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void scheduleCurrentlyPlayingUpdate() {
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                    final CurrentlyPlayingContext currentlyPlayingContext = spotifyApi.getInformationAboutUsersCurrentPlayback().build().execute();
                    if (currentlyPlayingContext == null) return;
                    final String currentTrackId = currentlyPlayingContext.getItem().getId();
                    this.currentTrack = spotifyApi.getTrack(currentTrackId).build().execute();
                    this.currentPlayingContext = currentlyPlayingContext;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void startConnection() {
        if (!authenticated) {
            try {
                // Open the authorization window on the default browser
                Desktop.getDesktop().browse(authCodeUriRequest.execute());
                Lecture.INSTANCE.getExecutorService().submit(() -> {
                    try {
                        if (callbackServer != null) {
                            // Close the server if the module was disabled and re-enabled to prevent already bound exception
                            callbackServer.stop(0);
                        }
                        Lecture.text("Spotify: " + "Please allow access to the application.");
                        callbackServer = HttpServer.create(new InetSocketAddress(4030), 0);
                        callbackServer.createContext("/", context -> {
                            callback.codeCallback(context.getRequestURI().getQuery().split("=")[1]);
                            final String messageSuccess = context.getRequestURI().getQuery().contains("code")
                                    ? "Successfully authorized.\nYou can now close this window!"
                                    : "Unable to authorize client, re-toggle the module.";
                            context.sendResponseHeaders(200, messageSuccess.length());
                            OutputStream out = context.getResponseBody();
                            out.write(messageSuccess.getBytes());
                            out.close();
                            callbackServer.stop(0);
                        });
                        callbackServer.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void build(String clientID) {
        spotifyApi = new SpotifyApi.Builder().setClientId(clientID)
                .setRedirectUri(SpotifyHttpManager.makeUri("http://localhost:4030"))
                .build();

        authCodeUriRequest = spotifyApi.authorizationCodePKCEUri(CODE_CHALLENGE)
                .code_challenge_method("S256")
                .scope("user-read-playback-state user-read-playback-position user-modify-playback-state user-read-currently-playing")
                .build();

        loadTokens();
    }

    private void loadTokens() {
        try {
            if (Files.exists(ACCESS_TOKEN.toPath())) {
                String storedTokens = new String(Files.readAllBytes(ACCESS_TOKEN.toPath()));
                AuthorizationCodeCredentials credentials = GSON.fromJson(storedTokens, AuthorizationCodeCredentials.class);
                if (credentials != null) {
                    spotifyApi.setAccessToken(credentials.getAccessToken());
                    spotifyApi.setRefreshToken(credentials.getRefreshToken());
                    tokenRefreshInterval = credentials.getExpiresIn();
                    authenticated = true;
                    scheduleTokenRefresh();
                    scheduleCurrentlyPlayingUpdate();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void saveTokens() {
        AuthorizationCodeCredentials credentials = new AuthorizationCodeCredentials.Builder()
                .setAccessToken(spotifyApi.getAccessToken())
                .setRefreshToken(spotifyApi.getRefreshToken())
                .setExpiresIn(tokenRefreshInterval)
                .build();

        try {
            String jsonTokens = GSON.toJson(credentials);
            byte[] bytes = jsonTokens.getBytes();
            Files.write(ACCESS_TOKEN.toPath(), bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void skipToPreviousTrack() {
        try {
            spotifyApi.skipUsersPlaybackToPreviousTrack().build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            Lecture.text("Spotify: " + e.getMessage());
        }
    }

    public void skipTrack() {
        try {
            spotifyApi.skipUsersPlaybackToNextTrack().build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            Lecture.text("Spotify: " + e.getMessage());
        }
    }

    public void toggleShuffleState() {
        try {
            spotifyApi.toggleShuffleForUsersPlayback(!currentPlayingContext.getShuffle_state()).build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            Lecture.text("Spotify: " + e.getMessage());
        }
    }

    public void pausePlayback() {
        try {
            spotifyApi.pauseUsersPlayback().build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            Lecture.text("Spotify: " + e.getMessage());
        }
    }

    public void resumePlayback() {
        try {
            spotifyApi.startResumeUsersPlayback().build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            Lecture.text("Spotify: " + e.getMessage());
        }
    }

    public void changeVolume(int volume) {
        try {
            spotifyApi.setVolumeForUsersPlayback(volume).build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            Lecture.text("Spotify: " + e.getMessage());
        }
    }

    public boolean isPlaying() {
        return currentPlayingContext.getIs_playing();
    }
}