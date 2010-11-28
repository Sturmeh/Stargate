
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
    public static final int ENTRANCE = -2;
    public static final int ANYTHING = -1;
    private static ArrayList<Gate> gates = new ArrayList<Gate>();
    private String filename;
    private Integer[][] layout;
    private HashMap<Character, Integer> types;
    private RelativeBlockVector[] entrances;
    private RelativeBlockVector[] border;

    private Gate(String filename, Integer[][] layout, HashMap<Character, Integer> types) {
        this.filename = filename;
        this.layout = layout;
        this.types = types;

        populateCoordinates();
    }

    private void populateCoordinates() {
        ArrayList<RelativeBlockVector> entrances = new ArrayList<RelativeBlockVector>();
        ArrayList<RelativeBlockVector> border = new ArrayList<RelativeBlockVector>();

        for (int y = 0; y < layout.length; y++) {
            for (int x = 0; x < layout[y].length; x++) {
                Integer id = layout[y][x];

                if (id == ENTRANCE) {
                    entrances.add(new RelativeBlockVector(x, y, 0));
                } else if (id != ANYTHING) {
                    border.add(new RelativeBlockVector(x, y, 0));
                }
            }
        }
    }
    
    public void save() {
        HashMap<Integer, Character> reverse = new HashMap<Integer, Character>();

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("stargates/" + filename));
            for (Character type : types.keySet()) {
                Integer value = types.get(type);
                reverse.put(value, type);

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
                        symbol = 'O';
                    } else if (id == ANYTHING) {
                        symbol = ' ';
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

    public Integer[][] getLayout() {
        return layout;
    }

    public RelativeBlockVector[] getEntrances() {
        return entrances;
    }

    public RelativeBlockVector[] getBorder() {
        return border;
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
                if (gate != null) gates.add(gate);
            }
        }
    }

    public static Gate loadGate(File file) {
        Scanner scanner = null;
        boolean designing = false;
        ArrayList<ArrayList<Integer>> design = new ArrayList<ArrayList<Integer>>();
        HashMap<Character, Integer> types = new HashMap<Character, Integer>();
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

                        if (symbol == 'O') {
                            id = ENTRANCE;
                        } else if (symbol == ' ') {
                            id = ANYTHING;
                        } else if ((symbol == '?') || (!types.containsKey(symbol))) {
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
                            // TODO: Support for cost, permissions, etc
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
        gate.save(); // Updates format for version changes

        return gate;
    }
    
    public static void populateDefaults(File dir) {
        Integer[][] layout = new Integer[][] {
            {ANYTHING, Portal.OBSIDIAN, Portal.OBSIDIAN, ANYTHING},
            {Portal.OBSIDIAN, ENTRANCE, ENTRANCE, Portal.OBSIDIAN},
            {Portal.OBSIDIAN, ENTRANCE, ENTRANCE, Portal.OBSIDIAN},
            {Portal.OBSIDIAN, ENTRANCE, ENTRANCE, Portal.OBSIDIAN},
            {ANYTHING, Portal.OBSIDIAN, Portal.OBSIDIAN, ANYTHING},
        };
        HashMap<Character, Integer> types = new HashMap<Character, Integer>();
        types.put('X', Portal.OBSIDIAN);

        Gate gate = new Gate("nethergate.gate", layout, types);
        gate.save();
        gates.add(gate);
    }
    
    static class StargateFilenameFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return name.endsWith(".gate");
        }
    }
}
