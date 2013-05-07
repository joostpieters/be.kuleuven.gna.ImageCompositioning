package gna;

public class Position implements Comparable<Position> {
    private final int x, y;
    private int distanceToSource;
    private boolean inQueue;
    private Position previous;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDistanceToSource() {
        return distanceToSource;
    }

    public void setDistanceToSource(int distanceToSource) {
        this.distanceToSource = distanceToSource;
    }

    public boolean isInQueue() {
        return inQueue;
    }

    public void setInQueue(boolean inQueue) {
        this.inQueue = inQueue;
    }

    public Position getPrevious() {
        return previous;
    }

    public void setPrevious(Position previous) {
        this.previous = previous;
    }

    public boolean isAdjacentTo(Position other) {
        return Math.abs(x - other.x) <= 1 && Math.abs(y - other.y) <= 1 && !this.equals(other);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Position other = (Position) obj;
        if (x != other.x)
            return false;
        if (y != other.y)
            return false;
        return true;
    }

    @Override
    public int compareTo(Position position) {
        if (this.distanceToSource > position.getDistanceToSource()) {
            return 1;
        } else if (this.distanceToSource == position.getDistanceToSource()) {
            return 0;
        } else {
            return -1;
        }
    }
}
