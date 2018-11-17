# AsyncTracker

Utility for tracking async threaded jobs. This to be able to wait for unfinished jobs before shutting down.

This is trivially easy. newJob() returns an UUID, which is also stored in a map. jobDone(uuid) removes the UUID.
waitForActiveJobs() just waits for the map to become empty.

Example:

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
