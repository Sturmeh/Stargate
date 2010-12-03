
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CostHandler.java - Plug-in for hey0's minecraft mod.
 * @author Shaun (sturmeh)
 * @author Dinnerbone
 */
class CostHandler {
    private Item[] items = new Item[0];
    private final static Pattern regex = Pattern.compile("\\s*(?:(\\d+)\\s*[x\\*]\\s*)?([^,]+)\\s*(?:$|,)");
    private PaymentMethod method;
    private int money;
    private String destination;

    public CostHandler() {
        method = PaymentMethod.None;
    }

    public CostHandler(String cost) {
        method = PaymentMethod.Blocks;

        ArrayList<Item> array = new ArrayList<Item>();
        Matcher matcher = regex.matcher(cost);

        while (matcher.find()) {
            int amount = 1;
            int itemid = etc.getDataSource().getItem(matcher.group(2));

            if (itemid == 0) {
                try {
                    itemid = Integer.parseInt(matcher.group(2));
                } catch (NumberFormatException ex) {
                    Stargate.log(Level.WARNING, ex.getMessage() + " while parsing block payment string - no such block exists?");
                    continue;
                }
            }

            if ((matcher.group(1) != null) && (!matcher.group(1).isEmpty())) {
                try {
                    amount = Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException ex) {
                    amount = 1;
                    Stargate.log(Level.WARNING, ex.getMessage() + " while parsing block payment string");
                }
            }

            array.add(new Item(itemid, amount));
            Stargate.log(new Item(itemid, amount).toString());
        }

        items = array.toArray(items);
    }

    public CostHandler(String cost, String moneyDest) {
        method = PaymentMethod.iConomy;
        try {
            money = Integer.parseInt(cost);
        } catch (NumberFormatException ex) {
            money = 0;
            Stargate.log(Level.WARNING, ex.getMessage() + " while parsing iConomy payment");
        }
    }

    public boolean deductCost(Player player) {
        if (method == PaymentMethod.Blocks) {
            return deductBlocks(player);
        } else if (method == PaymentMethod.iConomy) {
            return deductIConomy(player);
        } else {
            return true;
        }
    }

    private boolean deductBlocks(Player player) {
        Inventory inventory = player.getInventory();

        for (Item item : items) {
            if (inventory.hasItem(item.getItemId(), item.getAmount(), 65)) {
                inventory.removeItem(item);
                inventory.updateInventory();
                return true;
            }
        }

        return false;
    }

    private boolean deductIConomy(Player player) {
        if (!iData.iExist()) {
            Stargate.log(Level.WARNING, "iConomy payment selected but iConomy does not exist");
            return true;
        }

        if (money > 0) {
            iData icon = new iData();
            int balance = icon.getBalance(player.getName());
            String deducted = icon.settings.getString("money-deducted", "");
            String coin = icon.settings.getString("money-name", "");
            String receive = icon.settings.getString("money-receive", "");

            if (balance >= money) {
                String[] recipient = destination.split(" ", 2);
                icon.setBalance(player.getName(), balance - money);
                if (!deducted.isEmpty()) player.sendMessage(String.format(deducted, money + coin));

                if ((recipient.length > 0) && (recipient[0].equalsIgnoreCase("player"))) {
                    if ((recipient.length > 1) && (icon.hasBalance(recipient[1]))) {
                        balance = icon.getBalance(recipient[1]);
                        icon.setBalance(recipient[1], balance);

                        if (!receive.isEmpty()) player.sendMessage(String.format(receive, money + coin));
                    } else {
                        Stargate.log(Level.WARNING, "cost-destination set to player but specified player does not exist or was not defined");
                    }
                }

                return true;
            } else {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        if (method == PaymentMethod.iConomy) {
            return String.valueOf(money);
        } else if (method == PaymentMethod.Blocks) {
            StringBuilder builder = new StringBuilder();

            for (Item item : items) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }

                if (item.getAmount() > 1) {
                    builder.append(item.getAmount());
                    builder.append("* ");
                }

                builder.append(etc.getDataSource().getItem(item.getItemId()));
            }

            return builder.toString();
        } else {
            return "None";
        }
    }

    public enum PaymentMethod {
        iConomy,
        Blocks,
        None
    };
}