import java.util.Random;
/**
 * Stargate.java - Plug-in for hey0's minecraft mod.
 * @author Shaun (sturmeh)
 * @author Dinnerbone
 */
public class Stargate extends SuperPlugin {
	public final Listener listener = new Listener();
	Random generator = new Random(etc.getServer().getTime());
	
	public Stargate() { super("stargate"); }

	public void initializeExtra() {
		etc.getLoader().addListener(PluginLoader.Hook.PLAYER_MOVE, listener, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.COMPLEX_BLOCK_CHANGE, listener, this, PluginListener.Priority.MEDIUM);
	}

	private class Listener extends PluginListener {
		public void onPlayerMove(Player player, Location from, Location to) {	
			Portal portal = Portal.getByEntrance(to);
			
			if ((portal != null) && (portal.isOpen())) {
				player.sendMessage(Colors.Green + "Teleport text goes here");
			}
		}
		
		public boolean onComplexBlockChange(Player player, ComplexBlock signBlock) {
			if (!(signBlock instanceof Sign)) return false;
			SignPost sign = new SignPost((Sign)signBlock);
			Portal portal = Portal.createPortal(sign);

			if (portal != null) {
				player.sendMessage(Colors.Green + "You feel a slight tremble in the ground around the portal");
			}
			
			return false;
		}
	}
}
