package net.minecraft.client.gui;

import best.lettuce.Lettuce;
import best.lettuce.gui.altmanager.GuiAltManager;
import best.lettuce.gui.altmanager.helpers.AltManagerUtils;
import best.lettuce.gui.notification.Notification;
import best.lettuce.gui.notification.NotificationManager;
import best.lettuce.gui.notification.NotificationType;
import best.lettuce.modules.impl.render.HUD;
import best.lettuce.utils.MinecraftInstance;
import best.lettuce.utils.animation.Animation;
import best.lettuce.utils.animation.Direction;
import best.lettuce.utils.animation.impl.DecelerateAnimation;
import best.lettuce.utils.color.ColorUtils;
import best.lettuce.utils.fonts.FontUtils;
import best.lettuce.utils.misc.MouseUtils;
import best.lettuce.utils.non_utils.text.TextField;
import best.lettuce.utils.render.RenderUtils;
import best.lettuce.utils.render.RoundedUtils;
import best.lettuce.utils.text.RandomStringGenerator;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.awt.*;
import java.io.IOException;
import java.util.List;

import de.florianmichael.viamcp.ViaMCP;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.LanServerDetector;
import net.minecraft.client.network.OldServerPinger;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import static best.lettuce.utils.MinecraftInstance.*;

public class GuiMultiplayer extends GuiScreen implements GuiYesNoCallback
{
    private static final Logger logger = LogManager.getLogger();
    private final OldServerPinger oldServerPinger = new OldServerPinger();
    private GuiScreen parentScreen;
    private ServerSelectionList serverListSelector;
    private ServerList savedServerList;
    private GuiButton btnEditServer;
    private GuiButton btnSelectServer;
    private GuiButton btnDeleteServer;
    private boolean deletingServer;
    private boolean addingServer;
    private boolean editingServer;
    private boolean directConnect;
    private String hoveringText;
    private ServerData selectedServer;
    private LanServerDetector.LanServerList lanServerList;
    private LanServerDetector.ThreadLanServerFind lanServerDetector;
    private boolean initialized;

    public final Animation a = new DecelerateAnimation(650, 1);
    public final Animation a2 = new DecelerateAnimation(300, 1);
    public final Animation a3 = new DecelerateAnimation(300, 1);
    public final Animation a4 = new DecelerateAnimation(300, 1);
    public boolean hoverAlt;
    public boolean hovermicro;
    public boolean hovergen;
    public boolean hoveraddalt;
    RandomStringGenerator generator = new RandomStringGenerator.Builder().withinRange('a', 'z').build();

    public final TextField textbox = new TextField(lettuceFont14);

    public GuiMultiplayer(GuiScreen parentScreen)
    {
        this.parentScreen = parentScreen;
    }

