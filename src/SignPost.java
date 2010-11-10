public class SignPost {
	private Block parent;
	private Sign sign;
	private Block block;
	
	public SignPost(Sign sign) {
		this.sign = sign;
		block = etc.getServer().getBlockAt(sign.getX(), sign.getY(), sign.getZ());
	}
	
	public SignPost(Blox block) {
		this.block = block.getBlock();
	}
	
	public Block getParent() {
		if (parent == null) findParent();
		return parent;
	}
	
	public Block getBlock() {
		return block;
	}
	
	public String getText(int index) {
		if (sign == null) findSign();
		if (sign == null) return "";
		return sign.getText(index);
	}
	
	public void setText(int index, String value) {
		if (sign == null) findSign();
		if (sign == null) return;
		sign.setText(index, value);
	}
	
	public String getIdText() {
		if (sign == null) findSign();
		if (sign == null) return "";
		StringBuilder result = new StringBuilder();

		result.append(getText(0));
		result.append("\n");
		result.append(getText(1));
		result.append("\n");
		result.append(getText(2));
		result.append("\n");
		result.append(getText(3));
		
		return result.toString().toLowerCase();
	}
	
	public void update() {
		if (sign == null) findSign();
		if (sign == null) return;
		sign.update();
	}
	
	private void findParent() {
		int offsetX = 0;
		int offsetY = 0;
		int offsetZ = 0;
		
		if (block.getType() == 68) {
			if (block.getData() == 0x2) {
				offsetZ = 1;
			} else if (block.getData() == 0x3) {
				offsetZ = -1;
			} else if (block.getData() == 0x4) {
				offsetX = 1;
			} else if (block.getData() == 0x5) {
				offsetX = -1;
			}
		} else if (block.getType() == 63) {
			offsetY = -1;
		}
		
		parent = etc.getServer().getBlockAt(sign.getX() + offsetX, sign.getY() + offsetY, sign.getZ() + offsetZ);		
	}
	
	private void findSign() {
		try {
			sign = (Sign)etc.getServer().getComplexBlock(block.getX(), block.getY(), block.getZ());
		} finally {
			
		}
	}
	
	public static SignPost getFromBlock(Block block) {
		ComplexBlock complex = etc.getServer().getComplexBlock(block.getX(), block.getY(), block.getZ());
		if (!(complex instanceof Sign)) return null;
		return new SignPost((Sign)complex);
	}
	
	public static SignPost getFromLocation(Location location) {
		return getFromBlock(etc.getServer().getBlockAt((int)location.x, (int)location.y, (int)location.z));
	}
}