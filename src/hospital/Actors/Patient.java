package hospital.Actors;

import hospital.resources.WaitingRooms;
import hospital.sync.Semaphore;
import hospital.util.Logger;

public class Patient extends Thread {
    private final WaitingRooms waitingRooms;
    private final Semaphore treatedSignal = new Semaphore(0);

    public Patient(String name, WaitingRooms waitingRooms) {
        super(name);             
        this.waitingRooms = waitingRooms;
    }

    @Override
    public void run() {
        try {
            Logger.log(getPatientName() + " arrived.");

            waitingRooms.enterWaitingRoom(this);   

            treatedSignal.acquire();               

            Logger.log(getPatientName() + " left.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.log(getPatientName() + " was interrupted.");
        }
    }

    public void setTreated() {
        treatedSignal.release();
    }

    public String getPatientName() {
        return getName();
    }
}