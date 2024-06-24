import java.util.ArrayList;

public class RBThread extends Thread{

    private ArrayList<Umpire> umpires;

    public RBThread(ArrayList<Umpire> umpires){
        this.umpires = umpires;
    }

    @Override
    public void run() {
        System.out.println("RBThread is running");
        Main.RoundBound(umpires, 1);
        System.out.println("RBThread is finished!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Thread.currentThread().interrupt();
    }
}
