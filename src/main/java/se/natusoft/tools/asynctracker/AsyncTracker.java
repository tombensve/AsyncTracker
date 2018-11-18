package se.natusoft.tools.asynctracker;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The point of this class is for concurrent jobs to get an instance of this and call
 * newJob() at start of job, save the UUID, and when the job is done call the jobDone(uuid)
 * to mark the job as done.
 *
 * When shutting down the main thread can call waitForActiveJobs() to make sure they all have
 * finished before exiting.
 *
 * Basically this is a simple utility to track concurrent jobs. Instances are gotten by
 * a static getter so that an instance of this is not needed to be passed around. The
 * instance to use can be gotten from anywhere.
 *
 * There are also 4 states of this:
 *
 *     STARTING
 *     RUNNING   - Required for calling newJob().
 *     STOPPING  - Required for calling waitForActiveJobs().
 *     STOPPED   - waitForActiveJobs() will set this state when no jobs are left.
 */
@SuppressWarnings("unused")
public class AsyncTracker {

    /** Holds tracker instances. */
    private static Map<String, AsyncTracker> trackers = new LinkedHashMap<>(  );

    /**
     * Returns a named instance.
     *
     * @param name The name of the instance to get.
     *
     * @return A tracker instance.
     */
    public static AsyncTracker get(String name) {

        AsyncTracker at = trackers.get( name );

        if (at == null) {

            at = new AsyncTracker();
            trackers.put(name, at);
        }

        return at;
    }

    /** The jobs tracked. */
    private Map<UUID, UUID> jobs = Collections.synchronizedMap(new LinkedHashMap<>(  ));

    /** The current state. */
    private State state = State.STOPPED;

    /** Tracks the last job of a series of sequentian jobs. */
    private Instant lastJobDone = null;

    /**
     * Private constructor forcing the get(name) method.
     */
    private AsyncTracker() {}

    /**
     * Changes state of this instance.
     *
     * This actually does nothing other than setting this.state to the passed value!
     * The state value is for user use if wanted it does not affect AsyncTracker functionality.
     *
     * @param state The state to set.
     */
    void setState(State state) {

        this.state = state;
    }

    /**
     * The state value is for user use if wanted it does not affect AsyncTracker functionality.
     *
     * @return The state of this instance.
     */
    State getState() {
        return this.state;
    }

    /**
     * Starts a new job. Keep the returned UUID and pass it to jobDone(uuid) when the job is done.
     *
     * @return A unique job id.
     */
    public UUID newJob() {

        UUID jobId = UUID.randomUUID();
        this.jobs.put( jobId, jobId );
        return jobId;
    }

    /**
     * Marks the job with the specified job id as done.
     *
     * @param jobId The id of the job to mark as done.
     */
    public void jobDone(UUID jobId) {

        this.jobs.remove( jobId );
    }

    /**
     * @return true if there is active jobs.
     */
    @SuppressWarnings("WeakerAccess")
    public boolean hasActiveJobs() {

        return !this.jobs.isEmpty();
    }

    /**
     * This will wait for all active jobs to finnish before returning.
     *
     * This must be called from main thread!
     *
     * @throws IllegalStateException with a wrapped InterruptedException if it occurs.
     */
    public void waitForActiveJobs() {

        while (hasActiveJobs()) {

            try {
                Thread.sleep( 500 );
            }
            catch ( InterruptedException ie ) {
                throw new IllegalStateException( ie.getMessage(), ie );
            }
        }

        this.state = State.STOPPED;
    }

    /**
     * This marks "last job" as done with a timestamp of when. The idea for this is that when there
     * are several consecutive jobs in a thread pool they will be started in order even if executing in
     * parallel and might take different time to finish. This is the reason for the secondsToAdd.
     * "lastJobDone()" will not return true until Instant.now() is greater than "now" of this method +
     * secondsToAdd.
     *
     * Yes, this is a bit of a guess.
     *
     * The idea here is to only track the last job, assuming that is known, and then assume that
     * all previous jobs are also done after the last job is done + extra time.
     *
     * This is of course an alternative to newJob() / jobDone(uuid).
     */
    public void markLastJobDone(int secondsToAdd) {
        this.lastJobDone = Instant.now();
        this.lastJobDone = this.lastJobDone.plus( secondsToAdd, ChronoUnit.SECONDS );
    }

    /**
     * @return true if now has passed markLastJobDone(seconds) time + seconds.
     */
    @SuppressWarnings("WeakerAccess")
    public boolean lastJobDone() {
        return this.lastJobDone != null && this.lastJobDone.isBefore( Instant.now() );
    }

    /**
     * Waits for 'last job done' to be marked as true.
     *
     * @throws IllegalStateException with a wrapped InterruptedException if it occurs.
     */
    public void waitForLastJobDone()  {
        while(!lastJobDone()) {
            try {
                Thread.sleep( 500 );
            }
            catch ( InterruptedException ie ) {
                throw new IllegalStateException( ie.getMessage(), ie );
            }
        }
    }

    public synchronized void start() {
        this.state = State.STARTING;
    }

    public synchronized void started() {
        this.state = State.RUNNING;
    }

    public synchronized void stop() {
        this.state = State.STOPPING;
    }

    public synchronized void stopped() {
        this.state = State.STOPPED;
    }
}
