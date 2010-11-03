import java.util.ArrayList;

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
	private static final ArrayList<Portal> portalNames = new ArrayList<Portal>();

	public Location base;
	private Location coBase;
	private Location exBase;
	private Blox topLeft;
	private int modX;
	private int modZ;
	private SignPost id;
	
	private Portal () {
	}
	
	private Portal (Blox topLeft, int modX, int modZ, SignPost id) {
		this.topLeft = topLeft;
		this.modX = modX;
		this.modZ = modZ;
		this.id = id;
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

		if (idParent.getType() != OBSIDIAN) 
			return null;

		//PortalFacing facing = null;
		Blox parent = new Blox(idParent.getX(), idParent.getY(), idParent.getZ());
		Blox topleft = null;

		int modX = 0;
		int modZ = 0;

		if (idParent.getX() > id.getBlock().getX()) {
			//facing = PortalFacing.NORTH;
			modZ -= 1;
		} else if (idParent.getX() < id.getBlock().getX()) {
			//facing = PortalFacing.SOUTH;
			modZ += 1;
		} else if (idParent.getZ() > id.getBlock().getZ()) {
			//facing = PortalFacing.EAST;
			modX += 1;
		} else if (idParent.getZ() < id.getBlock().getZ()) {
			//facing = PortalFacing.WEST;
			modX -= 1;
		}

		Blox entry = parent.makeRelative(modX, -1, modZ);

		if (entry.getType() == AIR && entry.makeRelative(0, -1, 0).getType() == OBSIDIAN) {
			entry.setType(FIRE);

			if (entry.getType() == PORTAL) {
				entry.setType(AIR);
				topleft = entry.makeRelative(modX * 2, 3, modZ * 2);
			} else {
				return null;
			}
		} else {
			entry = parent.makeRelative(-modX, -1, -modZ);
			Blox relative = entry.makeRelative(0, -1, 0);

			if (entry.getType() == AIR && relative.getType() == OBSIDIAN) {
				entry.setType(FIRE);

				if (entry.getType() == PORTAL) {
					entry.setType(AIR);
					topleft = entry.makeRelative(modX, 3, modZ);
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
		
		Portal portal = new Portal(topleft, modX, modZ, id);

		return portal;
	}
}