
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;

/**
 * Gate.java - Plug-in for hey0's minecraft mod.
 * @author Shaun (sturmeh)
 * @author Dinnerbone
 */
public class Gate {
    public static final int ANYTHING = -1;
    public static final int ENTRANCE = -2;
    public static final int CONTROL = -3;
    private static HashMap<String, Gate> gates = new HashMap<String, Gate>();
    private static HashMap<Integer, ArrayList<Gate>> controlBlocks = new HashMap<Integer, ArrayList<Gate>>();

    private String filename;
    private Integer[][] layout;
    private HashMap<Character, Integer> types;
    private RelativeBlockVector[] entrances = new RelativeBlockVector[0];
    private RelativeBlockVector[] border = new RelativeBlockVector[0];
    private RelativeBlockVector[] controls = new RelativeBlockVector[0];
    private HashMap<RelativeBlockVector, Integer> exits = new HashMap<RelativeBlockVector, Integer>();
    private int portalBlockOpen = 90;
    private int portalBlockClosed = 0;
    private PaymentMethod costType = PaymentMethod.None;
    private int costToUse = 0;
    private int costToActivate = 0;
    private int costToCreate = 0;
    private String costDestination = "none";

    private Gate(String filename, Integer[][] layout, HashMap<Character, Integer> types) {
        this.filename = filename;
        this.layout = layout;
        this.types = types;

        populateCoordinates();
    }

    private void populateCoordinates() {
        ArrayList<RelativeBlockVector> entrances = new ArrayList<RelativeBlockVector>();
        ArrayList<RelativeBlockVector> border = new ArrayList<RelativeBlockVector>();
        ArrayList<RelativeBlockVector> controls = new ArrayList<RelativeBlockVector>();
        RelativeBlockVector[] relativeExits = new RelativeBlockVector[layout[0].length];
        int[] exitDepths = new int[layout[0].length];
        int bottom = 0;
        RelativeBlockVector lastExit = null;

        for (int y = 0; y < layout.length; y++) {
            for (int x = 0; x < layout[y].length; x++) {
                Integer id = layout[y][x];

                if (id == ENTRANCE) {
                    entrances.add(new RelativeBlockVector(x, y, 0));
                    exitDepths[x] = y;
                    bottom = y;
                } else if (id == CONTROL) {
                    controls.add(new RelativeBlockVector(x, y, 0));
                } else if (id != ANYTHING) {
                    border.add(new RelativeBlockVector(x, y, 0));
                }
            }
        }

        for (int x = 0; x < exitDepths.length; x++) {
            if (exitDepths[x] >= bottom - 3) {
                lastExit = new RelativeBlockVector(x, exitDepths[x], 0);
            }

            relativeExits[x] = lastExit;
        }

        for (int x = relativeExits.length - 1; x >= 0; x--) {
            if (relativeExits[x] != null) {
                lastExit = relativeExits[x];
            } else {
                relativeExits[x] = lastExit;
            }

            if (exitDepths[x] > 0) this.exits.put(relativeExits[x], x);
        }

        this.entrances = entrances.toArray(this.entrances);
        this.border = border.toArray(this.border);
        this.controls = controls.toArray(this.controls);
    }
    
    public void save() {
        HashMap<Integer, Character> reverse = new HashMap<Integer, Character>();

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("stargates/" + filename));

            writeConfig(bw, "portal-open", portalBlockOpen);
            writeConfig(bw, "portal-closed", portalBlockClosed);
            writeConfig(bw, "cost-type", costType.toString());
            writeConfig(bw, "cost-to-use", costToUse);
            writeConfig(bw, "cost-to-create", costToCreate);
            writeConfig(bw, "cost-to-activate", costToActivate);
            writeConfig(bw, "cost-destination", costDestination);

            for (Character type : types.keySet()) {
                Integer value = types.get(type);

                if (!type.equals('-')) {
                    reverse.put(value, type);
                }

                bw.append(type);
                bw.append('=');
                bw.append(value.toString());
                bw.newLine();
            }

            bw.newLine();

            for (int y = 0; y < layout.length; y++) {
                for (int x = 0; x < layout[y].length; x++) {
                    Integer id = layout[y][x];
                    Character symbol;

                    if (id == ENTRANCE) {
                        symbol = '.';
                    } else if (id == ANYTHING) {
                        symbol = ' ';
                    } else if (id == CONTROL) {
                        symbol = '-';
                    } else if (reverse.containsKey(id)) {
                        symbol = reverse.get(id);
                    } else {
                        symbol = '?';
                    }

                    bw.append(symbol);
                }
                bw.newLine();
            }

