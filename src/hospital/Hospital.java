package hospital;

import hospital.Actors.Doctor;
import hospital.Actors.Patient;
import hospital.resources.TreatmentRooms;
import hospital.resources.WaitingRooms;
import hospital.util.Logger;

import java.util.Scanner;

public class Hospital {

    public static void main(String[] args) {
        int capacity;
        int numDoctors;
        int numPatients;

        try (Scanner in = new Scanner(System.in)) {
            capacity    = readPositiveInt(in, "Waiting room capacity: ");
            numDoctors  = readPositiveInt(in, "Number of doctors: ");
            numPatients = readPositiveInt(in, "Number of patients: ");
        }

        WaitingRooms waitingRooms = new WaitingRooms(capacity);
        TreatmentRooms treatmentRooms = new TreatmentRooms();

        Doctor[] doctors = new Doctor[numDoctors];
        for (int i = 0; i < numDoctors; i++) {
            doctors[i] = new Doctor(i + 1, waitingRooms, treatmentRooms);
        }

        Patient[] patients = new Patient[numPatients];
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < numPatients; i++) {
            patients[i] = new Patient("P" + (i + 1), waitingRooms);
            if (i > 0) names.append(' ');
            names.append(patients[i].getPatientName());
        }

        Logger.log("", "Patients arriving:", names.toString());

        // Doctors go first. They sit idle and wait for a patient to show up.
        for (Doctor d : doctors) d.start();
        for (Patient p : patients) p.start();

        try {
            // 1. Wait (blocked, not polling) for every patient to be treated and leave.
            for (Patient p : patients) p.join();

            // 2. Poison pills: one per doctor, delivered through the waiting room.
            waitingRooms.close(numDoctors);

            // 3. Wait for every doctor to stop.
            for (Doctor d : doctors) d.join();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.log("Simulation interrupted.");
            return;
        }

        Logger.log("All patients have been treated.", "Simulation completed.");
    }

    /** Re-asks until the user types an integer >= 1. Blocks on input, never spins. */
    private static int readPositiveInt(Scanner in, String prompt) {
        while (true) {
            System.out.print(prompt);
            System.out.flush();
            if (!in.hasNext()) {                       // input closed: fail instead of looping
                throw new IllegalStateException("No more input available.");
            }
            if (in.hasNextInt()) {
                int v = in.nextInt();
                if (v >= 1) return v;
            } else {
                in.next();                             // discard the bad token
            }
            System.out.println("Please enter a whole number >= 1.");
        }
    }
}
