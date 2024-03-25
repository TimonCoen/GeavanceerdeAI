import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        ArrayList<Game> tournament = makeTournament();

        /////////////////////////////// test /////////////////
        Game game = tournament.get(0);
        System.out.println(game.toString());
        System.out.println(game.edgesToString());


    }

    private static ArrayList<Game> makeTournament(){
        ArrayList<Game> tournament = new ArrayList<>();
        InputReader inputReader = new InputReader("instances/umps8.txt");
        ArrayList<ArrayList<Integer>> dist = inputReader.getDist();
        ArrayList<ArrayList<Integer>> opponents = inputReader.getOpponents();
        int nteams = inputReader.getnTeams();
        int ngames = nteams/2;
        int roundCounter = 0;

        for(ArrayList<Integer> round: opponents){
            for(Integer game: round){
                if(game > 0) {
                    Game newGame = new Game(round.indexOf(game) + 1, game, opponents.indexOf(round));

                    if(tournament.size() >= ngames) {
                        for (int i = ngames * (roundCounter-1); i < ngames * roundCounter; i++) {
                            Game prevGame = tournament.get(i);
                            int travelDist = dist.get(prevGame.home -1).get(newGame.home -1);
                            Edge newEdge = new Edge(prevGame, newGame, travelDist);
                            prevGame.addEdge(newEdge);
                        }
                    }
                    tournament.add(newGame);
                }
            }
            roundCounter++;
        }

        return tournament;
    }
}