            bw.close();
        } catch (IOException ex) {
            Stargate.log(Level.SEVERE, "Could not load Gate " + filename + " - " + ex.getMessage());
        }
    }

    private void writeConfig(BufferedWriter bw, String key, int value) throws IOException {
        bw.append(String.format("%s=%d", key, value));
        bw.newLine();
    }

    private void writeConfig(BufferedWriter bw, String key, String value) throws IOException {
        bw.append(String.format("%s=%s", key, value));
        bw.newLine();
    }

    public Integer[][] getLayout() {
        return layout;
    }

    public RelativeBlockVector[] getEntrances() {
        return entrances;
    }

    public RelativeBlockVector[] getBorder() {
        return border;
    }

    public RelativeBlockVector[] getControls() {
        return controls;
    }

    public HashMap<RelativeBlockVector, Integer> getExits() {
        return exits;
    }

    public int getControlBlock() {
        return types.get('-');
    }

    public String getFilename() {
        return filename;
    }

    public int getPortalBlockOpen() {
        return portalBlockOpen;
    }

    public int getPortalBlockClosed() {
        return portalBlockClosed;
    }

    public boolean deductCost(CostFor type, Player player) {
        if (costType == PaymentMethod.Blocks) {
            Stargate.log(Level.WARNING, "Blocks payment type is NYI");
        }

        if (costType == PaymentMethod.iConomy) {
            if (!iData.iExist()) {
                Stargate.log(Level.WARNING, "iConomy payment selected but iConomy does not exist");
                return true;
            }

            int cost = 0;

            switch (type) {
                case Activating:
                    cost = costToActivate;
                    break;
                case Creating:
                    cost = costToCreate;
                    break;
                case Using:
                    cost = costToUse;
                    break;
            }

            if (cost == 0) {
                iData icon = new iData();
                int balance = icon.getBalance(player.getName());
                String deducted = icon.settings.getString("money-deducted", "");
                String money = icon.settings.getString("money-name", "");
                String receive = icon.settings.getString("money-receive", "");
                
                if (balance >= cost) {
                    String[] recipient = costDestination.split(" ", 2);
                    icon.setBalance(player.getName(), balance - cost);
                    if (!deducted.isEmpty()) player.sendMessage(String.format(deducted, cost + money));

                    if ((recipient.length > 0) && (recipient[0].equalsIgnoreCase("player"))) {
                        if ((recipient.length > 1) && (icon.hasBalance(recipient[1]))) {
                            balance = icon.getBalance(recipient[1]);
                            icon.setBalance(recipient[1], balance);

                            if (!receive.isEmpty()) player.sendMessage(String.format(receive, cost + money));
                        } else {
                            Stargate.log(Level.WARNING, "cost-destination set to player but specified player does not exist or was not defined");
                        }
                    }

                    return true;
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean matches(Block topleft, int modX, int modZ) {
        return matches(new Blox(topleft), modX, modZ);
    }

    public boolean matches(Blox topleft, int modX, int modZ) {
        for (int y = 0; y < layout.length; y++) {
            for (int x = 0; x < layout[y].length; x++) {
                int id = layout[y][x];

                if (id == ENTRANCE) {
                    if (topleft.modRelative(x, y, 0, modX, 1, modZ).getType() != 0) {
                        return false;
                    }
                } else if (id == CONTROL) {
                    if (topleft.modRelative(x, y, 0, modX, 1, modZ).getType() != getControlBlock()) {
                        return false;
                    }
                } else if (id != ANYTHING) {
                     if (topleft.modRelative(x, y, 0, modX, 1, modZ).getType() != id) {
                         return false;
                     }
                }
            }
        }

        return true;
    }

    private static void registerGate(Gate gate) {
        gates.put(gate.getFilename(), gate);

        int blockID = gate.getControlBlock();

        if (!controlBlocks.containsKey(blockID)) {
            controlBlocks.put(blockID, new ArrayList<Gate>());
        }

        controlBlocks.get(blockID).add(gate);
    }

    private static Gate loadGate(File file) {
        Scanner scanner = null;
        boolean designing = false;
        ArrayList<ArrayList<Integer>> design = new ArrayList<ArrayList<Integer>>();
        HashMap<Character, Integer> types = new HashMap<Character, Integer>();
        HashMap<String, String> config = new HashMap<String, String>();
        int cols = 0;

        try {
            scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (designing) {
                    ArrayList<Integer> row = new ArrayList<Integer>();

                    if (line.length() > cols) {
                        cols = line.length();
                    }

                    for (Character symbol : line.toCharArray()) {
                        Integer id = ANYTHING;

                        if (symbol.equals('.')) {
                            id = ENTRANCE;
                        } else if (symbol.equals(' ')) {
                            id = ANYTHING;
                        } else if (symbol.equals('-')) {
                            id = CONTROL;
                        } else if ((symbol.equals('?')) || (!types.containsKey(symbol))) {
                            Stargate.log(Level.SEVERE, "Could not load Gate " + file.getName() + " - Unknown symbol '" + symbol + "' in diagram");
                            return null;
                        } else {
                            id = types.get(symbol);
                        }

                        row.add(id);
                    }

                    design.add(row);
                } else {
                    if ((line.isEmpty()) || (!line.contains("="))) {
                        designing = true;
                    } else {
                        String[] split = line.split("=");
                        String key = split[0].trim();
                        String value = split[1].trim();

                        if (key.length() == 1) {
                            Character symbol = key.charAt(0);
                            Integer id = Integer.parseInt(value);

                            types.put(symbol, id);
                        } else {
                            config.put(key, value);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Stargate.log(Level.SEVERE, "Could not load Gate " + file.getName() + " - Invalid block ID given");
            return null;
        } finally {
            if (scanner != null) scanner.close();
        }

        Integer[][] layout = new Integer[design.size()][cols];

        for (int y = 0; y < design.size(); y++) {
            ArrayList<Integer> row = design.get(y);
            Integer[] result = new Integer[cols];

            for (int x = 0; x < cols; x++) {
                if (x < row.size()) {
                    result[x] = row.get(x);
                } else {
                    result[x] = ANYTHING;
                }
            }

            layout[y] = result;
        }

        Gate gate = new Gate(file.getName(), layout, types);

        gate.portalBlockOpen = readConfig(config, gate, file, "portal-open", gate.portalBlockOpen);
        gate.portalBlockClosed = readConfig(config, gate, file, "portal-closed", gate.portalBlockClosed);
        
        if (config.containsKey("cost-type")) {
            String val = config.get("cost-type");

            if ((val.equalsIgnoreCase("iconomy")) || (val.equalsIgnoreCase("coins"))) {
                gate.costType = PaymentMethod.iConomy;
            } else if ((val.equalsIgnoreCase("blocks")) || (val.equalsIgnoreCase("items"))) {
                gate.costType = PaymentMethod.Blocks;
            }
        }
        gate.costToUse = readConfig(config, gate, file, "cost-to-use", gate.costToUse);
        gate.costToActivate = readConfig(config, gate, file, "cost-to-activate", gate.costToActivate);
        gate.costToCreate = readConfig(config, gate, file, "cost-to-create", gate.costToCreate);
        gate.costDestination = readConfig(config, gate, file, "cost-destination", gate.costDestination);

        if (gate.getControls().length != 2) {
            Stargate.log(Level.SEVERE, "Could not load Gate " + file.getName() + " - Gates must have exactly 2 control points.");
            return null;
        } else {
            gate.save(); // Updates format for version changes
            return gate;
        }
    }

    private static int readConfig(HashMap<String, String> config, Gate gate, File file, String key, int def) {
        if (config.containsKey(key)) {
            try {
                return Integer.parseInt(config.get(key));
            } catch (NumberFormatException ex) {
                Stargate.log(Level.WARNING, String.format("%s reading %s: %s is not numeric", ex.getClass().getName(), file, key));
            }
        }

        return def;
    }

    private static String readConfig(HashMap<String, String> config, Gate gate, File file, String key, String def) {
        if (config.containsKey(key)) {
            if (!config.get(key).isEmpty()) {
                return config.get(key);
            }
        }

        return def;
    }

    public static void loadGates() {
        File dir = new File("stargates");
        File[] files;

        if (dir.exists()) {
            files = dir.listFiles(new StargateFilenameFilter());
        } else {
            files = new File[0];
        }

        if (files.length == 0) {
            dir.mkdir();
            populateDefaults(dir);
        } else {
            for (File file : files) {
                Gate gate = loadGate(file);
                if (gate != null) registerGate(gate);
            }
        }
    }
    
    public static void populateDefaults(File dir) {
        Integer[][] layout = new Integer[][] {
            {ANYTHING, Portal.OBSIDIAN, Portal.OBSIDIAN, ANYTHING},
            {Portal.OBSIDIAN, ENTRANCE, ENTRANCE, Portal.OBSIDIAN},
            {CONTROL, ENTRANCE, ENTRANCE, CONTROL},
            {Portal.OBSIDIAN, ENTRANCE, ENTRANCE, Portal.OBSIDIAN},
            {ANYTHING, Portal.OBSIDIAN, Portal.OBSIDIAN, ANYTHING},
        };
        HashMap<Character, Integer> types = new HashMap<Character, Integer>();
        types.put('X', Portal.OBSIDIAN);
        types.put('-', Portal.OBSIDIAN);

        Gate gate = new Gate("nethergate.gate", layout, types);
        gate.save();
        registerGate(gate);
    }

    public static Gate[] getGatesByControlBlock(Block block) {
        return getGatesByControlBlock(block.getType());
    }

    public static Gate[] getGatesByControlBlock(int type) {
        Gate[] result = new Gate[0];
        
        result = controlBlocks.get(type).toArray(result);

        return result;
    }

    public static Gate getGateByName(String name) {
        return gates.get(name);
    }
    
    static class StargateFilenameFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return name.endsWith(".gate");
        }
    }

    public enum PaymentMethod {
        iConomy,
        Blocks,
        None
    };

    public enum CostFor {
        Using,
        Activating,
        Creating
    };
}
