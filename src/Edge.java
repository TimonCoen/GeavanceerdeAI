public class Edge {
    Game source;
    Game destination;
    int distance;

public Edge(Game source, Game destination, int distance){
    this.source = source;
    this.destination = destination;
    this.distance = distance;
}

    public void setDestination(Game destination) {
        this.destination = destination;
    }

    public void setSource(Game source) {
        this.source = source;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public String toString(){
        String os = source.toString() + " -> " + destination.toString();
        return os;
    }
}
