/**
 * Portal.java - Plug-in for hey0's minecraft mod.
 * @author Shaun (sturmeh)
 * @author Dinnerbone
 */
public class Portal {
	public static final int OBSIDIAN = 49;
	public static final int FIRE = 51;
	public static final int AIR = 0;
	public static final int PORTAL = 90;
	
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
	
	public static Portal createPortal(SignPost id) {
		Block idParent = id.getParent();
		PortalFacing facing = PortalFacing.SOUTH;
		int entranceX, entranceZ;
		int entranceY = idParent.getY() - 2;
		
		if (idParent.getType() != OBSIDIAN) return null;
		
		entranceX = idParent.getX();
		entranceZ = idParent.getZ() - 1;
		if (etc.getServer().getBlockIdAt(entranceX, entranceY, entranceZ) == AIR) {
			etc.getServer().setBlockAt(FIRE, entranceX, entranceY, entranceZ);
			
			if (etc.getServer().getBlockIdAt(entranceX, entranceY, entranceZ) == PORTAL) {
				etc.getServer().setBlockAt(AIR, entranceX, entranceY, entranceZ);
				Stargate.broadcast("Omg a portal!");
			}
		}
		
		return null;
	}
	
	public enum PortalFacing {
		NORTH,
		EAST,
		SOUTH,
		WEST
	}
}