import java.util.ArrayList;
import java.util.Random;


public class Stargate extends SuperPlugin {
	public final Listener listener = new Listener();
	Random generator = new Random(etc.getServer().getTime());
	public Stargate() { super("stargate"); }
	private ArrayList<Portal> gates = new ArrayList<Portal>();
	public final int OBSIDIAN = 49;
	public final int FIRE = 51;
	public final int AIR = 0;

	//private Portal lasta;
	//private Portal lastb;

	public void initializeExtra() {
		etc.getLoader().addListener(PluginLoader.Hook.PLAYER_MOVE, listener, this, PluginListener.Priority.MEDIUM);
	}
	/*
	public boolean extraCommand(Player player, String[] split) {
		if ((player.canUseCommand("/close")) && 
				(split[0].equalsIgnoreCase("/close")) &&
				(split.length == 2)) {
			if (split[1].equals("a") && lasta != null) {
				lasta.close();
				return true;
			} else if (split[1].equals("b") && lastb != null) {
				lastb.close();
				return true;
			}
		} else if ((player.canUseCommand("/open")) && 
				(split[0].equalsIgnoreCase("/open")) &&
				(split.length == 2)) {
			if (split[1].equals("a") && lasta != null) {
				lasta.open();
				return true;
			} else if (split[1].equals("b") && lastb != null) {
				lastb.open();
				return true;
			}
		}
		return false;
	}
	 */

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

	public class Portal {
		public Location base;
		private Location coBase;
		private Location exBase;

		public Portal (int x, int y, int z) {
			base = new Location(x, y, z);
			if (etc.getServer().getBlockIdAt(x+1, y, z) == 90) {
				coBase = new Location(x+1, y, z);
				exBase = new Location(x-1, y, z);
				base.rotX = 180;
			} else if (etc.getServer().getBlockIdAt(x-1, y, z) == 90) {
				coBase = new Location(x-1, y, z);
				exBase = new Location(x+1, y, z);
				base.rotX = 180;
			} else if (etc.getServer().getBlockIdAt(x, y, z+1) == 90) {
				coBase = new Location(x, y, z+1);
				exBase = new Location(x, y, z-1);
				base.rotX = 90;
			} else if (etc.getServer().getBlockIdAt(x, y, z-1) == 90) {
				coBase = new Location(x, y, z-1);
				exBase = new Location(x, y, z+1);
				base.rotX = 90;
			}
		}

		public boolean same(Portal twin) {
			if (compareLoc(base, twin.base))
				return true;
			return compareLoc(coBase, twin.base);
		}

		public void send(Portal from, Player player) {
			if (isOpen() && from.isOpen()) {
				from.close();
				player.teleportTo(new Location((base.x + coBase.x)/2, base.y, (base.z + coBase.z)/2, base.rotX, 0));
				close();
			}
		}

		public boolean isOpen() {
			return (etc.getServer().getBlockIdAt((int)base.x, (int)base.y, (int)base.z) == 90);
		}

		public void open() {
			if (!isOpen())
				etc.getServer().setBlockAt(FIRE, (int)base.x, (int)base.y, (int)base.z); // Fire
		}

		public void close() {
			if (isOpen()) {
				etc.getServer().setBlockAt(AIR, (int)exBase.x, (int)exBase.y, (int)exBase.z); // Air
				etc.getServer().setBlockAt(OBSIDIAN, (int)exBase.x, (int)exBase.y, (int)exBase.z); // Obsidian
			}
		}

		private boolean compareLoc(Location a, Location b) {
			return (a.x == b.x && a.y == b.y && a.z == b.z);
		}
	}
}
