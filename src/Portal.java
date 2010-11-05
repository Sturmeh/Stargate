import java.util.ArrayList;
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
	public static final int BUTTON = 77;
	private static final HashMap<String, Portal> lookupBlocks = new HashMap<String, Portal>();
	private static final HashMap<String, Portal> lookupNames = new HashMap<String, Portal>();
	private static final HashMap<String, Portal> lookupEntrances = new HashMap<String, Portal>();
	private static final ArrayList<String> allPortals = new ArrayList<String>();

	private Blox topLeft;
	private int modX;
	private int modZ;
	private float rotX;
	private SignPost id;
	private String name;
	private String destination;
	private Blox button;
	private Player player;
	
	private Portal (Blox topLeft, int modX, int modZ, float rotX, SignPost id, Blox button) {
		this.topLeft = topLeft;
		this.modX = modX;
		this.modZ = modZ;
		this.rotX = rotX;
		this.id = id;
		this.destination = "";
		this.button = button;

		this.setName(id.getText(0));
		this.register();
		cycleDestination();
	}

	public boolean isOpen() {
		return getBlockAt(1, -3).getType() == PORTAL;
	}

	public boolean open(Player openFor) {
		if (isOpen()) return false;
		
		getBlockAt(1, -3).setType(FIRE);
		player = openFor;
		
		return true;
	}

	public void close() {
		getBlockAt(1, -3).setType(AIR);
		player = null;
	}
	
	public Player isOpenFor() {
		return player;
	}
	
	public Location getExit() {
		Blox exit = getBlockAt(1, -3).makeRelative(modZ, 0, modX);
		exit.setType(20);
		return new Location(exit.getX(), exit.getY(), exit.getZ(), rotX, 0);
	}
	
	public void setName(String name) {
		this.name = name.toLowerCase().trim();
		
		drawSign();
	}
	
	public String getName() {
		return name;
	}
	
	public Portal getDestination() {
		return Portal.getByName(destination);
	}
	
	public String getDestinationName() {
		return destination;
	}
	
	public void cycleDestination() {
		int index = allPortals.indexOf(destination);
		if (++index >= allPortals.size()) index = 0;
		destination = allPortals.get(index);
		
		drawSign();
	}
	
	public void drawSign() {
		id.setText(0, "--" + name + "--");
		int max = allPortals.size() - 1;
		int done = 0;

		if (max > 0) {
			int index = allPortals.indexOf(destination);
			
			if ((index == max) && (max > 1) && (++done <= 3)) id.setText(done, allPortals.get(index - 2));
			if ((index > 0) && (++done <= 3)) id.setText(done, allPortals.get(index - 1));
			if (++done <= 3) id.setText(done, " >" + destination + "< ");
			if ((max >= index + 1) && (++done <= 3)) id.setText(done, allPortals.get(index + 1));
			if ((max >= index + 2) && (++done <= 3)) id.setText(done, allPortals.get(index + 2));
		}
		
		for (done++; done <= 3; done++) {
			id.setText(done, "");
		}
		
		id.update();
	}
	
	public Blox[] getEntrances() {
		Blox[] entrances = {
			getBlockAt(1, -3),
			getBlockAt(2, -3)
		};
		
		return entrances;
	}
	
	public Blox[] getFrame() {
		Blox[] frame = {
			getBlockAt(1, 0),
			getBlockAt(2, 0),
			getBlockAt(0, -1),
			getBlockAt(3, -1),
			getBlockAt(0, -2),
			getBlockAt(3, -2),
			getBlockAt(0, -3),
			getBlockAt(3, -3),
			getBlockAt(1, -4),
			getBlockAt(2, -4)
		};
		
		return frame;
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
		lookupNames.remove(getName());
		
		for (Blox frame : getFrame())
			lookupBlocks.remove(frame.toString());
		// Include the sign and button
		lookupBlocks.remove(new Blox(id.getBlock()).toString());
		lookupBlocks.remove(button.toString());
		
		for (Blox entrance : getEntrances())
			lookupEntrances.remove(entrance.toString());
		
		allPortals.remove(getName());
		close();
		
		id.setText(0, getName());
		id.setText(1, "");
		id.setText(2, "");
		id.setText(3, "");
	}
	
	private Blox getBlockAt(int left, int depth) {
		return topLeft.makeRelative(-left * modX, depth, -left * modZ);
	}
	
	private void register() {
		lookupNames.put(getName(), this);
		
		for (Blox frame : getFrame())
			lookupBlocks.put(frame.toString(), this);
		// Include the sign and button
		lookupBlocks.put(new Blox(id.getBlock()).toString(), this);
		lookupBlocks.put(button.toString(), this);
		
		for (Blox entrance : getEntrances())
			lookupEntrances.put(entrance.toString(), this);
		
		allPortals.add(getName());
	}

	public static Portal createPortal(SignPost id) {
		Block idParent = id.getParent();

		if (idParent.getType() != OBSIDIAN) return null;

		Blox parent = new Blox(idParent.getX(), idParent.getY(), idParent.getZ());
		Blox topleft = null;

		int modX = 0;
		int modZ = 0;
		float rotX = 0f;

		if (idParent.getX() > id.getBlock().getX()) {
			modZ -= 1;
			rotX = 90f;
		} else if (idParent.getX() < id.getBlock().getX()) {
			modZ += 1;
			rotX = 270f;
		} else if (idParent.getZ() > id.getBlock().getZ()) {
			modX += 1;
			rotX = 180f;
		} else if (idParent.getZ() < id.getBlock().getZ()) {
			modX -= 1;
			rotX = 0f;
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
		
		Blox button = parent.makeRelative(modX * modN * 3 + modZ, 0, modZ * modN * 3 + -modX);
		button.setType(BUTTON);

		Portal portal = new Portal(topleft, modX, modZ, rotX, id, button);
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

	public static Portal getByBlock(Block block) {
		return getByBlock(new Blox(block));
	}
	
	public static Portal getByBlock(Blox block) {
		return lookupBlocks.get(block.toString());
	}
}