    public void initGui()
    {
        textbox.setText("");
        textbox.setVisible(true);
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();

        if (!this.initialized)
        {
            this.initialized = true;
            this.savedServerList = new ServerList(this.mc);
            this.savedServerList.loadServerList();
            this.lanServerList = new LanServerDetector.LanServerList();

            try
            {
                this.lanServerDetector = new LanServerDetector.ThreadLanServerFind(this.lanServerList);
                this.lanServerDetector.start();
            }
            catch (Exception exception)
            {
                logger.warn("Unable to start LAN server detection: " + exception.getMessage());
            }

            this.serverListSelector = new ServerSelectionList(this, this.mc, this.width, this.height, 32, this.height - 64, 36);
            this.serverListSelector.func_148195_a(this.savedServerList);
        }
        else
        {
            this.serverListSelector.setDimensions(this.width, this.height, 32, this.height - 64);
        }

        this.createButtons();
    }

    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.serverListSelector.handleMouseInput();
    }

    public void createButtons()
    {
        this.buttonList.add(this.btnEditServer = new GuiButton(7, this.width / 2 - 154, this.height - 28, 70, 20, I18n.format("selectServer.edit", new Object[0])));
        this.buttonList.add(this.btnDeleteServer = new GuiButton(2, this.width / 2 - 74, this.height - 28, 70, 20, I18n.format("selectServer.delete", new Object[0])));
        this.buttonList.add(this.btnSelectServer = new GuiButton(1, this.width / 2 - 154, this.height - 52, 100, 20, I18n.format("selectServer.select", new Object[0])));
        this.buttonList.add(new GuiButton(4, this.width / 2 - 50, this.height - 52, 100, 20, I18n.format("selectServer.direct", new Object[0])));
        this.buttonList.add(new GuiButton(3, this.width / 2 + 4 + 50, this.height - 52, 100, 20, I18n.format("selectServer.add", new Object[0])));
        this.buttonList.add(new GuiButton(8, this.width / 2 + 4, this.height - 28, 70, 20, I18n.format("selectServer.refresh", new Object[0])));
        this.buttonList.add(new GuiButton(0, this.width / 2 + 4 + 76, this.height - 28, 75, 20, I18n.format("gui.cancel", new Object[0])));
        this.buttonList.add(ViaMCP.INSTANCE.getAsyncVersionSlider());
        this.selectServer(this.serverListSelector.func_148193_k());
    }

    public void updateScreen()
    {
        super.updateScreen();

        if (this.lanServerList.getWasUpdated())
        {
            List<LanServerDetector.LanServer> list = this.lanServerList.getLanServers();
            this.lanServerList.setWasNotUpdated();
            this.serverListSelector.func_148194_a(list);
        }

        this.oldServerPinger.pingPendingNetworks();
    }

    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);

        if (this.lanServerDetector != null)
        {
            this.lanServerDetector.interrupt();
            this.lanServerDetector = null;
        }

        this.oldServerPinger.clearPendingNetworks();
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            GuiListExtended.IGuiListEntry guilistextended$iguilistentry = this.serverListSelector.func_148193_k() < 0 ? null : this.serverListSelector.getListEntry(this.serverListSelector.func_148193_k());

            if (button.id == 2 && guilistextended$iguilistentry instanceof ServerListEntryNormal)
            {
                String s4 = ((ServerListEntryNormal)guilistextended$iguilistentry).getServerData().serverName;

                if (s4 != null)
                {
                    this.deletingServer = true;
                    String s = I18n.format("selectServer.deleteQuestion", new Object[0]);
                    String s1 = "\'" + s4 + "\' " + I18n.format("selectServer.deleteWarning", new Object[0]);
                    String s2 = I18n.format("selectServer.deleteButton", new Object[0]);
                    String s3 = I18n.format("gui.cancel", new Object[0]);
                    GuiYesNo guiyesno = new GuiYesNo(this, s, s1, s2, s3, this.serverListSelector.func_148193_k());
                    this.mc.displayGuiScreen(guiyesno);
                }
            }
            else if (button.id == 1)
            {
                this.connectToSelected();
            }
            else if (button.id == 4)
            {
                this.directConnect = true;
                this.mc.displayGuiScreen(new GuiScreenServerList(this, this.selectedServer = new ServerData(I18n.format("selectServer.defaultName", new Object[0]), "", false)));
            }
            else if (button.id == 3)
            {
                this.addingServer = true;
                this.mc.displayGuiScreen(new GuiScreenAddServer(this, this.selectedServer = new ServerData(I18n.format("selectServer.defaultName", new Object[0]), "", false)));
            }
            else if (button.id == 7 && guilistextended$iguilistentry instanceof ServerListEntryNormal)
            {
                this.editingServer = true;
                ServerData serverdata = ((ServerListEntryNormal)guilistextended$iguilistentry).getServerData();
                this.selectedServer = new ServerData(serverdata.serverName, serverdata.serverIP, false);
                this.selectedServer.copyFrom(serverdata);
                this.mc.displayGuiScreen(new GuiScreenAddServer(this, this.selectedServer));
            }
            else if (button.id == 0)
            {
                this.mc.displayGuiScreen(this.parentScreen);
            }
            else if (button.id == 8)
            {
                this.refreshServerList();
            }
        }
    }

    private void refreshServerList()
    {
        this.mc.displayGuiScreen(new GuiMultiplayer(this.parentScreen));
    }

    public void confirmClicked(boolean result, int id)
    {
        GuiListExtended.IGuiListEntry guilistextended$iguilistentry = this.serverListSelector.func_148193_k() < 0 ? null : this.serverListSelector.getListEntry(this.serverListSelector.func_148193_k());

        if (this.deletingServer)
        {
            this.deletingServer = false;

            if (result && guilistextended$iguilistentry instanceof ServerListEntryNormal)
            {
                this.savedServerList.removeServerData(this.serverListSelector.func_148193_k());
                this.savedServerList.saveServerList();
                this.serverListSelector.setSelectedSlotIndex(-1);
                this.serverListSelector.func_148195_a(this.savedServerList);
            }

            this.mc.displayGuiScreen(this);
        }
        else if (this.directConnect)
        {
            this.directConnect = false;

            if (result)
            {
                this.connectToServer(this.selectedServer);
            }
            else
            {
                this.mc.displayGuiScreen(this);
            }
        }
        else if (this.addingServer)
        {
            this.addingServer = false;

            if (result)
            {
                this.savedServerList.addServerData(this.selectedServer);
                this.savedServerList.saveServerList();
                this.serverListSelector.setSelectedSlotIndex(-1);
                this.serverListSelector.func_148195_a(this.savedServerList);
            }

            this.mc.displayGuiScreen(this);
        }
        else if (this.editingServer)
        {
            this.editingServer = false;

            if (result && guilistextended$iguilistentry instanceof ServerListEntryNormal)
            {
                ServerData serverdata = ((ServerListEntryNormal)guilistextended$iguilistentry).getServerData();
                serverdata.serverName = this.selectedServer.serverName;
                serverdata.serverIP = this.selectedServer.serverIP;
                serverdata.copyFrom(this.selectedServer);
                this.savedServerList.saveServerList();
                this.serverListSelector.func_148195_a(this.savedServerList);
            }

            this.mc.displayGuiScreen(this);
        }
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if(hoverAlt){
            textbox.keyTyped(typedChar, keyCode);
            if(Keyboard.isKeyDown(13) && textbox.isEmpty()){
                AltManagerUtils.getInstance().loginWithString(textbox.getText(), "", false);
                NotificationManager.post(new Notification(NotificationType.SUCCESS, "", "Successfully logged as " + textbox.getText(), 3));
                textbox.setText("");
            }
        }
        int i = this.serverListSelector.func_148193_k();
        GuiListExtended.IGuiListEntry guilistextended$iguilistentry = i < 0 ? null : this.serverListSelector.getListEntry(i);

        if (keyCode == 63)
        {
            this.refreshServerList();
        }
        else
        {
            if (i >= 0)
            {
                if (keyCode == 200)
                {
                    if (isShiftKeyDown())
                    {
                        if (i > 0 && guilistextended$iguilistentry instanceof ServerListEntryNormal)
                        {
                            this.savedServerList.swapServers(i, i - 1);
                            this.selectServer(this.serverListSelector.func_148193_k() - 1);
                            this.serverListSelector.scrollBy(-this.serverListSelector.getSlotHeight());
                            this.serverListSelector.func_148195_a(this.savedServerList);
                        }
                    }
                    else if (i > 0)
                    {
                        this.selectServer(this.serverListSelector.func_148193_k() - 1);
                        this.serverListSelector.scrollBy(-this.serverListSelector.getSlotHeight());

                        if (this.serverListSelector.getListEntry(this.serverListSelector.func_148193_k()) instanceof ServerListEntryLanScan)
                        {
                            if (this.serverListSelector.func_148193_k() > 0)
                            {
                                this.selectServer(this.serverListSelector.getSize() - 1);
                                this.serverListSelector.scrollBy(-this.serverListSelector.getSlotHeight());
                            }
                            else
                            {
                                this.selectServer(-1);
                            }
                        }
                    }
                    else
                    {
                        this.selectServer(-1);
                    }
                }
                else if (keyCode == 208)
                {
                    if (isShiftKeyDown())
                    {
                        if (i < this.savedServerList.countServers() - 1)
                        {
                            this.savedServerList.swapServers(i, i + 1);
                            this.selectServer(i + 1);
                            this.serverListSelector.scrollBy(this.serverListSelector.getSlotHeight());
                            this.serverListSelector.func_148195_a(this.savedServerList);
                        }
                    }
                    else if (i < this.serverListSelector.getSize())
                    {
                        this.selectServer(this.serverListSelector.func_148193_k() + 1);
                        this.serverListSelector.scrollBy(this.serverListSelector.getSlotHeight());

                        if (this.serverListSelector.getListEntry(this.serverListSelector.func_148193_k()) instanceof ServerListEntryLanScan)
                        {
                            if (this.serverListSelector.func_148193_k() < this.serverListSelector.getSize() - 1)
                            {
                                this.selectServer(this.serverListSelector.getSize() + 1);
                                this.serverListSelector.scrollBy(this.serverListSelector.getSlotHeight());
                            }
                            else
                            {
                                this.selectServer(-1);
                            }
                        }
                    }
                    else
                    {
                        this.selectServer(-1);
                    }
                }
                else if (keyCode != 28 && keyCode != 156)
                {
                    super.keyTyped(typedChar, keyCode);
                }
                else
                {
                    this.actionPerformed((GuiButton)this.buttonList.get(2));
                }
            }
            else
            {
                super.keyTyped(typedChar, keyCode);
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        //, this.width - 60f, 158, 50, 15
        textbox.setXPosition(this.width - 114f);
        textbox.setYPosition(142);
        textbox.setWidth(85f);
        textbox.setHeight(12);
        textbox.setFill(new Color(0,0,0, 50));
        textbox.setBackgroundText(EnumChatFormatting.BOLD + "Username...");
        textbox.setOutline(new Color(0,0,0, 50));
        if(!hoverAlt){
            textbox.setFocused(false);
        }
        a.setDirection(hoverAlt ? Direction.FORWARDS : Direction.BACKWARDS);
        a2.setDirection(hovermicro ? Direction.FORWARDS : Direction.BACKWARDS);
        a3.setDirection(hovergen ? Direction.FORWARDS : Direction.BACKWARDS);
        a4.setDirection(hoveraddalt ? Direction.FORWARDS : Direction.BACKWARDS);
        hoverAlt = MouseUtils.isHovering(this.width - 27 - a.getOutput().floatValue() * 90, 7, 20 + a.getOutput().floatValue() * 90, 20 + a.getOutput().floatValue() * 150, mouseX, mouseY);
        hovermicro = hoverAlt && MouseUtils.isHovering(this.width - 114, 158, 50, 15, mouseX, mouseY);
        hovergen = hoverAlt && MouseUtils.isHovering(this.width - 60f, 158, 50, 15, mouseX, mouseY);
        hoveraddalt = hoverAlt && MouseUtils.isHovering(this.width - 25f, 140, 15, 15, mouseX, mouseY);
        this.hoveringText = null;
        this.drawDefaultBackground();
        this.serverListSelector.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRendererObj, I18n.format("multiplayer.title", new Object[0]), this.width / 2, 20, 16777215);
        RenderUtils.scissorStart(this.width - 27.5f - a.getOutput().floatValue() * 90, 5, 22 + a.getOutput().floatValue() * 90, 25 + a.getOutput().floatValue() * 150);
        RoundedUtils.drawRound(this.width - 27 - a.getOutput().floatValue() * 90, 7, 20 + a.getOutput().floatValue() * 90, 20 + a.getOutput().floatValue() * 150, 10, ColorUtils.interpolateColorC(new Color(59, 44, 65, 255), new Color(59, 44, 65, 255), a.getOutput().floatValue()));
        MinecraftInstance.icontestFont40.drawCenteredString(FontUtils.ALTS, this.width - 17.25f - a.getOutput().floatValue() * 90, MinecraftInstance.icontestFont40.getMiddleOfBox(20) + 8, Color.WHITE);
        if(hoverAlt || !hoverAlt && !a.finished(Direction.BACKWARDS)) {
            lettuceBoldFont20.drawString("Alts Manager", this.width - 92f, lettuceBoldFont20.getMiddleOfBox(20) + 8, Color.WHITE);
        }
        RoundedUtils.drawRound(this.width - 114, 158, 50, 15, 3, ColorUtils.interpolateColorC(new Color(0,0,0, 50), new Color(255, 255, 255, 64), a2.getOutput().floatValue()));
        RenderUtils.drawMicrosoftLogo(this.width - 111.5f, 161f, 10, 0.5f);
        lettuceBoldFont14.drawString("Microsoft", this.width - 100, lettuceBoldFont14.getMiddleOfBox(15) + 159, ColorUtils.interpolateColorC(Color.WHITE, Color.WHITE.darker(), a2.getOutput().floatValue()));
        RoundedUtils.drawRound(this.width - 60f, 158, 50, 15, 3, ColorUtils.interpolateColorC(new Color(0,0,0, 50), new Color(255, 255, 255, 64), a3.getOutput().floatValue()));
        lettuceBoldFont14.drawCenteredString("Random Alt", this.width - 35.5f, 159 + lettuceBoldFont14.getMiddleOfBox(15), ColorUtils.interpolateColorC(Color.WHITE, Color.WHITE.darker(), a3.getOutput().floatValue()));
        RoundedUtils.drawRound(this.width - 25f, 140, 15, 15, 3, ColorUtils.interpolateColorC(new Color(0,0,0, 50), new Color(255, 255, 255, 64), a4.getOutput().floatValue()));
        icontestFont26.drawString(FontUtils.ADD_ALT, this.width - 23.5f, 142f + icontestFont26.getMiddleOfBox(12), ColorUtils.interpolateColorC(Color.WHITE, Color.WHITE.darker(), a4.getOutput().floatValue()));
        textbox.drawTextBox();
        RenderUtils.scissorEnd();
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (this.hoveringText != null)
        {
            this.drawHoveringText(Lists.newArrayList(Splitter.on("\n").split(this.hoveringText)), mouseX, mouseY);
        }
        Lettuce.INSTANCE.getModuleManager().getModule(HUD.class).drawNotifications();
    }

    public void connectToSelected()
    {
        GuiListExtended.IGuiListEntry guilistextended$iguilistentry = this.serverListSelector.func_148193_k() < 0 ? null : this.serverListSelector.getListEntry(this.serverListSelector.func_148193_k());

        if (guilistextended$iguilistentry instanceof ServerListEntryNormal)
        {
            this.connectToServer(((ServerListEntryNormal)guilistextended$iguilistentry).getServerData());
        }
        else if (guilistextended$iguilistentry instanceof ServerListEntryLanDetected)
        {
            LanServerDetector.LanServer lanserverdetector$lanserver = ((ServerListEntryLanDetected)guilistextended$iguilistentry).getLanServer();
            this.connectToServer(new ServerData(lanserverdetector$lanserver.getServerMotd(), lanserverdetector$lanserver.getServerIpPort(), true));
        }
    }

    private void connectToServer(ServerData server)
    {
        this.mc.displayGuiScreen(new GuiConnecting(this, this.mc, server));
    }

    public void selectServer(int index)
    {
        this.serverListSelector.setSelectedSlotIndex(index);
        GuiListExtended.IGuiListEntry guilistextended$iguilistentry = index < 0 ? null : this.serverListSelector.getListEntry(index);
        this.btnSelectServer.enabled = false;
        this.btnEditServer.enabled = false;
        this.btnDeleteServer.enabled = false;

        if (guilistextended$iguilistentry != null && !(guilistextended$iguilistentry instanceof ServerListEntryLanScan))
        {
            this.btnSelectServer.enabled = true;

            if (guilistextended$iguilistentry instanceof ServerListEntryNormal)
            {
                this.btnEditServer.enabled = true;
                this.btnDeleteServer.enabled = true;
            }
        }
    }

    public OldServerPinger getOldServerPinger()
    {
        return this.oldServerPinger;
    }

    public void setHoveringText(String p_146793_1_)
    {
        this.hoveringText = p_146793_1_;
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {   if(hoverAlt) {
        textbox.mouseClicked(mouseX, mouseY, mouseButton);
    }
    if(hoveraddalt && mouseButton == 0){
        if(!textbox.isEmpty()) {
            AltManagerUtils.getInstance().loginWithString(textbox.getText(), "", false);
            NotificationManager.post(new Notification(NotificationType.SUCCESS, "", "Successfully logged as " + textbox.getText(), 3));
            textbox.setText("");
        }
        else {
            NotificationManager.post(new Notification(NotificationType.WARNING, "", "Username must contains at least a letter", 3));
        }
    }
    if(hovermicro && mouseButton == 0){
        AltManagerUtils.getInstance().microsoftLoginAsync("", "");
    }
    if(hovergen && mouseButton == 0){
        String name = generator.generate(8);
        AltManagerUtils.getInstance().loginWithString(name, "", false);
        NotificationManager.post(new Notification(NotificationType.SUCCESS, "", "Successfully logged as " + name, 3));
    }
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if(!hoverAlt) {
            this.serverListSelector.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        this.serverListSelector.mouseReleased(mouseX, mouseY, state);
    }

    public ServerList getServerList()
    {
        return this.savedServerList;
    }

    public boolean func_175392_a(ServerListEntryNormal p_175392_1_, int p_175392_2_)
    {
        return p_175392_2_ > 0;
    }

    public boolean func_175394_b(ServerListEntryNormal p_175394_1_, int p_175394_2_)
    {
        return p_175394_2_ < this.savedServerList.countServers() - 1;
    }

    public void func_175391_a(ServerListEntryNormal p_175391_1_, int p_175391_2_, boolean p_175391_3_)
    {
        int i = p_175391_3_ ? 0 : p_175391_2_ - 1;
        this.savedServerList.swapServers(p_175391_2_, i);

        if (this.serverListSelector.func_148193_k() == p_175391_2_)
        {
            this.selectServer(i);
        }

        this.serverListSelector.func_148195_a(this.savedServerList);
    }

    public void func_175393_b(ServerListEntryNormal p_175393_1_, int p_175393_2_, boolean p_175393_3_)
    {
        int i = p_175393_3_ ? this.savedServerList.countServers() - 1 : p_175393_2_ + 1;
        this.savedServerList.swapServers(p_175393_2_, i);

        if (this.serverListSelector.func_148193_k() == p_175393_2_)
        {
            this.selectServer(i);
        }

        this.serverListSelector.func_148195_a(this.savedServerList);
    }
}
