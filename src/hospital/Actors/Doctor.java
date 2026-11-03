package hospital.Actors;
import hospital.resources.WaitingRooms;
import hospital.resources.TreatmentRooms;
import hospital.util.Logger;

public class Doctor extends Thread {

    private final int doctorId;
    private final WaitingRooms waitingRooms;
    private final TreatmentRooms treatmentRooms;

    public Doctor(int id, WaitingRooms w, TreatmentRooms r) {
        this.doctorId = id;
        this.waitingRooms = w;
        this.treatmentRooms = r;
    }

    @Override
    public void run() {

        while (true) {

            try {
                Patient patient = waitingRooms.takePatient(doctorId);

                if (patient == null) {
                    Logger.log("Doctor " + doctorId + " is stopping.");
                    break;
                }

                Logger.log("Doctor " + doctorId + " is assigned to "+ patient.getPatientName() + ".");

                Logger.log("Doctor " + doctorId + " is trying to acquire a treatment room.");

                boolean acquired = treatmentRooms.acquireRoom(3000);

                if (!acquired) {

                    Logger.log("Doctor " + doctorId + " could not acquire a treatment room.");

                    Logger.log("Doctor " + doctorId + " is sending " + patient.getPatientName()+ " back to the waiting room.");

                    waitingRooms.enterWaitingRoom(patient);

                    Logger.log("Doctor " + doctorId + " is now available.");

                    continue;
                }

                Logger.log("Doctor " + doctorId + " acquired a treatment room.");

                Logger.log("Doctor " + doctorId+ " begins treating " + patient.getPatientName() + ".");

                try {
                    int treatmentTime = 1000 + (int) (Math.random() * 4000);

                    Thread.sleep(treatmentTime);

                    Logger.log("Doctor " + doctorId + " finished treating "+ patient.getPatientName() + ".");

                } finally {
                    treatmentRooms.releaseRoom();
                }

                patient.setTreated();

                Logger.log("Doctor " + doctorId + " is now available.");

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                Logger.log("Doctor " + doctorId + " was interrupted.");

                break;
            }
        }
    }
}
