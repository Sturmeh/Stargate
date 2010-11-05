import java.util.Random;
/**
 * Stargate.java - Plug-in for hey0's minecraft mod.
 * @author Shaun (sturmeh)
 * @author Dinnerbone
 */
public class Stargate extends SuperPlugin {
	public final Listener listener = new Listener();
	Random generator = new Random(etc.getServer().getTime());
	private static String teleportMessage = "This sentence has been constructed to match the length of...";
	private static String registerMessage = "You feel a slight tremble in the ground around the portal...";
	private static String destroyzMessage = "You feel a great shift in energy, as it leaves the portal...";
	
	public Stargate() { super("stargate"); }

	public void initializeExtra() {
		etc.getLoader().addListener(PluginLoader.Hook.PLAYER_MOVE, listener, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.BLOCK_DESTROYED, listener, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.COMPLEX_BLOCK_CHANGE, listener, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.BLOCK_CREATED, listener, this, PluginListener.Priority.MEDIUM);
	}
	
	public void reloadExtra() {
		teleportMessage = config.getString("teleport-message", teleportMessage);
		registerMessage = config.getString("portal-create-message", registerMessage);
		destroyzMessage = config.getString("portal-destroy-message", destroyzMessage);
	}

	private class Listener extends PluginListener {
		public void onPlayerMove(Player player, Location from, Location to) {	
			Portal portal = Portal.getByEntrance(to);
			
			if ((portal != null) && (portal.isOpen())) {
				if (portal.isOpenFor().getName() == player.getName()) {
					Portal destination = portal.getDestination();
					
					player.sendMessage(Colors.Blue + teleportMessage);
					player.teleportTo(destination.getExit());
					
					portal.close();
					destination.close();
				} else {
					player.sendMessage(Colors.Red + "Get your own portal!"); // TODO: Message
				}
			}
		}
		
		public boolean onBlockDestroy(Player player, Block block) { 
			if (block.getType() != Portal.SIGN && block.getType() != Portal.OBSIDIAN && block.getType() != Portal.BUTTON) return false;
			Portal gate = Portal.getByBlock(block);
			
			if (gate == null) return false;
			if (!player.canUseCommand("/stargate")) return true;
			
			if ((block.getType() == Portal.BUTTON) && (block.getStatus() == 0)) {
				if (!gate.isOpen()) {
					Portal destination = gate.getDestination();
					if (destination == null) {
						player.sendMessage(Colors.Red + "You must select a destination"); // TODO: Message
					} else if (destination.isOpen()) {
						player.sendMessage(Colors.Red + "The destination is already in use"); // TODO: Message
					} else {
						gate.open(player);
						destination.open(player);
					}
				} else {
					gate.close();
				}
				
				return true;
			} else if (block.getStatus() == 3) {
				gate.unregister();
				player.sendMessage(Colors.Red + destroyzMessage);
			}
			
			return false;
		}
		
		public boolean onComplexBlockChange(Player player, ComplexBlock signBlock) {
			if (!(signBlock instanceof Sign)) return false;
			SignPost sign = new SignPost((Sign)signBlock);
			Portal portal = Portal.createPortal(sign);

			if (portal != null)
				player.sendMessage(Colors.Green + registerMessage);
			
			return false;
		}
		
		public boolean onBlockCreate(Player player, Block blockPlaced, Block blockClicked, int itemInHand) {
			if (blockClicked.getType() == Portal.SIGN) {
				Portal portal = Portal.getByBlock(blockClicked);
				
				if (portal != null) {
					portal.cycleDestination();
					return true;
				}
			}
			
			return false;
		}
	}
}
