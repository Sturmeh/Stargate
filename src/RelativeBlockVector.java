/**
 * RelativeBlockVector.java - Plug-in for hey0's minecraft mod.
 * @author Shaun (sturmeh)
 * @author Dinnerbone
 */
public class RelativeBlockVector {
    private int right = 0;
    private int depth = 0;
    private int distance = 0;

    public RelativeBlockVector(int right, int depth, int distance) {
        this.right = right;
        this.depth = depth;
        this.distance = distance;
    }

    public int getX() {
        return right;
    }

    public int getY() {
        return depth;
    }

    public int getZ() {
        return distance;
    }
}
