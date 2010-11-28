
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Portal.java - Plug-in for hey0's minecraft mod.
 * @author Shaun (sturmeh)
 * @author Dinnerbone
 */
public class Gate {
    public static final int ENTRANCE = -2;
    public static final int ANYTHING = -1;
    private static ArrayList<Gate> gates = new ArrayList<Gate>();
    private String name;
    private Integer[][] layout;
    private HashMap<Character, Integer> types;

    private Gate(String name, Integer[][] layout, HashMap<Character, Integer> types) {
        this.name = name;
        this.layout = layout;
        this.types = types;
    }
    
    public void save() {
        HashMap<Integer, Character> reverse = new HashMap<Integer, Character>();

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("stargates/" + name + ".portal"));
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
            Logger.getLogger(Gate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Integer[][] getLayout() {
        return layout;
    }

    public static void loadGates() {
        File dir = new File("stargates");

        if (!dir.exists()) {
            dir.mkdir();
            populateDefaults(dir);
        }
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

        Gate gate = new Gate("nethergate", layout, types);
        gate.save();
    }

    public static void saveGate() {

    }
}
