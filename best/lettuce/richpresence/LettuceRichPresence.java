package best.lettuce.richpresence;

import best.lettuce.utils.MinecraftInstance;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.arikia.dev.drpc.DiscordUser;
import net.arikia.dev.drpc.callbacks.ReadyCallback;

public class LettuceRichPresence {
    public static boolean running = true;
    private static long times = 0;

    public static void startrpc() {
        times = System.currentTimeMillis();

        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().setReadyEventHandler(discordUser -> System.out.println("Starting DiscordRPC")).build();

        DiscordRPC.discordInitialize("1135272147300909186", handlers, true);

        DiscordRichPresence.Builder b = new DiscordRichPresence.Builder("Playing Minecraft");
        //b.setDetails("Made by " + MinecraftInstance.AUTHOR);
        b.setBigImage("clown", "Lecture Client " + MinecraftInstance.VERSION);
        b.setSmallImage("info", "cracked by tsukasa.tokyo");
        //b.setSmallImage("info", "Made by " + Client.author);
        b.setStartTimestamps(times);
        DiscordRPC.discordUpdatePresence(b.build());

        new Thread("RichPresence") {
            @Override
            public void run() {
                while (running) {
                    DiscordRPC.discordRunCallbacks();
                }
            }
        }.start();

    }

    public static void update(String title){
        DiscordRichPresence.Builder b = new DiscordRichPresence.Builder(title);
        //b.setDetails("Made by " + MinecraftInstance.AUTHOR);
        b.setBigImage("clown", "Lecture Client " + MinecraftInstance.VERSION);
        b.setSmallImage("info", "cracked by tsukasa.tokyo");
        b.setStartTimestamps(times);
        DiscordRPC.discordUpdatePresence(b.build());
    }

    public static void shutdownrpc() {
        running = false;
        DiscordRPC.discordShutdown();
    }
}