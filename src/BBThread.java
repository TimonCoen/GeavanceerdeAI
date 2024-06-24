import java.util.ArrayList;

public class BBThread extends Thread{

    private ArrayList<Umpire> umpires;

    public BBThread(ArrayList<Umpire> umpires){
        this.umpires = umpires;
    }

    @Override
    public void run() {
        System.out.println("BBThread is running");
        Main.BranchBound(umpires, 0, 1);
        System.out.println("BBThread is finished!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Thread.currentThread().interrupt();
    }
}
