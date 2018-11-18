# AsyncTracker

Utility for tracking async threaded jobs. This to be able to wait for unfinished jobs before shutting down.

This is trivially easy. newJob() returns an UUID, which is also stored in a map. jobDone(uuid) removes the UUID.
waitForActiveJobs() just waits for the map to become empty.

Why ? Becuase there is asynchronous frameworks out there that just pull the capet out under your feet when shuting down, not waiting for running jobs to finish first.

## Active jobs example

    something.forEach((someEntry) -> {

        UUID jobId = AsyncTracker.get("mytracker").newJob();
        someAsyncCall(someEntry, (res) -> {
                ...

                AsyncTracker.get("mytracker").jobDone(jobId);
            }
        );
    });

    ...

    AsyncTracker.get("mytracker").waitForActiveJobs();

    shutdown();

Since waitForActiveJobs() does Thread.sleep(...) it should preferably only be called from the "main" thread or a thread where it is safe to block.

## Last job example

This is an alternative and only tracks the last job in the case you know what is the last job. Since there is the possibility of earlier jobs taking longer time to complete than the last job, the number of seconds to add to "now" is passed to `...markLastJobDone(sec)`. How much of a "guesstimate" this is, is up to you.

    someJob.doSomething(What ever, (res) -> {

        // Some code executing here ...

    });

    ...

    someOtherJob.doSomething(What ever, () -> {

        // Some code executing here ...

        AsyncTracker.get( "mytracker" ).markLastJobDone( 3 );
    });

    ...

    AsyncTracker.get( "mytracker" ).waitForLastJobDone();

    shutdown();

waitForLastJobDone(sec) also does a Thread.sleep(...) ...