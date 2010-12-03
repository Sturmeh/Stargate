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
	
	public Blox (String string) {
		String[] split = string.split(",");
		this.x = Integer.parseInt(split[0]);
		this.y = Integer.parseInt(split[1]);
		this.z = Integer.parseInt(split[2]);
	}
	
	public Blox makeRelative(int x, int y, int z) {
		return new Blox(this.x + x, this.y + y, this.z + z);
	}
	
	public Location makeRelativeLoc(double x, double y, double z, float rotX, float rotY) {
		return new Location((double)this.x + x, (double)this.y + y, (double)this.z + z, rotX, rotY);
	}

        public Blox modRelative(int right, int depth, int distance, int modX, int modY, int modZ) {
             return makeRelative(-right * modX + distance * modZ, -depth * modY, -right * modZ + -distance * modX);
        }

        public Location modRelativeLoc(double right, double depth, double distance, float rotX, float rotY, int modX, int modY, int modZ) {
            return makeRelativeLoc(0.5 + -right * modX + distance * modZ, depth, 0.5 + -right * modZ + -distance * modX, rotX, 0);
        }

	public void setType(int type) {
		etc.getServer().setBlockAt(type, x, y, z);
	}

	public int getType() {
		return etc.getServer().getBlockIdAt(x, y, z);
	}

	public void setData(int data) {
		etc.getServer().setBlockData(x, y, z, data);
	}

	public int getData() {
		return etc.getServer().getBlockData(x, y, z);
	}

	public Block getBlock() {
		return etc.getServer().getBlockAt(x, y, z);
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getZ() {
		return z;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(x);
		builder.append(',');
		builder.append(y);
		builder.append(',');
		builder.append(z);
		return builder.toString();
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