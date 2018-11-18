package se.natusoft.tools.asynctracker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PretendJob {

    private static ExecutorService execsvc = Executors.newFixedThreadPool(30);

    public static void shutdown() {
        System.out.println("Shutting down ...");
        execsvc.shutdown();
        System.out.println("Shutdown complete.");

    }

    void doSomething(int seconds, Handler handler) {
        execsvc.submit( () -> {
            try {
                Thread.sleep( seconds * 1000 );
            }
            catch ( InterruptedException ie ) {
                throw new IllegalStateException( ie.getMessage(), ie );
            }

            handler.handle();

        } );
    }
}
