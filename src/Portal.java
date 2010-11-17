import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;

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
	private Blox[] frame;
	private boolean verified;
	private boolean fixed;
	private ArrayList<String> destinations = new ArrayList<String>();
	private String network;
	
	private Portal (Blox topLeft, int modX, int modZ,
			float rotX, SignPost id, Blox button,
			String dest, String name, Blox[] frame, 
			boolean verified, String network) {
		this.topLeft = topLeft;
		this.modX = modX;
		this.modZ = modZ;
		this.rotX = rotX;
		this.id = id;
		this.destination = dest;
		this.button = button;
		this.verified = verified;
		this.fixed = dest.length() > 0;
		this.network = network;
		this.frame = frame;
		this.name = name;

		this.register();
		if (verified) this.drawSign(true);
	}

	public boolean isOpen() {
		return getBlockAt(1, -3).getType() == PORTAL;
	}

	public boolean open() {
		return open(null);
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
		deactivate();
	}
	
	public boolean isOpenFor(Player player) {
		if ((isFixed()) || (this.player == null)) return true;
		return (player != null) && (player.getName() == this.player.getName());
	}
	
	public boolean isFixed() {
		return fixed;
	}
	
	public Location getExit() {
		return getLocAt(1.5, -3.0, 1.0);
	}
	
	public void setName(String name) {
		this.name = filterName(name);
		
		drawSign(true);
	}
	
	public String getName() {
		return name;
	}
	
	public void setDestination(Portal destination) {
		setDestination(destination.getName());
	}
	
	public void setDestination(String destination) {
		this.destination = destination;
	}
	
	public Portal getDestination() {
		return Portal.getByName(destination);
	}
	
	public String getDestinationName() {
		return destination;
	}
	
	public boolean isVerified() {
		verified = verified || getBlockAt(1, 0).getType() == OBSIDIAN;
		return verified;
	}
	
	public boolean wasVerified() {
		return verified;
	}
	
	public boolean checkIntegrity() {
		boolean result = true;
		Blox[] frame = getFrame();
		
		for (int i = 0; i < frame.length && result; i++) {
			result = result && frame[i].getType() == OBSIDIAN;
		}
		
		return result;
	}
	
	public void activate() {
		destinations.clear();
		
		for (String name : allPortals) {
			Portal portal = getByName(name);
			if ((portal.getNetwork().equals(network)) && (!name.equals(getName()))) {
				destinations.add(name);
			}
		}
	}
	
	public void deactivate() {
		destinations.clear();
		destination = "";
		drawSign(true);
	}
	
	public boolean isActive() {
		return destinations.size() > 0;
	}
	
	public String getNetwork() {
		return network;
	}
	
	public void cycleDestination() {
		if (!isActive()) activate();
		
		if (destinations.size() > 0) {
			int index = destinations.indexOf(destination);
			if (++index >= destinations.size()) index = 0;
			destination = destinations.get(index);
		}
		
		drawSign(true);
	}
	
	public void drawSign(boolean update) {
		id.setText(0, "--" + name + "--");
		int max = destinations.size() - 1;
		int done = 0;
		
		if (!isActive()) {
			id.setText(++done, "Right click to");
			id.setText(++done, "use the gate");
			id.setText(++done, " (" + network + ") ");
		} else {
			if (isFixed()) {
				id.setText(++done, "To: " + destination);
			} else {
				int index = destinations.indexOf(destination);
				
				if ((index == max) && (max > 1) && (++done <= 3)) id.setText(done, destinations.get(index - 2));
				if ((index > 0) && (++done <= 3)) id.setText(done, destinations.get(index - 1));
				if (++done <= 3) id.setText(done, " >" + destination + "< ");
				if ((max >= index + 1) && (++done <= 3)) id.setText(done, destinations.get(index + 1));
				if ((max >= index + 2) && (++done <= 3)) id.setText(done, destinations.get(index + 2));
			}
		}
		
		for (done++; done <= 3; done++) {
			id.setText(done, "");
		}
		
		if (update) id.update();
	}
	
	public Blox[] getEntrances() {
		Blox[] entrances = {
			getBlockAt(1, -3),
			getBlockAt(2, -3)
		};
		
		return entrances;
	}
	
	public Blox[] getFrame() {
		if (frame == null) {
			frame = new Blox[] {
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
		}
		
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
		if (button != null) lookupBlocks.remove(button.toString());
		
		for (Blox entrance : getEntrances())
			lookupEntrances.remove(entrance.toString());
		
		allPortals.remove(getName());
		close();
		
		if (id.getBlock().getType() == SIGN) {
			id.setText(0, getName());
			id.setText(1, "");
			id.setText(2, "");
			id.setText(3, "");
			id.update();
		}
		
		for (String originName : allPortals) {
			Portal origin = Portal.getByName(originName);

			if ((origin != null) && (origin.isFixed()) && (origin.getDestinationName().equals(getName())) && (origin.isVerified())) {
				origin.close();
			}
		}
		
		saveAllGates();
	}
	
	private Blox getBlockAt(int right, int depth) {
		return topLeft.makeRelative(-right * modX, depth, -right * modZ);
	}
	
	private Location getLocAt(double right, double depth, double distance) {
		return topLeft.makeRelativeLoc(0.5 + -right * modX + distance * modZ, depth, 0.5 + -right * modZ + -distance * modX, rotX, 0); 
	}
	
	private void register() {
		lookupNames.put(getName(), this);
		
		for (Blox frame : getFrame())
			lookupBlocks.put(frame.toString(), this);
		// Include the sign and button
		lookupBlocks.put(new Blox(id.getBlock()).toString(), this);
		if (button != null) lookupBlocks.put(button.toString(), this);
		
		for (Blox entrance : getEntrances())
			lookupEntrances.put(entrance.toString(), this);
		
		allPortals.add(getName());
	}

	@Override
	public String toString() {
		return String.format("Portal [id=%s, name=%s]", id, name);
	}

	public static Portal createPortal(SignPost id) {
		Block idParent = id.getParent();

		if (idParent.getType() != OBSIDIAN) return null;

		Blox parent = new Blox(idParent.getX(), idParent.getY(), idParent.getZ());
		Blox topleft = null;
		String name = filterName(id.getText(0));
		String destName = filterName(id.getText(1));
		String network = filterName(id.getText(2));
		
		if ((name.length() < 1) || (name.length() > 11) || (getByName(name) != null)) return null;
		if ((network.length() < 1) || (network.length() > 11)) network = Stargate.getDefaultNetwork();
		if (destName.length() > 0) network = "";

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
		
		Portal portal = null;
		
		if (destName.length() > 0) {
			portal = new Portal(topleft, modX, modZ, rotX, id, null, destName, name, null, true, network);
			
			Portal destination = getByName(destName);
			if (destination != null) portal.open();
		} else {
			Blox button = parent.makeRelative(modX * modN * 3 + modZ, 0, modZ * modN * 3 + -modX);
			button.setType(BUTTON);
	
			portal = new Portal(topleft, modX, modZ, rotX, id, button, "", name, null, true, network);
		}
		
		for (String originName : allPortals) {
			Portal origin = Portal.getByName(originName);
			
			if ((origin != null) && (origin.isFixed()) && (origin.getDestinationName().equals(portal.getName())) && (origin.isVerified())) {
				origin.open();
			}
		}
		
		saveAllGates();
		
		for (String portalName: allPortals) {
			Portal gate = getByName(portalName);
			if (gate.isVerified()) gate.drawSign(true);
		}
		
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
	
	public static void saveAllGates() {
		String loc = Stargate.getSaveLocation();
        
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(loc, false));
            
            for (String name : allPortals) {
            	Portal portal = Portal.getByName(name);
                StringBuilder builder = new StringBuilder();
            	Blox sign = new Blox(portal.id.getBlock());
            	Blox button = portal.button;
            	Blox[] frame = portal.getFrame();
            	int frameCount = 0;

	            builder.append(portal.name);
	            builder.append(':');
	            builder.append(sign.toString());
	            builder.append(':');
	            builder.append((button != null) ? button.toString() : "");
	            builder.append(':');
	            builder.append(portal.modX);
	            builder.append(':');
	            builder.append(portal.modZ);
	            builder.append(':');
	            builder.append(portal.rotX);
	            builder.append(':');
	            builder.append(portal.topLeft.toString());
	            builder.append(':');
	            
	            for (Blox block : frame) {
	            	if (frameCount++ > 0) builder.append(";");
	            	builder.append(block.toString());
	            }

	            builder.append(':');
	            builder.append(portal.isFixed() ? portal.getDestinationName() : "");
	            builder.append(':');
	            builder.append(portal.getNetwork());
	            
	            bw.append(builder.toString());
	            bw.newLine();
            }
            
            bw.close();
        } catch (Exception e) {
            Stargate.log(Level.SEVERE, "Exception while writing stargates to " + loc + ": " + e);
        }
	}
	
	public static void loadAllGates() {
		String location = Stargate.getSaveLocation();
		
		lookupBlocks.clear();
		lookupNames.clear();
		lookupEntrances.clear();
		allPortals.clear();
		
        if (new File(location).exists()) {
            try {
                Scanner scanner = new Scanner(new File(location));
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    if (line.startsWith("#") || line.equals("")) {
                        continue;
                    }
                    String[] split = line.split(":");
                    if (split.length < 3) {
                        continue;
                    }
                    String name = split[0];
                    SignPost sign = new SignPost(new Blox(split[1]));
                    Blox button = (split[2].length() > 0) ? new Blox(split[2]) : null;
                    int modX = Integer.parseInt(split[3]);
                    int modZ = Integer.parseInt(split[4]);
                    ArrayList<Blox> frame = new ArrayList<Blox>();
                    float rotX = Float.parseFloat(split[5]);
                    Blox topLeft = new Blox(split[6]);
                    String[] frameSplit = split[7].split(";");
                    
                    for (String pos : frameSplit) {
                    	frame.add(new Blox(pos));
                    }
                    
                    Blox[] frameBlox = new Blox[0];
                    frameBlox = frame.toArray(frameBlox);

                    String fixed = (split.length > 8) ? split[8] : "";
                    String network = (split.length > 9) ? split[9] : Stargate.getDefaultNetwork();
                    
                    if (fixed.length() > 0) network = "";
                    
                    Portal portal = new Portal(topLeft, modX, modZ, rotX, sign, button, fixed, name, frameBlox, false, network);
                    
                    if (fixed.length() > 0) {
                    	if (portal.isVerified()) {
                    		Portal destination = getByName(fixed);
                    		
                    		if (destination != null) portal.open();
                    	}
                    }
        			
        			for (String originName : allPortals) {
        				Portal origin = Portal.getByName(originName);

        				if ((origin != null) && (origin.isFixed()) && (origin.getDestinationName().equals(portal.getName())) && (origin.isVerified())) {
        					origin.open();
        				}
        			}
                }
                scanner.close();
            } catch (Exception e) {
                Stargate.log(Level.SEVERE, "Exception while reading stargates from " + location + ": " + e);
            }
        }
	}
	
	public static String filterName(String input) {
		return input.replaceAll("[^\\w\\s]", "").toLowerCase().trim();
	}
}