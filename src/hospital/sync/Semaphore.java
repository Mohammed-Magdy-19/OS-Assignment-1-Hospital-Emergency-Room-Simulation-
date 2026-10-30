package hospital.sync;

class Semaphore {

    private int permits;

    public Semaphore(int initial_permits) {
        if (initial_permits < 0) {
            throw new IllegalArgumentException("initial permits must be >= 0");
        }

        this.permits = initial_permits;
    }

    /** Wait (P): blocks until a permit is available, then takes it. */
    public synchronized void acquire() throws InterruptedException {
        // 'while', never 'if': protects against spurious wakeups and stolen permits
        while (permits == 0) {
            wait();
        }

        permits--;
    }

    /** Signal (V): adds a permit and wakes waiting threads. */
    public synchronized void release() {
        permits++;
        // notifyAll because normal and timed waiters share this monitor;
        // each woken thread rechecks 'permits' in its while loop.
        notifyAll();
    }

    /**
     * Tries to take a permit within the given time limit.
     *
     * @param timeoutMs maximum time to wait in milliseconds (0 = single
     *                  non-blocking check)
     * @return true if a permit was acquired, false if the time ran out
     */

    public synchronized boolean tryAcquire(long timeoutMs) throws InterruptedException {
        // Absolute deadline: a stolen wakeup or spurious wakeup cannot extend the timeout.
        final long deadlineNs = System.nanoTime() + timeoutMs * 1_000_000L;

        while (permits == 0) {
            long remainingNs = deadlineNs - System.nanoTime();
            if (remainingNs <= 0) {
                return false; // timed out, no spinning
            }
            long remainingMs = remainingNs / 1_000_000L;
            int extraNs = (int) (remainingNs % 1_000_000L);
            wait(remainingMs, extraNs); // thread sleeps here
        }
        permits--;
        return true;
    }

}