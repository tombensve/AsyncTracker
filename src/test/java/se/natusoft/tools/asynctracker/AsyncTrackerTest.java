package se.natusoft.tools.asynctracker;

import org.junit.AfterClass;
import org.junit.Test;

import java.util.UUID;

public class AsyncTrackerTest {

    @AfterClass
    public static void allDone() {
        PretendJob.shutdown();
    }

    @Test
    public void testNewJobJobDone1() {
        System.out.println("testNewJobJobDone1");

        String tname = "test1";

        PretendJob job = new PretendJob();

        UUID jobId1 = AsyncTracker.get( tname ).newJob();
        System.out.println("NJD: Doing first job ... ");
        job.doSomething( 2, () -> {

            // Some code executing here ...
            System.out.println("NJD: first job done.");

            AsyncTracker.get( tname ).jobDone( jobId1 );
        });

        UUID jobId2 = AsyncTracker.get( tname ).newJob();
        System.out.println("NJD: Doing second job ... ");
        job.doSomething( 3, () -> {

            // Some code executing here ...
            System.out.println("NJD: Second job done.");

            AsyncTracker.get( tname ).jobDone( jobId2 );
        });

        UUID jobId3 = AsyncTracker.get( tname ).newJob();
        System.out.println("NJD: Doing third job ... ");
        job.doSomething( 1, () -> {

            // Some code executing here ...
            System.out.println("NJD: Third job done.");

            AsyncTracker.get( tname ).jobDone( jobId3 );
        });


        System.out.println("Waiting for active jobs ...");
        AsyncTracker.get( tname ).waitForActiveJobs();
        System.out.println("Jobs done!");

        System.out.println( );
    }

    @Test
    public void testNewJobJobDone2() {
        System.out.println("testNewJobJobDone2");

        String tname = "test2";
        PretendJob job = new PretendJob();

        UUID jobId1 = AsyncTracker.get( tname ).newJob();
        System.out.println("NJD: Doing first job ... ");
        job.doSomething( 2, () -> {

            // Some code executing here ...
            System.out.println("NJD: First job done.");

            AsyncTracker.get( tname ).jobDone( jobId1 );
        });

        UUID jobId2 = AsyncTracker.get( tname ).newJob();
        System.out.println("NJD: Doing second job ... ");
        job.doSomething( 2, () -> {

            // Some code executing here ...
            System.out.println("NJD: Second job done.");

            AsyncTracker.get( tname ).jobDone( jobId2 );
        });

        UUID jobId3 = AsyncTracker.get( tname ).newJob();
        System.out.println("NJD: Doing third job ... ");
        job.doSomething( 2, () -> {

            // Some code executing here ...
            System.out.println("NJD: Third job done.");

            AsyncTracker.get( tname ).jobDone( jobId3 );
        });


        System.out.println("Waiting for active jobs ...");
        AsyncTracker.get( tname ).waitForActiveJobs();
        System.out.println("Jobs done!");

        System.out.println(  );
    }

    @Test
    public void testLastJobDone1() {
        System.out.println("testLastJobDone1");

        String tname = "test3";
        PretendJob job = new PretendJob();

        System.out.println("LJD: Doing first job ... ");
        job.doSomething( 2, () -> {

            // Some code executing here ...
            System.out.println("LJD: First job done.");


        });

        System.out.println("LJD: Doing second job ... ");
        job.doSomething( 3, () -> {

            // Some code executing here ...
            System.out.println("LJD: Second job done.");


        });

        System.out.println("LJD: Doing third job ... ");
        job.doSomething( 1, () -> {

            // Some code executing here ...
            System.out.println("LJD: Third job done.");

            AsyncTracker.get( tname).markLastJobDone( 3 );
        });


        System.out.println("Waiting for last job ...");
        AsyncTracker.get( tname ).waitForLastJobDone();
        System.out.println("Last job done!");

        System.out.println(  );
    }

    @Test
    public void testLastJobDone2() {
        System.out.println("testLastJobDone2");

        String tname = "test4";
        PretendJob job = new PretendJob();

        System.out.println("LJD: Doing first job ... ");
        job.doSomething( 2, () -> {

            // Some code executing here ...
            System.out.println("LJD: First job done.");

        });

        System.out.println("LJD: Doing second job ... ");
        job.doSomething( 2, () -> {

            // Some code executing here ...
            System.out.println("LJD: Second job done.");


        });

        System.out.println("LJD: Doing third job ... ");
        job.doSomething( 2, () -> {

            // Some code executing here ...
            System.out.println("LJD: Third job done.");

            AsyncTracker.get( tname).markLastJobDone( 0 );
        });


        System.out.println("Waiting for last job ...");
        AsyncTracker.get( tname ).waitForLastJobDone();
        System.out.println("Last job done!");

        System.out.println(  );
    }

}
