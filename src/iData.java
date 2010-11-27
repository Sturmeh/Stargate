import java.io.*;
import java.sql.*;
import java.util.logging.Logger;

/*
* iData
*
*    Main class for iConomy data retrieval / updating.
*    Injectable class for plugin-addons
*
* @author  Nijikokun
*/
public final class iData implements Serializable {
    protected static final Logger log = Logger.getLogger("Minecraft");
    public iProperty accounts;
    public iProperty settings;
    private int startingBalance;

    // Serial
    private static final long serialVersionUID = -5796481236376288855L;

    // Database
    static boolean mysql = false;
    static String driver = "com.mysql.jdbc.Driver";
    static String user = "root";
    static String pass = "root";
    static String db = "jdbc:mysql://localhost:3306/minecraft";

    // Directories
    static String directory = "iConomy/";

    /*
     * Returns true if iConomy exists, if not, returns false. If false do nothing, warn in console of iConomy not existing.
     */
    static public boolean iExist() {
        File file = new File("iConomy/settings.properties");

        // If it doesn't exist, iConomy is not installed.
        return file.exists();
    }

    /*
     * Main Class
     *    Controls connection to MySQL or Flatfile
     *
     * @example: public iData data;
     * @example: this.data = new iData(this.mysql, this.startingBalance, this.driver, this.user, this.pass, this.db);
     */
    public iData() {
        if(!iExist())
            return;

        // Settings
        this.settings = new iProperty(directory + "settings.properties");

        // Setup
        mysql = this.settings.getBoolean("use-mysql");

        // Get the starting balance
        this.startingBalance = this.settings.getInt("starting-balance");

        // Database
        driver = this.settings.getString("driver");
        user = this.settings.getString("user");
        pass = this.settings.getString("pass");
        db = this.settings.getString("db");

        if (!mysql) {
            this.accounts = new iProperty("iConomy/balances.properties");
        } else {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException ex) {
                log.severe("[iConomy MySQL] Unable to find driver class " + driver);
            }
        }
    }

    /*
     * Private class for iData
     */
    private Connection MySQL() {
        try {
            return DriverManager.getConnection(db,user,pass);
        } catch (SQLException ex) {
            log.severe("[iConomy MySQL] Unable to retreive MySQL connection");
        }

        return null;
    }

    /*
     * Checks whether a user has a balance currently or not.
     *
     * @return boolean(true/false)
     */
    public boolean hasBalance(String playerName) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean has = false;

        if (mysql) {
            try {
                conn = MySQL();
                ps = conn.prepareStatement("SELECT balance FROM iBalances WHERE player = ? LIMIT 1");
                ps.setString(1, playerName);
                rs = ps.executeQuery();

                has = (rs.next()) ? true : false;
            } catch (SQLException ex) {
                log.severe("[iConomy] Unable to grab the balance for [" + playerName + "] from database!");
            } finally {
                try {
                    if (ps != null) { ps.close(); }
                    if (rs != null) { rs.close(); }
                    if (conn != null) { conn.close(); }
                } catch (SQLException ex) { }
            }
        } else {
            return (this.accounts.getInt(playerName) != 0) ? true : false;
        }

        return has;
    }

    /*
     * Grabs the balance of a player, offline or online. Requries player name not object.
     *    Will create a balance if one does not exist.
     *
     * @return int (balance)
     */
    public int getBalance(String playerName) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int balance = this.startingBalance;

        if (mysql) {
            try {
                conn = MySQL();
                ps = conn.prepareStatement("SELECT balance FROM iBalances WHERE player = ? LIMIT 1");
                ps.setString(1, playerName);
                rs = ps.executeQuery();

                if (rs.next()) {
                    balance = rs.getInt("balance");
                } else {
                    ps = conn.prepareStatement("INSERT INTO iBalances (player, balance) VALUES(?,?)");
                    ps.setString(1, playerName);
                    ps.setInt(2, balance);
                    ps.executeUpdate();
                }
            } catch (SQLException ex) {
                log.severe("[iConomy] Unable to grab the balance for [" + playerName + "] from database!");
            } finally {
                try {
                    if (ps != null) { ps.close(); }
                    if (rs != null) { rs.close(); }
                    if (conn != null) { conn.close(); }
                } catch (SQLException ex) { }
            }
        } else {
            this.accounts.load();
            return (this.hasBalance(playerName)) ? this.accounts.getInt(playerName) : this.accounts.getInt(playerName, this.startingBalance);
        }

        return balance;
    }

    /*
     * Sets the balance of a player. requires player name not object
     *    Will create a balance if one does not exist.
     */
    public void setBalance(String playerName, int balance) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        if (mysql) {
            try {
                conn = MySQL();

                if (hasBalance(playerName)) {
                    ps = conn.prepareStatement("UPDATE iBalances SET balance = ? WHERE player = ? LIMIT 1", Statement.RETURN_GENERATED_KEYS);
                    ps.setInt(1, balance);
                    ps.setString(2, playerName);
                    ps.executeUpdate();
                } else {
                    ps = conn.prepareStatement("INSERT INTO iBalances (player, balance) VALUES(?,?)");
                    ps.setString(1, playerName);
                    ps.setInt(2, balance);
                    ps.executeUpdate();
                }
            } catch (SQLException ex) {
                log.severe("[iConomy] Unable to update or create the balance for [" + playerName + "] from database!");
            } finally {
                try {
                    if (ps != null) { ps.close(); }
                    if (rs != null) { rs.close(); }
                    if (conn != null) { conn.close(); }
                } catch (SQLException ex) { }
            }
        } else {
            this.accounts.setInt(playerName, balance);
        }
    }
}