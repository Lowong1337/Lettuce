package best.lettuce.modules.impl.misc;

import best.lettuce.event.base.EventListener;
import best.lettuce.event.impl.game.EventTick;
import best.lettuce.event.impl.player.EventAttack;
import best.lettuce.event.impl.player.EventMotion;
import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.property.impl.ModeProperty;
import best.lettuce.modules.property.impl.StringProperty;
import best.lettuce.utils.MinecraftInstance;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public class KillSult extends Module {
    public KillSult(){
        super("Kill Sult", Category.MISC, "Automatically sends message when killing a player.");
        this.addProperties(mode, prefix);
    }

    public final ModeProperty mode = new ModeProperty("Mode", "Basic", "Basic", "Order");
    //public final BooleanProperty heromc = new BooleanProperty("HeroMC BW Team", false);
    public final StringProperty prefix = new StringProperty("Prefix", "");
    public EntityLivingBase target;
    int msgnumber = 0;

    public final EventListener<EventAttack> onAttack = e -> {
        target = e.getTarget();
        //Client.text(target.getName());
    };

    public final EventListener<EventTick> onTick = e -> {
        this.setSuffix(mode.getMode());
    };

    public String[] messages = new String[] {
            "Imagine being dumb, %name%.",
            "Sorry %name%, I'm just testing Lettuce client",
            "Lettuce client, made by " + MinecraftInstance.AUTHOR,
            "Lettuce client lets %name% know what skill is.",
            "Your mom is fat, your dad is gay, %name%.",
            "%name% didn't even use his brain to play Minecraft.",
            "*knock *knock. Who's there? Your mom!",
            "Better luck next time, %name%.",
            "Sorry, I forgot that %name% is retarded.",
            "%name% is an FDP client user.",
            "%name% tried to kill me but failed."
    };

    public final EventListener<EventMotion> onMotion = e -> {
        if(target != null){
            if(target instanceof EntityPlayer && target.isDead){
                switch (mode.getMode()){
                    case "Basic" : {
                        mc.thePlayer.sendChatMessage(prefix.getString() + "L " + target.getName());
                    }
                    break;
                    case "Order" : {
                        mc.thePlayer.sendChatMessage(prefix.getString() + messages[msgnumber].replace("%name%", target.getName()));
                    }
                    break;
                }
                target = null;
                msgnumber++;
                if(messages.length - 1 == msgnumber){
                    msgnumber = 0;
                }
            }
        }
    };
}