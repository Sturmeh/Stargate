import java.util.HashMap;

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
	public static final int SIGN = 68;
	private static final HashMap<String, Portal> signRegistry = new HashMap<String, Portal>();
	private static final HashMap<String, Portal> lookupNames = new HashMap<String, Portal>();
	private static final HashMap<String, Portal> lookupEntrances = new HashMap<String, Portal>();

	private Blox topLeft;
	private int modX;
	private int modZ;
	private SignPost id;
	private String name;
	
	private Portal () {
	}
	
	private Portal (Blox topLeft, int modX, int modZ, SignPost id) {
		this.topLeft = topLeft;
		this.modX = modX;
		this.modZ = modZ;
		this.id = id;
		
		this.setName(id.getText(0));
		
		this.register();
	}

	public boolean isOpen() {
		return getBlockAt(-1, -3).getType() == PORTAL;
	}

	public void open() {
		if (!isOpen())
			getBlockAt(-1, -3).setType(FIRE);
	}

	public void close() {
		getBlockAt(-1, -3).setType(AIR);
	}
	
	public void setName(String name) {
		this.name = name.toLowerCase().trim();
		
		drawSign();
	}
	
	public String getName() {
		return this.name;
	}
	
	public void drawSign() {
		id.setText(0, "--" + name + "--");
		id.setText(1, "");
		id.setText(2, "");
		id.setText(3, "");
	}
	
	public Blox[] getEntrances() {
		Blox[] entrances = {
			getBlockAt(-1, -3),
			getBlockAt(-2, -3)
		};
		
		return entrances;
	}
	
	@Override
	public int hashCode() {
		return getName().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		
		Portal portal = (Portal) obj;
		return this.getName() == portal.getName(); 
	}
	
	public void unregister() {
		Blox[] entrances = getEntrances();
		lookupNames.remove(getName());
		signRegistry.remove(new Blox(id.getBlock()).toString());
		
		for (Blox entrance : entrances) {
			lookupEntrances.remove(entrance.toString());
		}
		
		close();
	}
	
	private Blox getBlockAt(int left, int depth) {
		return topLeft.makeRelative(left * modX, depth, left * modZ);
	}
	
	private void register() {
		Blox[] entrances = getEntrances();
		lookupNames.put(getName(), this);
		signRegistry.put(new Blox(id.getBlock()).toString(), this);
		
		for (Blox entrance : entrances) {
			lookupEntrances.put(entrance.toString(), this);
		}
	}

	public static Portal createPortal(SignPost id) {
		Block idParent = id.getParent();

		if (idParent.getType() != OBSIDIAN) return null;

		Blox parent = new Blox(idParent.getX(), idParent.getY(), idParent.getZ());
		Blox topleft = null;

		int modX = 0;
		int modZ = 0;

		if (idParent.getX() > id.getBlock().getX()) {
			modZ -= 1;
		} else if (idParent.getX() < id.getBlock().getX()) {
			modZ += 1;
		} else if (idParent.getZ() > id.getBlock().getZ()) {
			modX += 1;
		} else if (idParent.getZ() < id.getBlock().getZ()) {
			modX -= 1;
		}

		int modN = 1; // Negative modifier for sign offset.
		Blox entry = null;
		
		while (Math.abs(modN) == 1) { // For 1, -1
			entry = parent.makeRelative(modX*modN, -1, modZ*modN);
			if (entry.getType() != AIR || entry.makeRelative(0, -1, 0).getType() != OBSIDIAN)
				modN -= 2;
			else 
				break;
		}
		
		if (Math.abs(modN) != 1) return null;
			
		entry.setType(FIRE);
		boolean isPortal = (entry.getType() == PORTAL);
		
		entry.setType(AIR);
		if (!isPortal) return null;
		
		if (modN > 0)
			topleft = entry.makeRelative(modX * 2, 3, modZ * 2);
		else
			topleft = entry.makeRelative(modX, 3, modZ);

		Portal portal = new Portal(topleft, modX, modZ, id);

		return portal;
	}
	
	public static Portal getByName(String name) {
		return lookupNames.get(name);
	}
	
	public static Portal getByEntrance(Location location) {
		return getByEntrance(new Blox(location));
	}
	
	public static Portal getByEntrance(Block block) {
		return getByEntrance(new Blox(block));
	}
	
	public static Portal getByEntrance(Blox block) {
		return lookupEntrances.get(block.toString());
	}
	
	public static Portal getBySign(Location location) {
		return getBySign(new Blox(location));
	}
	
	public static Portal getBySign(Block block) {
		return getBySign(new Blox(block));
	}
	
	public static Portal getBySign(Blox block) {
		return signRegistry.get(block.toString());
	}
}