
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;

/**
 * Portal.java - Plug-in for hey0's minecraft mod.
 * @author Shaun (sturmeh)
 * @author Dinnerbone
 */
public class Portal {
    public static final int OBSIDIAN = 49;
    public static final int FIRE = 51;
    public static final int SIGN = 68;
    public static final int BUTTON = 77;
    private static final HashMap<Block, Portal> lookupBlocks = new HashMap<Block, Portal>();
    private static final HashMap<String, Portal> lookupNames = new HashMap<String, Portal>();
    private static final HashMap<Block, Portal> lookupEntrances = new HashMap<Block, Portal>();
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
    private Blox[] entrances;
    private boolean verified;
    private boolean fixed;
    private boolean gracePeriod;
    private ArrayList<String> destinations = new ArrayList<String>();
    private String network;
    private Gate gate;
    private boolean isOpen = false;
    private String owner = "";
    private HashMap<Block, Integer> exits;
    private HashMap<Integer, Block> reverseExits;

    private Portal(Blox topLeft, int modX, int modZ,
            float rotX, SignPost id, Blox button,
            String dest, String name,
            boolean verified, String network, Gate gate,
            String owner) {
        this.topLeft = topLeft;
        this.modX = modX;
        this.modZ = modZ;
        this.rotX = rotX;
        this.id = id;
        this.destination = dest;
        this.button = button;
        this.verified = verified;
        this.fixed = dest.length() > 0;
        this.gracePeriod = false;
        this.network = network;
        this.name = name;
        this.gate = gate;
        this.owner = owner;

        this.register();
        if (verified) {
            this.drawSign(true);
        }
    }

    public synchronized boolean manipGrace(boolean set, boolean var) {
        if (!set) {
            return gracePeriod;
        }
        gracePeriod = var;
        return false;
    }

    private boolean pastGrace() {
        if (isFixed()) {
            return false; // ignore fixed gates.
        }
        if (manipGrace(false, false)) {
            return manipGrace(true, false);
        }
        if (isActive() || isOpen()) {
            return true;
        }
        return false; // else.
    }

    public boolean isOpen() {
        return isOpen || isFixed();
    }

    public boolean open() {
        return open(null);
    }

    public boolean open(Player openFor) {
        return open(openFor, isFixed());
    }

    public boolean open(Player openFor, boolean force) {
        if (isOpen() && !force) return false;

        etc.getServer().loadChunk(topLeft.getBlock());

        for (Blox inside : getEntrances()) {
            inside.setType(gate.getPortalBlockOpen());
        }

        isOpen = true;
        if (!isFixed()) {
            player = openFor;
            manipGrace(true, true);

            Portal end = getDestination();
            if (end != null && !end.isOpen() && end.getDestination() == null) {
                end.open(openFor, false);
                end.setDestination(this);
                if (end.isVerified()) end.drawSign(true);
            }
        }

        return true;
    }

    public void close(boolean force) {
        if (fixed && !force) return;

        Portal end = getDestination();

        if (end != null && end.isOpen()) {
            end.deactivate(); // Clear it's destination first.
            end.close(false);
        }

        for (Blox inside : getEntrances()) {
            inside.setType(gate.getPortalBlockClosed());
        }

        player = null;
        isOpen = false;

        deactivate();
    }

    public boolean isOpenFor(Player player) {
        if ((isFixed()) || (this.player == null)) {
            return true;
        }
        return (player != null) && (player.getName().equalsIgnoreCase(this.player.getName()));
    }

    public boolean isFixed() {
        return fixed;
    }

    public boolean isPowered() {
        RelativeBlockVector[] controls = gate.getControls();

        for (RelativeBlockVector vector : controls) {
            if (getBlockAt(vector).getBlock().isPowered()) {
                return true;
            }
        }

        return false;
    }

    public void teleport(Player player, Portal origin) {
        Location traveller = player.getLocation();
        Location exit = getExit(traveller, origin);

        exit.rotX = origin.getRotation() - traveller.rotX + this.getRotation() + 180;

        player.teleportTo(exit);
    }

