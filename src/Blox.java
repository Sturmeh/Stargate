public class Blox {
	private int x;
	private int y;
	private int z;

	public Blox (int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Blox (Block block) {
		this.x = block.getX();
		this.y = block.getY();
		this.z = block.getZ();
	}
	
	public Blox (Location location) {
		this.x = (int)location.x;
		this.y = (int)location.y;
		this.z = (int)location.z;
	}
	
	public Blox makeRelative(int x, int y, int z) {
		return new Blox(this.x + x, this.y + y, this.z + z);
	}

	public void setType(int type) {
		etc.getServer().setBlockAt(type, x, y, z);
	}

	public int getType() {
		return etc.getServer().getBlockIdAt(x, y, z);
	}
	
	public String toString() {
		return x+", "+y+", "+z;
	}
	
	@Override
	public int hashCode() {
		int result = 0;
		
		result += x * 92821;
		result += y * 92821;
		result += z * 92821;
		
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		
		Blox blox = (Blox) obj;
		
		return (x == blox.x) && (y == blox.y) && (z == blox.z); 
	}
}