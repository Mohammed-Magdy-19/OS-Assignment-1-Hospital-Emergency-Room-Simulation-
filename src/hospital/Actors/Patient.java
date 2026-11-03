package hospital.Actors;

import hospital.resources.WaitingRooms;
import hospital.util.Logger;

public class Patient extends Thread {
    private final String patientName;
    private final WaitingRooms waitingRooms;
    private boolean treated = false;

    public Patient(String name, WaitingRooms waitingRooms) {
        super(name);
        this.patientName = name;
        this.waitingRooms = waitingRooms;
    }

    @Override
    public void run() {
        try {
            Logger.log(patientName + " arrived.");

            waitingRooms.enterWaitingRoom(patientName);

            synchronized (this) {
                while (!treated) {
                    wait();
                }
            }

            Logger.log(patientName + " left.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.log(patientName + " was interrupted.");
        }
    }

    public synchronized void setTreated() {
        this.treated = true;
        notify();
    }

    public String getPatientName() {
        return patientName;
    }
}