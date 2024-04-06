import java.lang.reflect.Array;
import java.util.ArrayList;

public class Game {
    int home;
    int out;
    int round;
    ArrayList<Edge> edges;

    public Game(int home, int out, int round){
        this.home = home;
        this.out = out;
        this.round = round;
        edges = new ArrayList<>();
    }

    public Game(Game game){
        this.home = game.home;
        this.out = game.out;
        this.round = game.round;
        this.edges = game.edges;
    }

    public void addEdge(Edge edge){
        edges.add(edge);
    }

    public ArrayList getEdges(){
        return edges;
    }

    public int getDistance(){
        return getDistance();
    }

    public String toString(){
        return  home + " x " + out;
    }

    public String edgesToString(){
        String os = "edges [";
        for(Edge edge : edges)
            os += edge.toString() + ", ";
        os += "]";
        return os;
    }
}

