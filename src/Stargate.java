
import java.util.HashMap;
import java.util.concurrent.SynchronousQueue;

/**
 * Stargate.java - Plug-in for hey0's minecraft mod.
 * @author Shaun (sturmeh)
 * @author Dinnerbone
 */
public class Stargate extends ThreadedPlugin {
    private final Listener listener = new Listener();
    private static String gateSaveLocation = "stargates/locations.dat";
    private static String teleportMessage = "You feel weightless as the portal carries you to new land...";
    private static String registerMessage = "You feel a slight tremble in the ground around the portal...";
    private static String destroyzMessage = "You feel a great shift in energy, as it leaves the portal...";
    private static String noownersMessage = "You feel a great power, yet feel a lack of belonging here...";
    private static String unselectMessage = "You seem to want to go somewhere, but it's still a secret..."; 
    private static String collisinMessage = "You anticipate a great surge, but it appears it's blocked...";
    private static String cantAffordToUse = "You check your pocket for spare coin, sadly you find none...";
    private static String cantAffordToNew = "You check your pocket for spare coin, sadly you find none...";
    private static String defaultNetwork = "central";
    private static SynchronousQueue<Portal> slip = new SynchronousQueue<Portal>();
    private HashMap<Integer, Location> vehicles = new HashMap<Integer, Location>();

    public Stargate() { 
        super("Stargate", 2.0f, "stargates/stargate");  
    }

    @Override
    public void initializeExtra() {
        etc.getLoader().addListener(PluginLoader.Hook.PLAYER_MOVE, listener, this, PluginListener.Priority.MEDIUM);
        etc.getLoader().addListener(PluginLoader.Hook.BLOCK_RIGHTCLICKED, listener, this, PluginListener.Priority.MEDIUM);
        etc.getLoader().addListener(PluginLoader.Hook.BLOCK_DESTROYED, listener, this, PluginListener.Priority.MEDIUM);
        etc.getLoader().addListener(PluginLoader.Hook.BLOCK_PLACE, listener, this, PluginListener.Priority.MEDIUM);
        etc.getLoader().addListener(PluginLoader.Hook.COMPLEX_BLOCK_CHANGE, listener, this, PluginListener.Priority.MEDIUM);
        etc.getLoader().addListener(PluginLoader.Hook.COMPLEX_BLOCK_SEND, listener, this, PluginListener.Priority.MEDIUM);
        etc.getLoader().addListener(PluginLoader.Hook.BLOCK_PHYSICS, listener, this, PluginListener.Priority.MEDIUM);
        etc.getLoader().addListener(PluginLoader.Hook.VEHICLE_POSITIONCHANGE, listener, this, PluginListener.Priority.MEDIUM);
        etc.getLoader().addListener(PluginLoader.Hook.FLOW, listener, this, PluginListener.Priority.MEDIUM);
        setInterval(160); // 8 seconds.
    }

    @Override
    public void reloadConfig() {
        gateSaveLocation = config.getString("portal-save-location", gateSaveLocation);
        teleportMessage = config.getString("teleport-message", teleportMessage);
        registerMessage = config.getString("portal-create-message", registerMessage);
        destroyzMessage = config.getString("portal-destroy-message", destroyzMessage);
        noownersMessage = config.getString("not-owner-message", noownersMessage);
        unselectMessage = config.getString("not-selected-message", unselectMessage);
        collisinMessage = config.getString("other-side-blocked-message", collisinMessage);
        cantAffordToUse = config.getString("cant-afford-use-message", cantAffordToUse);
        cantAffordToNew = config.getString("cant-afford-create-message", cantAffordToNew);

        defaultNetwork = config.getString("default-gate-network", defaultNetwork).trim();

        Gate.loadGates();
        Portal.loadAllGates();
    }

    @Override
    public synchronized void doWork() {
        Portal open = Portal.getNextOpen();

        if (open != null) {
            try {
                slip.put(open);
            } catch (InterruptedException e) {
            }
        }
    }

    public void threadSafeOperation() {
        Portal open = slip.poll();
        if (open != null) {
            if (open.isOpen()) {
                open.close();
            } else if (open.isActive()) {
                open.deactivate();
            }
        }
    }

    public static String getSaveLocation() {
        return gateSaveLocation;
    }

    public static String getDefaultNetwork() {
        return defaultNetwork;
    }

    public static String getCantAffordToNew() {
        return cantAffordToNew;
    }

    private void onButtonPressed(Player player, Portal gate) {
        Portal destination = gate.getDestination();

        if (!gate.isOpen()) {
            if ((destination == null) || (destination == gate)) {
                if (!unselectMessage.isEmpty()) {
                    player.sendMessage(Colors.Red + unselectMessage);
                }
            } else if ((destination.isOpen() || destination.isFixed())) {
                if (!collisinMessage.isEmpty()) {
                    player.sendMessage(Colors.Red + collisinMessage);
                }
            } else if (gate.getGate().deductCost(Gate.CostFor.Activating, player)) {
                gate.open(player);
                destination.open(player);
                destination.setDestination(gate);
                if (destination.isVerified()) {
                    destination.drawSign(true);
                }
            } else {
                if (!cantAffordToUse.isEmpty()) {
                    player.sendMessage(Colors.Red + cantAffordToUse);
                }
            }
        } else {
            gate.close();
            if (destination != null) {
                destination.close();
            }
        }
    }

