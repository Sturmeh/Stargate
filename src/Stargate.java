import java.util.ArrayList;
import java.util.Random;
/**
 * Stargate.java - Plug-in for hey0's minecraft mod.
 * @author Shaun (sturmeh)
 * @author Dinnerbone
 */
public class Stargate extends SuperPlugin {
	public final Listener listener = new Listener();
	Random generator = new Random(etc.getServer().getTime());
	private ArrayList<Portal> gates = new ArrayList<Portal>();
	
	public Stargate() { super("stargate"); }

	public void initializeExtra() {
		etc.getLoader().addListener(PluginLoader.Hook.PLAYER_MOVE, listener, this, PluginListener.Priority.MEDIUM);
	}

	private Portal getExisting(Player explorer, Portal candidate) {
		if (!gates.isEmpty()) {
			for (Portal gate : gates) {
				if (gate.same(candidate))
					return gate;
			}
		}
		gates.add(candidate);
		explorer.sendMessage(Colors.DarkPurple+"You uncover a portal...");
		return candidate;
	}
	
	private Portal getRandom(Portal from) {
		ArrayList<Portal> tmp = new ArrayList<Portal>();
		for (Portal gate : gates) {
			if (!gate.same(from) && gate.isOpen())
				tmp.add(gate);
		}
		if (tmp.isEmpty())
			return null;
		return tmp.get(generator.nextInt(tmp.size()));
	}

	private class Listener extends PluginListener {
		public void onPlayerMove(Player player, Location from, Location to) {	
			if (etc.getServer().getBlockIdAt((int)to.x, (int)to.y, (int)to.z) == 90) {
				Portal gate = getExisting(player, new Portal((int)to.x, (int)to.y, (int)to.z));
				Portal dest = getRandom(gate);
				if (dest != null) {
					dest.send(gate, player);
				}
			}
		}
	}
}
