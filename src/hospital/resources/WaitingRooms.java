package hospital.resources;

import hospital.sync.Semaphore;
import hospital.util.Logger;

public class WaitingRooms {
    private final Semaphore seatsSemaphore;
    private final int capacity;
    private int currentOccupancy = 0;
    private final Object lock = new Object();

    public WaitingRooms(int capacity) {
        this.capacity = capacity;
        this.seatsSemaphore = new Semaphore(capacity);
    }

    public void enterWaitingRoom(String patientName) throws InterruptedException {
        synchronized (lock) {
            if (currentOccupancy >= capacity) {
                Logger.log("The waiting room is full.");
                Logger.log(patientName + " is waiting outside for an available seat.");
            }
        }

        seatsSemaphore.acquire();

        synchronized (lock) {
            currentOccupancy++;
        }

        Logger.log(patientName + " entered the waiting room.");
    }

    public void leaveWaitingRoom() {
        synchronized (lock) {
            currentOccupancy--;
        }
        seatsSemaphore.release(); 
    }

    public int getCapacity() {
        return capacity;
    }
}