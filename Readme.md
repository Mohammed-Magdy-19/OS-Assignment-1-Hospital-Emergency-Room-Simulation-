# Hospital Emergency Room Simulation (CS241/SCS241 – Assignment 1)

A multithreaded Java simulation of an ER: patients (producers) fill a bounded waiting
room, doctors (consumers) take patients from it and must acquire one of **2** treatment
rooms (within 3 s) before treating. Built only with our own `Semaphore` class
(no `java.util.concurrent`).

## Build and run

```bash
# from the project root (the folder that contains src/)
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out hospital.Hospital
```

Input (asked on startup): waiting-room capacity, number of doctors, number of patients
(each must be a whole number >= 1; invalid input is re-asked).

Example: `2 / 3 / 5` – see `tests/sample_output.txt` for a full run.

## Project structure

```
src/hospital/
├── Hospital.java              entry point: input, creates threads, shutdown
├── sync/Semaphore.java        counting semaphore: acquire / release / tryAcquire(ms)
├── resources/WaitingRooms.java    bounded buffer (empty / full / mutex semaphores)
├── resources/TreatmentRooms.java  Semaphore(2) + 3 s timed acquire
├── Actors/Patient.java        producer thread
├── Actors/Doctor.java         consumer thread
└── util/Logger.java           synchronized (atomic) printing
```

## Design

| Shared resource | Synchronization |
|---|---|
| Waiting room (bounded buffer, capacity from user) | `empty` = capacity, `full` = 0, `mutex` = 1. Producer: `empty -> mutex -> add -> mutex -> full`. Consumer: `full -> mutex -> poll -> mutex -> empty`. Always `empty`/`full` before `mutex`. |
| Treatment rooms (2) | One `Semaphore(2)`, independent from the waiting room. Doctors use `tryAcquire(3000)`; on `false` the doctor sends the patient back through `enterWaitingRoom`. |
| Console | `Logger.log` is `synchronized`, so each line (or multi-line block) is atomic. |

* **Semaphore** – `synchronized` methods, `wait()` inside `while (permits == 0)`
  (protects against spurious wakeups), `notifyAll()` in `release()`. `tryAcquire`
  uses an absolute deadline, so wakeups cannot extend the timeout. Blocking is done by
  `wait()`, never by spinning.
* **Full waiting room** – detected with `empty.tryAcquire(0)` (not `queue.size()`); the
  patient logs "waiting outside" and then sleeps on `empty.acquire()`.
* **Patient ↔ doctor hand-off** – each patient blocks on its own `treatedSignal`
  semaphore until its doctor calls `setTreated()`.
* **No deadlock** – a doctor never holds the waiting-room mutex while waiting for a
  treatment room; the room is always released in a `finally`; a doctor only times out
  when both rooms are held by treating doctors, who always finish and then free a seat.

### Termination (Hospital.java)

1. `join()` every patient. A patient thread only ends after being treated, so when all
   joins return every patient has been treated and left.
2. `waitingRooms.close(numberOfDoctors)` releases `full` once per doctor (poison pills).
   The queue is empty, so each idle doctor receives `null` from `takePatient()` and stops.
3. `join()` every doctor, then print `All patients have been treated.` and
   `Simulation completed.`

There is no polling: the main thread only blocks in `join()` and in `Scanner` input.

## Testing

`python3 tests/run_tests.py 20 out` runs each team scenario 20 times (runs in parallel
on a 1-core machine, which stresses scheduling) and checks every run's log for:
exit code 0 and final line `Simulation completed.`, every patient arrived / began
treatment / finished / left **exactly once**, never more than 2 rooms in use, waiting
room never above capacity, both empty at the end, no hangs (400 s limit), no stderr.

| Scenario | cap/doctors/patients | runs | passed | hangs | "could not acquire" events |
|---|---|---|---|---|---|
| Sample | 2/3/5 | 20 | 20 | 0 | 8 |
| Minimum buffer | 1/1/5 | 20 | 20 | 0 | 0 |
| More doctors than patients | 2/6/3 | 20 | 20 | 0 | 7 |
| Stress | 2/3/50 | 20 | 20 | 0 | 109 |
| Timeout-heavy | 3/5/20 | 20 | 20 | 0 | 223 |

**Idle CPU:** total CPU time of all JVMs vs. their wall time was 0.5%–2.4%
(almost all of it JVM start-up, about 0.2 s CPU per run), so there is no busy waiting.
Full table: `tests/test_results.md`.

## Notes

* Patient arrival order in the log is decided by the OS scheduler (e.g. `P2 arrived`
  can print before `P1 arrived`); the assignment states the sample is only one possible scenario.
* Doctors also print `Doctor N is stopping.` during shutdown (extra line, not an event of a patient).

## Team

| Member |ID| Files |
|---|---|---|
| Mohamed Magdy| 20240524 | Semaphore.java, Logger.java |
| Mohamed Atef | 20240809 | WaitingRooms.java |
| Adham Megahed | 20240072 | Doctor.java |
| Arwa Atef | 20240808 | TreatmentRooms.java, Patient.java |
| Mahmoud Khaled | 20240550 | Hospital.java, README.md, test runs |
