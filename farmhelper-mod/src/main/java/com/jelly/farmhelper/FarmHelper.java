package com.jelly.farmhelper;

import com.jelly.farmhelper.config.ConfigHandler;
import com.jelly.farmhelper.config.interfaces.FailsafeConfig;
import com.jelly.farmhelper.features.*;
import com.jelly.farmhelper.gui.MenuGUI;
import com.jelly.farmhelper.gui.Render;
import com.jelly.farmhelper.macros.MacroHandler;
import com.jelly.farmhelper.network.APIHelper;
import com.jelly.farmhelper.remote.RemoteControlHandler;
import com.jelly.farmhelper.utils.KeyBindUtils;
import com.jelly.farmhelper.utils.TickTask;
import com.jelly.farmhelper.utils.Utils;
import com.jelly.farmhelper.world.GameState;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.Display;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.Manifest;


@Mod(modid = FarmHelper.MODID, name = FarmHelper.NAME, version = FarmHelper.VERSION)
public class FarmHelper {
    public static final String MODID = "farmhelper";
    public static final String NAME = "Farm Helper";
    public static final String VERSION = "4.3.4.2";
    public static String analyticUrl;

    // the actual mod version from gradle properties, should match with VERSION
    public static String MODVERSION = "-1";
    public static String BOTVERSION = "-1";
    public static int tickCount = 0;
    public static boolean openedGUI = false;
    public static TickTask ticktask;
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static GameState gameState;



    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        setVersions();
        ConfigHandler.init();
        KeyBindUtils.setup();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new Render());
        MinecraftForge.EVENT_BUS.register(new MenuGUI());
        MinecraftForge.EVENT_BUS.register(new MacroHandler());
        MinecraftForge.EVENT_BUS.register(new Failsafe());
        MinecraftForge.EVENT_BUS.register(new Antistuck());
        MinecraftForge.EVENT_BUS.register(new Autosell());
        MinecraftForge.EVENT_BUS.register(new Scheduler());
        MinecraftForge.EVENT_BUS.register(new AutoReconnect());
        MinecraftForge.EVENT_BUS.register(new AutoCookie());
        MinecraftForge.EVENT_BUS.register(new AutoPot());
        MinecraftForge.EVENT_BUS.register(new BanwaveChecker());
        MinecraftForge.EVENT_BUS.register(new RemoteControlHandler());
        MinecraftForge.EVENT_BUS.register(new ProfitCalculator());
        MinecraftForge.EVENT_BUS.register(new Utils());
        gameState = new GameState();
        ProfitCalculator.fetchBazaarPrices();
        try {
            analyticUrl = (String) APIHelper.readJsonFromUrl("NONE","User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36")
                    .get("url");
        } catch (Exception ignored) {}

        if (FailsafeConfig.notifications) {
            registerInitNotification();
        }
    }


    public static void registerInitNotification() {
        new Thread(() -> {
            TrayIcon trayIcon = new TrayIcon(new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR), "Farm Helper Failsafe Notification");
            trayIcon.setToolTip("Farm Helper Failsafe Notification");
            try {
                SystemTray.getSystemTray().add(trayIcon);
            } catch (AWTException e) {
                throw new RuntimeException(e);
            }
            trayIcon.displayMessage("Farm Helper Failsafe Notification", "Register Notifications", TrayIcon.MessageType.INFO);
            SystemTray.getSystemTray().remove(trayIcon);
        }).start();
    }

    @SubscribeEvent
    public void OnKeyPress(InputEvent.KeyInputEvent event) {
        if (KeyBindUtils.customKeyBinds[0].isPressed()) {
            openedGUI = true;
            mc.displayGuiScreen(new MenuGUI());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public final void tick(TickEvent.ClientTickEvent event) throws IOException {
        if (ticktask != null ) {
            ticktask.onTick();
        }
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer != null && mc.theWorld != null)
            gameState.update();
        tickCount += 1;
        tickCount %= 20;
    }


    @SneakyThrows
    public static void setVersions() {
        Class<FarmHelper> clazz = FarmHelper.class;
        String className = clazz.getSimpleName() + ".class";
        String classPath = Objects.requireNonNull(clazz.getResource(className)).toString();
        if (!classPath.startsWith("jar")) return;

        String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) +
                "/META-INF/MANIFEST.MF";
        Manifest manifest = new Manifest(new URL(manifestPath).openStream());
        Attributes attr = manifest.getMainAttributes();
        MODVERSION = attr.getValue("modversion");
        BOTVERSION = attr.getValue("botversion");
        Display.setTitle(FarmHelper.NAME + " " + MODVERSION + " | Bing Chilling");
    }


}
