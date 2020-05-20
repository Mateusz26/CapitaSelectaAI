package tracks.levelGeneration.geneticLevelGenerator.TournamentandEightpointcrossover;

//Added new genetic operators: crossover & tournament selection 

public class Position implements Comparable<Position> {
    private int x;
    private int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public boolean isFirstPosition(Position p) {
        if(this.getY() < p.getY()) {
            return true;
        }
        else if(p.getY() < this.getY()) {
            return false;
        }
        else {
            if(this.getX() < p.getX()) {
                return true;
            }
            else {
                return false;
            }
        }
    }

    @Override
    public String toString() {
        return("x = " + x + " & y = " + y);
    }

    @Override
    public boolean equals(Object o) {
        if(o == this) {
            return true;
        }
        if(!(o instanceof Position)) {
            return false;
        }
        Position p = (Position) o;
        return this.x == p.getX() && this.y == p.getY();
    }

    @Override
    public int hashCode() {
        return x + y;
    }

    @Override
    public int compareTo(Position o) {
        if(this.equals(o)) {
            return 0;
        }
        else {
            if(this.y < o.getY()) {
                return -1;
            }
            else if(this.y > o.getY()) {
                return 1;
            }
            else {
                if(this.x < o.getX()) {
                    return -1;
                }
                else {
                    return 1;
                }
            }
        }
    }
}
