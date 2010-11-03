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
	private static final HashMap<String, Portal> lookupNames = new HashMap<String, Portal>();
	private static final HashMap<Blox, Portal> lookupEntrances = new HashMap<Blox, Portal>();

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
	}

	public boolean isOpen() {
		Blox base = getBlockAt(1, -3, 0);
		return base.getType() == PORTAL;
	}

	public void open() {
		if (!isOpen())
		{
			Blox base = getBlockAt(1, -3, 0);
			base.setType(FIRE);
		}
	}

	public void close() {
		Blox base = getBlockAt(1, -3, 0);
		base.setType(AIR);
	}
	
	public void setName(String name) {
		this.name = name.toLowerCase().trim();
		
		id.setText(0, "--" + name + "--");
		id.setText(1, "");
		id.setText(2, "");
		id.setText(3, "");
	}
	
	public String getName() {
		return this.name;
	}
	
	public Blox[] getEntrances() {
		Blox[] entrances = {
			getBlockAt(1, -3, 0),
			getBlockAt(2, -3, 0)
		};
		
		return entrances;
	}
	
	private Blox getBlockAt(int x, int y, int z) {
		return topLeft.makeRelative(x * modX, y, z * modZ);
	}

	public static Portal createPortal(SignPost id) {
		Block idParent = id.getParent();

		if (idParent.getType() != OBSIDIAN) 
			return null;

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
}