package hospital.resources;

import hospital.Actors.Patient;
import hospital.sync.Semaphore;
import hospital.util.Logger;
import java.util.LinkedList;
import java.util.Queue;

public class WaitingRooms {
    private final int capacity;
    private final Queue<Patient> queue = new LinkedList<>();

    private final Semaphore empty;                      
    private final Semaphore full  = new Semaphore(0);   
    private final Semaphore mutex = new Semaphore(1);   

    public WaitingRooms(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be >= 1");
        }
        this.capacity = capacity;
        this.empty = new Semaphore(capacity);
    }

    public void enterWaitingRoom(Patient patient) throws InterruptedException {
        if (!empty.tryAcquire(0)) {
            Logger.log("The waiting room is full.",
                       patient.getPatientName() + " is waiting outside for an available seat.");
            empty.acquire();
        }

        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            empty.release();   
            throw e;
        }

        try {
            queue.add(patient);
            Logger.log(patient.getPatientName() + " entered the waiting room.");
        } finally {
            mutex.release();
        }

        full.release();
    }

    public Patient takePatient(int doctorId) throws InterruptedException {
        full.acquire();

        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            full.release();   
            throw e;
        }

        Patient patient;
        try {
            patient = queue.poll();
            if (patient != null) {
                Logger.log("Doctor " + doctorId + " is assigned to " + patient.getPatientName() + ".");
            }
        } finally {
            mutex.release();
        }

        if (patient == null) {
            return null;      
        }

        empty.release();       
        return patient;
    }

    public void close(int numberOfDoctors) {
        for (int i = 0; i < numberOfDoctors; i++) {
            full.release();
        }
    }

    public int getCapacity() {
        return capacity;
    }
}