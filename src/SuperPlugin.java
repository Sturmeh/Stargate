import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * SuperPlugin.java - Plug-in template for hey0's minecraft mod.
 * @author Shaun (sturmeh)
 */
public abstract class SuperPlugin extends Plugin {
    public final ReloadListener reloader = new ReloadListener();
    protected PropertiesFile config;
    protected static final Logger log = Logger.getLogger("Minecraft");
    protected String name;
    protected float version = 1.0f;

    /**
     * This must be called to setup the plug-in!
     * @param name - The name for the config/logfile.
     * @deprecated see SuperPlugin(String name, float version)
     */
    @Deprecated
    public SuperPlugin(String name) {
        config = new PropertiesFile(name+".txt");
        this.name = name;
    }

    /**
     * This must be called to setup the plug-in!
     * @param name - The name for the config/logfile.
     * @param version - The version of this plugin
     */
    public SuperPlugin(String name, float version) {
        config = new PropertiesFile(name+".txt");
        this.name = name;
        this.version = version;
    }
    
    /**
     * This must be called to setup the plug-in!
     * @param name - The name for the config/logfile.
     * @param version - The version of this plugin
     * @param prop - The path and name of the properties file w/o ext.
     */
    public SuperPlugin(String name, float version, String prop) {
        if (prop != null && !prop.isEmpty())
            config = new PropertiesFile(prop+".properties");
        else
            config = null;
        this.name = name;
        this.version = version;
    }

    /**
     * This is called when the plug-in is enabled.
     */
    public void enableExtra() {}

    /**
     * This is called when the plug-in is disabled.
     */
    public void disableExtra() {}

    /**
     * Called after including a reload check for the plug-in.
     * @param player - Player issuing the command.
     * @param split - Array containing the command bits.
     * @return True if the command is to be captured here.
     */
    public boolean extraCommand(Player player, String[] split) { return false; }

    /**
     * This is called when a reload is issued.
     */
    public void reloadConfig() {}
    
    /**
     * This is called when the plug-in is initialised.
     */
    public void initializeExtra() {}

    public void enable() {
        reloadConfig();
        enableExtra();
        log.info(String.format("%s %.2f was enabled", name, version));
    }

    public void disable() {
        disableExtra();
        log.info(String.format("%s %.2f was disabled", name, version));
    }
    
    public void initialize() {
        if (config != null)
            etc.getLoader().addListener(PluginLoader.Hook.COMMAND, reloader, this, PluginListener.Priority.LOW);
        initializeExtra();
    }

    /**
     * Logs a message for debugging or for general information
     * @param String - Message to record
     */
    public static void log(String message) {
        log(Level.INFO, message);
    }

    /**
     * Logs a message for debugging or for general information
     * @param level - The level of this message
     * @param String - Message to record
     */
    public static void log(Level level, String message) {
        log.log(level, message);
    }

    private class ReloadListener extends PluginListener {
        public boolean onCommand(Player player, String[] split) {
            if (player.canUseCommand("/reload") && split[0].equalsIgnoreCase("/reload")) {
                try {
                    config.load();
                } catch (IOException e) {
                    log.warning("Failed to load "+name+".txt: " + e.getMessage());
                }
                reloadConfig();
            }
            return extraCommand(player, split);
        }
    }
}