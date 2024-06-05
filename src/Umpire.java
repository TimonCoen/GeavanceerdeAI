import java.util.ArrayList;
import java.util.List;

public class Umpire {
    int id;
//    List<Integer> Home;
//    List<Integer> Away;
    ArrayList<Game> schedule;
    ArrayList<Integer> homeTownVisit;
    int distance;

    public Umpire(int id) {
        this.id = id;
        schedule = new ArrayList<>();
        homeTownVisit = new ArrayList<>();
        for (int i=0; i<Main.nteams; i++){
            homeTownVisit.add(0);
        }
    }

    public Umpire(Umpire umpire){
        this.id = umpire.id;
        schedule = new ArrayList<>();
        schedule.addAll(umpire.schedule);
        this.homeTownVisit = new ArrayList<Integer>(umpire.homeTownVisit);
        this.distance = umpire.distance;
    }

    public void addGameToSchedule(Game g, int distance){
        schedule.add(g);
        this.distance += distance;
        homeTownVisit.set(g.home-1, homeTownVisit.get(g.home-1)+1);
        Main.homeTOWNtoVIST.set(g.home-1, Main.homeTOWNtoVIST.get(g.home-1)-1);
    }

    public void removeFromSchedule(Game g, int distance){
        schedule.remove(g);
        this.distance -= distance;
        homeTownVisit.set(g.home-1, homeTownVisit.get(g.home-1)-1);
        Main.homeTOWNtoVIST.set(g.home-1, Main.homeTOWNtoVIST.get(g.home-1)+1);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Game g : schedule) {
            stringBuilder.append(g.home).append(" ");
        }
        return stringBuilder.toString().trim(); // Trim to remove the trailing space
    }
}
