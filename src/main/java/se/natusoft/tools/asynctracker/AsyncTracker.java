package se.natusoft.tools.asynctracker;

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

    private State state = State.STOPPED;

    /**
     * Private constructor forcing the get(name) method.
     */
    private AsyncTracker() {}

    /**
     * Changes state of this instance.
     *
     * @param state The state to set.
     */
    void setState(State state) {

        this.state = state;
    }

    /**
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

        if (this.state != State.RUNNING)
            throw new IllegalStateException( "Instance must be running for jobs to be created!" );

        if (Thread.currentThread().getName().equals( "main" ))
            throw new IllegalStateException( "This should not be called from 'main' thread!" );

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

        if (Thread.currentThread().getName().equals( "main" ))
            throw new IllegalStateException( "This should not be called from 'main' thread!" );

        if (this.state == State.STOPPED || this.state == State.STARTING)
            throw new IllegalStateException( "Can't end a job when not running!" );

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
     * @throws Exception If called from wrong place.
     */
    public void waitForActiveJobs() throws Exception {

        if (this.state != State.STOPPING) throw new IllegalStateException( "Instance must be stopping for active jobs to be waited for!" );

        if (!Thread.currentThread().getName().equals("main")) {

            throw new IllegalStateException( "This method can only be called from 'main' thread!" );
        }

        while (hasActiveJobs()) {

            Thread.sleep( 500 );
        }

        this.state = State.STOPPED;
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
