import java.util.logging.Logger;

/**
 * SuperPlugin.java - Plug-in template for hey0's minecraft mod.
 * @author Shaun (sturmeh)
 */
public abstract class SuperPlugin extends Plugin {
	public final ReloadListener reloader = new ReloadListener();
	protected PropertiesFile config;
	protected final Logger log = Logger.getLogger("Minecraft");
	protected String name;

	/**
	 * This must be called to setup the plug-in!
	 * @param name - The name for the config/logfile.
	 */
	public SuperPlugin(String name) {
		config = new PropertiesFile(name+".txt");
		this.name = name;
		reloadConfig();
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
	 * This is called when a reload is issued, read config here.
	 */
	public void reloadExtra() {}

	/**
	 * Called after including a reload check for the plug-in.
	 * @param player - Player issuing the command.
	 * @param split - Array containing the command bits.
	 * @return True if the command is to be captured here.
	 */
	public boolean extraCommand(Player player, String[] split) { return false; }
	
	public void initializeExtra() {}

	public void initialize() {
			etc.getLoader().addListener(PluginLoader.Hook.COMMAND, reloader, this, PluginListener.Priority.LOW);
			initializeExtra();
	}
	
	public void enable() {
		enableExtra();
		log.info(name+" was enabled.");
	}

	public void disable() {
		disableExtra();
		log.info(name+" was disabled.");
	}

	private void reloadConfig() {
		config.load();
		reloadExtra();
	}

	/**
	 * Sends a message to all players!
	 * @param String - Message to send to all players.
	 */
	public void broadcast(String message) {
		for (Player p : etc.getServer().getPlayerList()) {
			p.sendMessage(message);
		}
	}
	
	/**
	 * Determines if a player used a command AND can use it.
	 * @param String - The command being checked.
	 * @param String - What the player is trying to use.
	 * @param Player - The player attempting to use the command.
	 */
	public boolean isApt(String apt, String input, Player heir) {
		return (heir.canUseCommand(apt) && input.equalsIgnoreCase(apt));
	}
	
	private class ReloadListener extends PluginListener {
		public boolean onCommand(Player player, String[] split) {
			if (isApt("/reload", split[0], player)) {
				reloadConfig();
			}
			return extraCommand(player, split);
		}
	}
}
