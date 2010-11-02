
public class Blox {
	private int x;
	private int y;
	private int z;

	public Blox (int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
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
}