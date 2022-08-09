package com.dhyun.portal;

import com.dhyun.portal.command.VoicechatCommands;
import com.dhyun.portal.config.ServerConfig;
import com.dhyun.portal.intercompatibility.CommonCompatibilityManager;
import com.dhyun.portal.voice.server.ServerVoiceEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

public abstract class Teleport {

    public static final String MODID = "teleportui";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static ServerVoiceEvents SERVER;
    public static ServerConfig SERVER_CONFIG;

    public static int COMPATIBILITY_VERSION;

    static {
        try {
            COMPATIBILITY_VERSION = readVersion();
        } catch (Exception e) {
            LOGGER.fatal("Failed to read compatibility version: {}", e.getMessage());
            COMPATIBILITY_VERSION = -1;
        }
    }

    public static final Pattern GROUP_REGEX = Pattern.compile("^[^\"\\n\\r\\t\\s][^\"\\n\\r\\t]{0,15}$");

    public void initialize() {
        LOGGER.info("Compatibility version {}", COMPATIBILITY_VERSION);
        CommonCompatibilityManager.INSTANCE.getNetManager().init();
        SERVER = new ServerVoiceEvents();
        CommonCompatibilityManager.INSTANCE.onRegisterServerCommands(VoicechatCommands::register);
    }

    public static void logDebug(String message, Object... objects) {
        if (debugMode()) {
            LOGGER.info(message, objects);
        }
    }

    public static boolean debugMode() {
        return CommonCompatibilityManager.INSTANCE.isDevEnvironment() || System.getProperty("voicechat.debug") != null;
    }

    private static int readVersion() throws IOException {
        Enumeration<URL> resources = Teleport.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
        while (resources.hasMoreElements()) {
            Manifest manifest = new Manifest(resources.nextElement().openStream());
            String version = manifest.getMainAttributes().getValue("Compatibility-Version");
            if (version != null) {
                return Integer.parseInt(version);
            }
        }
        // Use the environment variable in development
        String env = System.getenv("COMPATIBILITY_VERSION");
        if (env != null) {
            return Integer.parseInt(env);
        }
        throw new IOException("Could not read MANIFEST.MF");
    }

}