    private class Listener extends PluginListener {
        @Override
        public void onPlayerMove(Player player, Location from, Location to) {
            threadSafeOperation();
            Portal portal = Portal.getByEntrance(to);

            if ((portal != null) && (portal.isOpen())) {
                if (portal.isOpenFor(player)) {
                    if (portal.getGate().deductCost(Gate.CostFor.Using, player)) {
                        Portal destination = portal.getDestination();

                        if (destination != null) {
                            if (!teleportMessage.isEmpty()) {
                                player.sendMessage(Colors.Blue + teleportMessage);
                            }

                            destination.teleport(player, portal);

                            if (!portal.isFixed()) {
                                portal.close();
                            }
                            if ((!destination.isFixed()) && (destination.getDestinationName().equalsIgnoreCase(portal.getName()))) {
                                destination.close();
                            }
                        }
                    } else {
                        player.sendMessage(Colors.Red + cantAffordToUse);
                    }
                } else {
                    if (!noownersMessage.isEmpty()) {
                        player.sendMessage(Colors.Red + noownersMessage);
                    }
                }
            }
        }

        @Override
        public void onBlockRightClicked(Player player, Block block, Item item) {
            if ((block.blockType == Block.Type.SignPost) || (block.blockType == Block.Type.WallSign)) {
                Portal portal = Portal.getByBlock(block);

                if (portal != null) {
                    if ((!portal.isOpen()) && (!portal.isFixed())) {
                        portal.cycleDestination();
                    }
                }
            }
        }

        @Override
        public boolean onBlockDestroy(Player player, Block block) {
            if (block.getType() != Portal.SIGN && block.getType() != Portal.OBSIDIAN && block.getType() != Portal.BUTTON) {
                return false;
            }
            Portal portal = Portal.getByBlock(block);
            if (portal == null) portal = Portal.getByEntrance(block);

            if (portal == null) {
                return false;
            }

            if ((block.getType() == Portal.BUTTON) && (block.getStatus() == 0)) {
                if (player.canUseCommand("/stargateuse")) {
                    onButtonPressed(player, portal);
                }

                return true;
            } else if (block.getStatus() < 2) {
                if (!player.canUseCommand("/stargatedestroy")) {
                    return true;
                }
            } else if (block.getStatus() == 3) {
                if (!player.canUseCommand("/stargatedestroy")) {
                    return true;
                }
                portal.unregister();
                if (!destroyzMessage.isEmpty()) {
                    player.sendMessage(Colors.Red + destroyzMessage);
                }
            }

            return false;
        }

        @Override
        public boolean onComplexBlockChange(Player player, ComplexBlock signBlock) {
            if (!(signBlock instanceof Sign)) {
                return false;
            }
            SignPost sign = new SignPost((Sign) signBlock);

            if (!player.canUseCommand("/stargatecreate")) {
                return true;
            }

            Portal portal = Portal.createPortal(sign, player);

            if (portal != null && !registerMessage.isEmpty()) {
                player.sendMessage(Colors.Green + registerMessage);
            }

            return false;
        }

        @Override
        public boolean onSendComplexBlock(Player player, ComplexBlock signBlock) {
            if (!(signBlock instanceof Sign)) {
                return false;
            }
            Portal portal = Portal.getByBlock(etc.getServer().getBlockAt(signBlock.getX(), signBlock.getY(), signBlock.getZ()));
            if (portal == null) {
                return false;
            }
            boolean update = true;

            if ((!portal.wasVerified()) && (portal.isVerified())) {
                if (!portal.checkIntegrity()) {
                    portal.close();
                    portal.unregister();
                    update = false;
                    log("Destroying stargate at " + portal.toString());
                }
            }

            if (update) {
                portal.drawSign(false);
            }

            return false;
        }

        @Override
        public boolean onBlockPhysics(Block block, boolean placed) {
            if (block.getType() == 90) {
                return Portal.getByEntrance(block) != null;
            }

            return false;
        }

        @Override
        public boolean onBlockPlace(Player player, Block blockPlaced, Block blockClicked, Item itemInHand) {
            Portal portal = Portal.getByEntrance(blockPlaced);

            if (portal != null) {
                return !player.canUseCommand("/stargatecreate");
            }

            return false;
        }

        @Override
        public void onVehiclePositionChange(BaseVehicle vehicle, int x, int y, int z) {
            Player player = vehicle.getPassenger();

            Location lookup = vehicles.get(vehicle.getId());

            if (lookup != null) {
                vehicle.setMotion(lookup.x, lookup.y, lookup.z);
                vehicles.remove(vehicle.getId());
            }

            if (player != null) {
                Portal portal = Portal.getByEntrance(etc.getServer().getBlockAt(x, y, z));

                if ((portal != null) && (portal.isOpen())) {
                    if (portal.isOpenFor(player)) {
                        if (portal.getGate().deductCost(Gate.CostFor.Using, player)) {
                            Portal destination = portal.getDestination();

                            if (destination != null) {
                                if (!teleportMessage.isEmpty()) {
                                    player.sendMessage(Colors.Blue + teleportMessage);
                                }

                                vehicles.put(vehicle.getId(), destination.teleport(vehicle, portal));

                                if (!portal.isFixed()) {
                                    portal.close();
                                }
                                if ((!destination.isFixed()) && (destination.getDestinationName().equalsIgnoreCase(portal.getName()))) {
                                    destination.close();
                                }
                            }
                        } else {
                            player.sendMessage(Colors.Red + cantAffordToUse);
                        }
                    } else {
                        if (!noownersMessage.isEmpty()) {
                            player.sendMessage(Colors.Red + noownersMessage);
                        }
                    }
                }
            }
        }

        @Override
        public boolean onFlow(Block blockFrom, Block blockTo) {
            Portal portal = Portal.getByEntrance(blockFrom);

            if (portal != null) {
                return blockTo.getY() == blockFrom.getY();
            }

            return false;
        }
    }
}