    public Location teleport(BaseVehicle vehicle, Portal origin) {
        Location traveller = new Location(vehicle.getX(), vehicle.getY(), vehicle.getZ());
        Location exit = getExit(traveller, origin);

        double motX = vehicle.getMotionX();
        double motY = vehicle.getMotionY();
        double motZ = vehicle.getMotionZ();

        int xx = origin.modX * this.modX;
        int xz = origin.modX * this.modZ;
        int zx = origin.modZ * this.modX;
        int zz = origin.modZ * this.modZ;

        vehicle.setMotion(0, 0, 0);
        vehicle.teleportTo(exit);

        return new Location((motX * zz + motZ * xz) * 1, motY, (motX * zx + motZ * xx) * -1);
    }

    public Location getExit(Location traveller, Portal origin) {
        Block entrance = etc.getServer().getBlockAt((int)Math.floor(traveller.x), (int)Math.floor(traveller.y), (int)Math.floor(traveller.z));
        HashMap<Block, Integer> originExits = origin.getExits();
        HashMap<Block, Integer> destExits = this.getExits();

        if (originExits.containsKey(entrance)) {
            int position = (int)(((float)originExits.get(entrance) / originExits.size()) * destExits.size());
            Block exit = getReverseExits().get(position);

            if (exit != null) {
                Location loc = new Blox(exit).modRelativeLoc(0D, 0D, 1D, traveller.rotX, traveller.rotY, modX, 1, modZ);
                Block block = etc.getServer().getBlockAt((int)Math.floor(loc.x), (int)Math.floor(loc.y), (int)Math.floor(loc.z));

                if (block.blockType == Block.Type.Step) {
                    loc.y += 0.5;
                }

                loc.rotY = traveller.rotY;
                return loc;
            }
        }

        Stargate.log(Level.WARNING, "No position found calculting route from " + this + " to " + origin);
        Stargate.log(originExits.get(entrance) + " / " + originExits.size() + " * " + destExits.size());
        return traveller;
    }

    public float getRotation() {
        return rotX;
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
        return gate.matches(topLeft, modX, modZ);
    }

    public Gate getGate() {
        return gate;
    }

    public String getOwner() {
        return owner;
    }

    public void activate() {
        destinations.clear();

        for (String dest : allPortals) {
            Portal portal = getByName(dest);
            if ((portal.getNetwork().equalsIgnoreCase(network)) && (!dest.equalsIgnoreCase(getName()))) {
                destinations.add(dest);
            }
        }
    }

    public void deactivate() {
        if (fixed) {
            return;
        }
        destinations.clear();
        destination = "";
        drawSign(true);
    }

    public boolean isActive() {
        return fixed || (destinations.size() > 0);
    }

    public String getNetwork() {
        return network;
    }

    public void cycleDestination() {
        if (!isActive()) {
            activate();
        }

        if (destinations.size() > 0) {
            int index = destinations.indexOf(destination);
            if (++index >= destinations.size()) {
                index = 0;
            }
            destination = destinations.get(index);
        }

        drawSign(true);
    }

