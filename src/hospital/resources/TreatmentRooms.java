package hospital.resources;

import hospital.sync.Semaphore;
import hospital.util.Logger;

public class TreatmentRooms {
    private static final int TOTAL_ROOMS = 2;
    private static final long TIMEOUT_MS = 3000;

    private final Semaphore roomsSemaphore = new Semaphore(TOTAL_ROOMS);

    public boolean acquireRoom() throws InterruptedException {
        return roomsSemaphore.tryAcquire(TIMEOUT_MS);
    }

    public void releaseRoom(int doctorId) {
        Logger.log("Doctor " + doctorId + " released the treatment room.");
        roomsSemaphore.release();
    }

    public int getTotalRooms() {
        return TOTAL_ROOMS;
    }
}