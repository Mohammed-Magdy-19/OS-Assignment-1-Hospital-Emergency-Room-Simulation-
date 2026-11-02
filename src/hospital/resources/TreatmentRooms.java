package hospital.resources;

import hospital.sync.Semaphore;
import hospital.util.Logger;

public class TreatmentRooms {
    private final Semaphore roomsSemaphore;
    private final int totalRooms;

    public TreatmentRooms(int totalRooms) {
        this.totalRooms = totalRooms;
        this.roomsSemaphore = new Semaphore(totalRooms);
    }

    public boolean acquireRoom(long timeoutMs) throws InterruptedException {
        return roomsSemaphore.tryAcquire(timeoutMs);
    }

    public void releaseRoom() {
        roomsSemaphore.release();
    }

    public int getTotalRooms() {
        return totalRooms;
    }
}