    public final void drawSign(boolean update) {
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
                manipGrace(true, true);
                int index = destinations.indexOf(destination);

                if ((index == max) && (max > 1) && (++done <= 3)) {
                    id.setText(done, destinations.get(index - 2));
                }
                if ((index > 0) && (++done <= 3)) {
                    id.setText(done, destinations.get(index - 1));
                }
                if (++done <= 3) {
                    id.setText(done, " >" + destination + "< ");
                }
                if ((max >= index + 1) && (++done <= 3)) {
                    id.setText(done, destinations.get(index + 1));
                }
                if ((max >= index + 2) && (++done <= 3)) {
                    id.setText(done, destinations.get(index + 2));
                }
            }
        }

        for (done++; done <= 3; done++) {
            id.setText(done, "");
        }

        if (update) {
            id.update();
        }
    }

    public Blox[] getEntrances() {
        if (entrances == null) {
            RelativeBlockVector[] space = gate.getEntrances();
            entrances = new Blox[space.length];
            int i = 0;

            for (RelativeBlockVector vector : space) {
                entrances[i++] = getBlockAt(vector);
            }
        }
        return entrances;
    }

    public Blox[] getFrame() {
        if (frame == null) {
            RelativeBlockVector[] border = gate.getBorder();
            RelativeBlockVector[] controls = gate.getControls();
            frame = new Blox[border.length + controls.length];
            int i = 0;

            for (RelativeBlockVector vector : border) {
                frame[i++] = getBlockAt(vector);
            }

            for (RelativeBlockVector vector : controls) {
                frame[i++] = getBlockAt(vector);
            }
        }

        return frame;
    }

    public HashMap<Block, Integer> getExits() {
        if (exits == null) {
            exits = new HashMap<Block, Integer>();
            reverseExits = new HashMap<Integer, Block>();
            HashMap<RelativeBlockVector, Integer> relativeExits = gate.getExits();
            Set<RelativeBlockVector> exitBlocks = relativeExits.keySet();

            for (RelativeBlockVector vector : exitBlocks) {
                Block block = getBlockAt(vector).getBlock();
                Integer position = relativeExits.get(vector);
                exits.put(block, position);
                reverseExits.put(position, block);
            }
        }

        return exits;
    }

    public HashMap<Integer, Block> getReverseExits() {
        if (reverseExits == null) {
            getExits();
        }

        return reverseExits;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        Portal portal = (Portal) obj;
        return this.getName().equalsIgnoreCase(portal.getName());
    }

    public void unregister() {
        lookupNames.remove(getName().toLowerCase());

        for (Blox block : getFrame()) {
            lookupBlocks.remove(block.getBlock());
        }
        // Include the sign and button
        lookupBlocks.remove(id.getBlock());
        if (button != null) {
            lookupBlocks.remove(button.getBlock());
        }

        for (Blox entrance : getEntrances()) {
            lookupEntrances.remove(entrance.getBlock());
        }

        allPortals.remove(getName());
        close(true);

        if (id.getBlock().getType() == SIGN) {
            id.setText(0, getName());
            id.setText(1, "");
            id.setText(2, "");
            id.setText(3, "");
            id.update();
        }

        for (String originName : allPortals) {
            Portal origin = Portal.getByName(originName);

            if ((origin != null) && (origin.isFixed()) && (origin.getDestinationName().equalsIgnoreCase(getName())) && (origin.isVerified())) {
                origin.close(false);
            }
        }

        saveAllGates();
    }

    private Blox getBlockAt(int right, int depth) {
        return getBlockAt(right, depth, 0);
    }

    private Blox getBlockAt(RelativeBlockVector vector) {
        return topLeft.modRelative(vector.getRight(), vector.getDepth(), vector.getDistance(), modX, 1, modZ);
    }

    private Blox getBlockAt(int right, int depth, int distance) {
        return topLeft.modRelative(right, depth, distance, modX, 1, modZ);
    }

    private void register() {
        lookupNames.put(getName().toLowerCase(), this);

        for (Blox block : getFrame()) {
            lookupBlocks.put(block.getBlock(), this);
        }
        // Include the sign and button
        lookupBlocks.put(id.getBlock(), this);
        if (button != null) {
            lookupBlocks.put(button.getBlock(), this);
        }

        for (Blox entrance : getEntrances()) {
            lookupEntrances.put(entrance.getBlock(), this);
        }

        allPortals.add(getName());
    }

    @Override
    public String toString() {
        return String.format("Portal [id=%s, name=%s, type=%s]", id, name, gate.getFilename());
    }

    public static Portal createPortal(SignPost id, Player player) {
        Block idParent = id.getParent();

        Blox parent = new Blox(idParent.getX(), idParent.getY(), idParent.getZ());
        Blox topleft = null;
        String name = filterName(id.getText(0));
        String destName = filterName(id.getText(1));
        String network = filterName(id.getText(2));

        if ((name.length() < 1) || (name.length() > 11) || (getByName(name) != null)) {
            return null;
        }
        if ((network.length() < 1) || (network.length() > 11)) {
            network = Stargate.getDefaultNetwork();
        }
        if (destName.length() > 0) {
            network = "";
        }

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

        Gate[] possibleGates = Gate.getGatesByControlBlock(idParent);
        Gate gate = null;
        RelativeBlockVector buttonVector = null;

        for (Gate possibility : possibleGates) {
            if ((gate == null) && (buttonVector == null)) {
                RelativeBlockVector[] vectors = possibility.getControls();
                RelativeBlockVector otherControl = null;

                for (RelativeBlockVector vector : vectors) {
                    Blox tl = parent.modRelative(-vector.getRight(), -vector.getDepth(), -vector.getDistance(), modX, 1, modZ);

                    if (gate == null) {
                        if (possibility.matches(tl, modX, modZ)) {
                            gate = possibility;
                            topleft = tl;

                            if (otherControl != null) {
                                buttonVector = otherControl;
                            }
                        }
                    } else if (otherControl != null) {
                        buttonVector = vector;
                    }

                    otherControl = vector;
                }
            }
        }

        if ((gate == null) || (buttonVector == null)) {
            return null;
        }

        if (!gate.deductCost(Gate.CostFor.Creating, player)) {
            if (!Stargate.getCantAffordToNew().isEmpty()) {
                player.sendMessage(Colors.Red + Stargate.getCantAffordToNew());
            }
            return null;
        }

        Portal portal = null;

        if (destName.length() > 0) {
            portal = new Portal(topleft, modX, modZ, rotX, id, null, destName, name, true, network, gate, player.getName());

            Portal destination = getByName(destName);
            if (destination != null) {
                portal.open();
            }
        } else {
            Blox button = topleft.modRelative(buttonVector.getRight(), buttonVector.getDepth(), buttonVector.getDistance() + 1, modX, 1, modZ);
            button.setType(BUTTON);
            
            // start Hidendra
            /* dirty hacks */
            
            Block.Face[] faces = new Block.Face[] {
            	Block.Face.Front, Block.Face.Left, Block.Face.Right, Block.Face.Back
            };
            
            // we want THE OBSIDIAN
            for(Block.Face face : faces) {
            	Block blockFace = button.getBlock().getFace(face);
            	
            	if(blockFace.getType() != OBSIDIAN) {
            		continue;
            	}
            	
            	switch(face) {
            	case Front: // SOUTH - 0x01
            		button.setData(0x02); // inverse the button dir (south->N)
            		
            		break;
            		
            	case Left: // EAST - 0x03
            		button.setData(0x04);
            		
            		break;
            		
            	case Right: // WEST - 0x04
            		button.setData(0x03);
            		
            		break;
            		
            	case Back: // NORTH - 0x02
            		button.setData(0x01);
            		
            		break;
            	
            	}
            }
            
            // end

            portal = new Portal(topleft, modX, modZ, rotX, id, button, "", name, true, network, gate, player.getName());
        }

        for (String originName : allPortals) {
            Portal origin = Portal.getByName(originName);

            if ((origin != null) && (origin.isFixed()) && (origin.getDestinationName().equalsIgnoreCase(portal.getName())) && (origin.isVerified())) {
                origin.open();
            }
        }

        saveAllGates();

        return portal;
    }

    public static Portal getByName(String name) {
        return lookupNames.get(name.toLowerCase());
    }

    public static Portal getByEntrance(Location location) {
        return getByEntrance(new Blox(location).getBlock());
    }

    public static Portal getByEntrance(Block block) {
        return lookupEntrances.get(block);
    }

    public static Portal getByBlock(Block block) {
        return lookupBlocks.get(block);
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
                builder.append(portal.gate.getFilename());
                builder.append(':');
                builder.append(portal.isFixed() ? portal.getDestinationName() : "");
                builder.append(':');
                builder.append(portal.getNetwork());
                builder.append(':');
                builder.append(portal.getOwner());

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
                    if (line.startsWith("#") || line.isEmpty()) {
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
                    float rotX = Float.parseFloat(split[5]);
                    Blox topLeft = new Blox(split[6]);
                    Gate gate = (split[7].contains(";")) ? Gate.getGateByName("nethergate.gate") : Gate.getGateByName(split[7]);

                    String fixed = (split.length > 8) ? split[8] : "";
                    String network = (split.length > 9) ? split[9] : Stargate.getDefaultNetwork();
                    String owner = (split.length > 10) ? split[10] : "";

                    if (fixed.length() > 0) {
                        network = "";
                    }

                    Portal portal = new Portal(topLeft, modX, modZ, rotX, sign, button, fixed, name, false, network, gate, owner);

                    if (fixed.length() > 0) {
                        if (portal.isVerified()) {
                            Portal destination = getByName(fixed);

                            if (destination != null) {
                                portal.open();
                            }
                        }
                    }

                    for (String originName : allPortals) {
                        Portal origin = Portal.getByName(originName);

                        if ((origin != null) && (origin.isFixed()) && (origin.getDestinationName().equalsIgnoreCase(portal.getName())) && (origin.isVerified())) {
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
        return input.replaceAll("[\\|:#]", "").trim();
    }

    public static Portal getNextOpen() {
        for (String name : allPortals) {
            Portal tmp = getByName(name);
            if (tmp.pastGrace()) {
                return tmp;
            }
        }
        return null;
    }
}
