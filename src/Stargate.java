/**
 * Stargate.java - Plug-in for hey0's minecraft mod.
 * @author Shaun (sturmeh)
 * @author Dinnerbone
 */
public class Stargate extends SuperPlugin {
	public final Listener listener = new Listener();
	private static String gateSaveLocation = "stargates.txt";
	private static String teleportMessage = "You feel weightless as the portal carries you to new land...";
	private static String registerMessage = "You feel a slight tremble in the ground around the portal...";
	private static String destroyzMessage = "You feel a great shift in energy, as it leaves the portal...";
	private static String noownersMessage = "You feel a great power, yet feel a lack of belonging here...";
	private static String unselectMessage = "You expect something to happen and seem puzzled, what now...";
	private static String collisinMessage = "You anticipate a great surge, but it appears it's blocked...";
	
	public Stargate() { super("stargate"); }

	public void initializeExtra() {
		etc.getLoader().addListener(PluginLoader.Hook.PLAYER_MOVE, listener, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.BLOCK_CREATED, listener, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.BLOCK_DESTROYED, listener, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.COMPLEX_BLOCK_CHANGE, listener, this, PluginListener.Priority.MEDIUM);
	}
	
	public void reloadExtra() {
		gateSaveLocation = config.getString("portal-save-location", gateSaveLocation);
		teleportMessage = config.getString("teleport-message", teleportMessage);
		registerMessage = config.getString("portal-create-message", registerMessage);
		destroyzMessage = config.getString("portal-destroy-message", destroyzMessage);
		noownersMessage = config.getString("not-owner-message", noownersMessage);
		unselectMessage = config.getString("not-selected-message", unselectMessage);
		collisinMessage = config.getString("other-side-blocked-message", collisinMessage);
		Portal.loadAllGates();
	}
	
	public void enableExtra() {
		reloadExtra();
	}
	
	public static String getSaveLocation() {
		return gateSaveLocation;
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
					player.sendMessage(Colors.Red + noownersMessage);
				}
			}
		}
		
		public boolean onBlockDestroy(Player player, Block block) { 
			if (block.getType() != Portal.SIGN && block.getType() != Portal.OBSIDIAN && block.getType() != Portal.BUTTON) return false;
			Portal gate = Portal.getByBlock(block);
			
			if (gate == null) return false;
			if (!player.canUseCommand("/stargate")) return true;
			
			if ((block.getType() == Portal.BUTTON) && (block.getStatus() == 0)) {
				onButtonPressed(player, gate);
				
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
			if ((blockClicked.getType() == Portal.SIGN) || (blockClicked.getType() == Portal.BUTTON)) {
				Portal portal = Portal.getByBlock(blockClicked);
				
				if (portal != null) {
					if (blockClicked.getType() == Portal.SIGN) portal.cycleDestination();
					if (blockClicked.getType() == Portal.BUTTON) onButtonPressed(player, portal);
					
					return true;
				}
			}
			
			return false;
		}

		private void onButtonPressed(Player player, Portal gate) {
			Portal destination = gate.getDestination();
			
			if (!gate.isOpen()) {
				if ((destination == null) || (destination == gate)) {
					player.sendMessage(Colors.Red + unselectMessage);
				} else if (destination.isOpen()) {
					player.sendMessage(Colors.Red + collisinMessage);
				} else {
					gate.open(player);
					destination.open(player);
					destination.setDestination(gate);
				}
			} else {
				gate.close();
				if (destination != null) destination.close();
			}
		}
	}
}
