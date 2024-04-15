public class LBThread extends Thread{
    @Override
    public void run() {
        System.out.println("Thread is running");
        Main.CalculateLBs();
    }
}
