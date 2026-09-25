/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package worker;

import common.*;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkerService
        extends UnicastRemoteObject
        implements WorkerRemote, CoordinatorRemote {

    // Required by the assignment
    public final String leaderman = "cs324";

    private final WorkerInfo workerInfo;
    private final BootstrapRemote bootstrap;

    private final AtomicInteger jac;
    private final AtomicInteger jobs;

    private volatile int coordinatorId = -1;

    private final Map<Integer, WorkerRemote> neighbours;

    private final java.util.Set<String> elections;
    private final java.util.Set<String> coordinators;

    private final ExecutorService pool;

    public WorkerService(
            WorkerInfo info,
            BootstrapRemote bootstrap)
            throws RemoteException {

        super();

        this.workerInfo = info;
        this.bootstrap = bootstrap;

        this.jac = new AtomicInteger(info.jac);
        this.jobs = new AtomicInteger(0);

        this.neighbours =
                new ConcurrentHashMap<>();

        this.elections =
                ConcurrentHashMap.newKeySet();

        this.coordinators =
                ConcurrentHashMap.newKeySet();

        this.pool =
                Executors.newFixedThreadPool(5);
    }

    // --------------------------------------------------
    // Worker information
    // --------------------------------------------------

    @Override
    public WorkerInfo getWorkerInfo()
            throws RemoteException {

        return new WorkerInfo(
                workerInfo.id,
                workerInfo.host,
                workerInfo.port,
                jac.get()
        );
    }

    // --------------------------------------------------
    // Add neighbour
    // --------------------------------------------------

    @Override
    public void addNeighbour(
            WorkerInfo worker)
            throws RemoteException {

        if (worker.id == workerInfo.id) {
            return;
        }

        try {

            Registry registry =
                    LocateRegistry.getRegistry(
                            worker.host,
                            worker.port
                    );

            WorkerRemote remote =
                    (WorkerRemote)
                            registry.lookup(
                                    "WorkerService"
                            );

            neighbours.put(
                    worker.id,
                    remote
            );

            System.out.println(
                    "Worker " +
                    workerInfo.id +
                    " connected to Worker " +
                    worker.id
            );

        } catch (Exception e) {

            System.out.println(
                    "Worker " +
                    workerInfo.id +
                    " could not connect to Worker " +
                    worker.id
            );
        }
    }

    // --------------------------------------------------
    // Start election
    // --------------------------------------------------

    public synchronized void startElection() {

        String electionId =
                UUID.randomUUID().toString();

        elections.add(electionId);

        coordinatorId = -1;
        jobs.set(0);

        System.out.println();
        System.out.println(
                "Worker " +
                workerInfo.id +
                " STARTING ELECTION"
        );

        System.out.println(
                "Election ID: " +
                electionId
        );

        forwardElection(
                electionId,
                -1
        );

        Thread electionThread =
                new Thread(() -> {

                    try {

                        Thread.sleep(1500);

                        electLeader(
                                electionId
                        );

                    } catch (InterruptedException e) {

                        Thread.currentThread().interrupt();
                    }
                });

        electionThread.start();
    }

    // --------------------------------------------------
    // Forward ELECTION message
    // --------------------------------------------------

    private void forwardElection(
            String electionId,
            int sender) {

        for (Map.Entry<Integer, WorkerRemote> entry
                : neighbours.entrySet()) {

            int neighbourId =
                    entry.getKey();

            if (neighbourId == sender) {
                continue;
            }

            try {

                entry.getValue()
                        .receiveElection(
                                electionId,
                                workerInfo.id
                        );

            } catch (Exception e) {

                System.out.println(
                        "Could not send election to Worker "
                        + neighbourId
                );
            }
        }
    }

    // --------------------------------------------------
    // Receive ELECTION
    // --------------------------------------------------

    @Override
    public void receiveElection(
            String electionId,
            int sender)
            throws RemoteException {

        // Prevent duplicate processing
        if (!elections.add(electionId)) {
            return;
        }

        System.out.println(
                "Worker " +
                workerInfo.id +
                " received ELECTION"
        );

        forwardElection(
                electionId,
                sender
        );
    }

    // --------------------------------------------------
    // Select coordinator
    // --------------------------------------------------

    private void electLeader(
            String electionId) {

        try {

            List<WorkerInfo> active =
                    bootstrap.getActiveWorkers();

            if (active == null ||
                    active.isEmpty()) {

                System.out.println(
                        "No active workers."
                );

                return;
            }

            WorkerInfo leader =
                    active.get(0);

            for (WorkerInfo worker : active) {

                // Lowest JAC wins
                if (worker.jac < leader.jac) {

                    leader = worker;

                }

                // If JAC is equal,
                // highest ID wins
                else if (
                        worker.jac == leader.jac &&
                        worker.id > leader.id) {

                    leader = worker;
                }
            }

            coordinatorId =
                    leader.id;

            System.out.println();
            System.out.println(
                    "================================="
            );
            System.out.println(
                    "ELECTION RESULT"
            );
            System.out.println(
                    "Coordinator = Worker " +
                    leader.id
            );
            System.out.println(
                    "JAC = " +
                    leader.jac
            );
            System.out.println(
                    "================================="
            );

            broadcastCoordinator(
                    electionId,
                    leader.id,
                    -1
            );

        } catch (Exception e) {

            System.out.println(
                    "Election error:"
            );

            e.printStackTrace();
        }
    }

    // --------------------------------------------------
    // Broadcast coordinator
    // --------------------------------------------------

    private void broadcastCoordinator(
            String electionId,
            int leader,
            int sender) {

        String key =
                electionId +
                "-" +
                leader +
                "-" +
                workerInfo.id;

        if (!coordinators.add(key)) {
            return;
        }

        for (Map.Entry<Integer, WorkerRemote> entry
                : neighbours.entrySet()) {

            int neighbourId =
                    entry.getKey();

            if (neighbourId == sender) {
                continue;
            }

            try {

                entry.getValue()
                        .receiveCoordinator(
                                electionId,
                                leader
                        );

            } catch (Exception e) {

                System.out.println(
                        "Could not broadcast coordinator."
                );
            }
        }
    }

    // --------------------------------------------------
    // Receive coordinator
    // --------------------------------------------------

    @Override
    public void receiveCoordinator(
            String electionId,
            int leader)
            throws RemoteException {

        coordinatorId = leader;

        System.out.println(
                "Worker " +
                workerInfo.id +
                " coordinator = Worker " +
                leader
        );

        broadcastCoordinator(
                electionId,
                leader,
                -1
        );
    }

    // --------------------------------------------------
    // Submit job to coordinator
    // --------------------------------------------------

    @Override
    public JobResult submitJob(
            Job job)
            throws RemoteException {

        if (coordinatorId != workerInfo.id) {

            throw new RemoteException(
                    "Worker " +
                    workerInfo.id +
                    " is not the coordinator. " +
                    "Current coordinator = Worker " +
                    coordinatorId
            );
        }

        int jobNumber =
                jobs.incrementAndGet();

        if (jobNumber > 5) {

            jobs.decrementAndGet();

            System.out.println(
                    "Term ended after 5 jobs."
            );

            startElection();

            throw new RemoteException(
                    "Term ended after 5 jobs. " +
                    "New election started."
            );
        }

        // JAC increases when coordinator
        // assigns a job
        int newJac =
                jac.incrementAndGet();

        System.out.println();
        System.out.println(
                "Coordinator Worker " +
                workerInfo.id +
                " assigned Job " +
                job.jobId
        );

        System.out.println(
                "JAC = " +
                newJac
        );

        try {

            bootstrap.updateWorker(
                    getWorkerInfo()
            );

        } catch (Exception e) {

            System.out.println(
                    "Could not update JAC at Bootstrap."
            );
        }

        JobResult result =
                distributeJob(job);

        // Automatically start new election
        // after the 5th assigned job
        if (jobNumber == 5) {

            Thread electionThread =
                    new Thread(() -> {

                        try {

                            Thread.sleep(500);

                        } catch (InterruptedException e) {

                            Thread.currentThread()
                                    .interrupt();
                        }

                        System.out.println();
                        System.out.println(
                                "5 jobs completed."
                        );

                        System.out.println(
                                "Starting new election."
                        );

                        startElection();
                    });

            electionThread.start();
        }

        return result;
    }

    // --------------------------------------------------
    // Distribute job
    // --------------------------------------------------

    private JobResult distributeJob(
            Job job) {

        try {

            List<WorkerInfo> active =
                    bootstrap.getActiveWorkers();

            if (active == null ||
                    active.isEmpty()) {

                return executeLocal(job);
            }

            List<WorkerInfo> available =
                    new ArrayList<>();

            for (WorkerInfo worker : active) {

                available.add(worker);
            }

            List<Job> parts =
                    splitJob(
                            job,
                            available.size()
                    );

            List<Future<JobResult>> futures =
                    new ArrayList<>();

            for (int i = 0;
                    i < parts.size();
                    i++) {

                Job part =
                        parts.get(i);

                WorkerInfo worker =
                        available.get(i);

                WorkerRemote remote =
                        null;

                if (worker.id == workerInfo.id) {

                    final Job localPart = part;

                    futures.add(
                            pool.submit(
                                    () -> executeJobCalculation(
                                            localPart
                                    )
                            )
                    );

                } else {

                    try {

                        Registry registry =
                                LocateRegistry.getRegistry(
                                        worker.host,
                                        worker.port
                                );

                        remote =
                                (WorkerRemote)
                                        registry.lookup(
                                                "WorkerService"
                                        );

                        final WorkerRemote target =
                                remote;

                        futures.add(
                                pool.submit(
                                        () -> target.executeJob(part)
                                )
                        );

                    } catch (Exception e) {

                        System.out.println(
                                "Could not contact Worker " +
                                worker.id
                        );
                    }
                }
            }

            long finalResult;

            if (job.type == JobType.MAX) {

                finalResult =
                        Long.MIN_VALUE;

                for (Future<JobResult> future :
                        futures) {

                    JobResult r =
                            future.get();

                    if (r.result > finalResult) {
                        finalResult = r.result;
                    }
                }

            } else {

                finalResult = 0;

                for (Future<JobResult> future :
                        futures) {

                    JobResult r =
                            future.get();

                    finalResult +=
                            r.result;
                }
            }

            return new JobResult(
                    job.jobId,
                    finalResult
            );

        } catch (Exception e) {

            return new JobResult(
                    job.jobId,
                    -1
            );
        }
    }

    // --------------------------------------------------
    // Split workload
    // --------------------------------------------------

    private List<Job> splitJob(
            Job job,
            int numberOfWorkers) {

        List<Job> parts =
                new ArrayList<>();

        if (numberOfWorkers <= 0) {
            numberOfWorkers = 1;
        }

        if (job.type == JobType.PRIMESUM) {

            int total =
                    job.end - job.start + 1;

            int size =
                    (int) Math.ceil(
                            (double) total /
                            numberOfWorkers
                    );

            int current =
                    job.start;

            while (current <= job.end) {

                int partEnd =
                        Math.min(
                                current + size - 1,
                                job.end
                        );

                parts.add(
                        new Job(
                                JobType.PRIMESUM,
                                current,
                                partEnd
                        )
                );

                current =
                        partEnd + 1;
            }

        } else {

            int[] numbers =
                    job.numbers;

            if (numbers == null ||
                    numbers.length == 0) {

                parts.add(
                        new Job(
                                job.type,
                                new int[0]
                        )
                );

                return parts;
            }

            int size =
                    (int) Math.ceil(
                            (double) numbers.length /
                            numberOfWorkers
                    );

            int index = 0;

            while (index < numbers.length) {

                int end =
                        Math.min(
                                index + size,
                                numbers.length
                        );

                int[] part =
                        new int[end - index];

                for (int i = index;
                        i < end;
                        i++) {

                    part[i - index] =
                            numbers[i];
                }

                parts.add(
                        new Job(
                                job.type,
                                part
                        )
                );

                index = end;
            }
        }

        return parts;
    }

    // --------------------------------------------------
    // Execute job remotely
    // --------------------------------------------------

    @Override
    public JobResult executeJob(
            Job job)
            throws RemoteException {

        Future<JobResult> future =
                pool.submit(
                        () -> executeJobCalculation(job)
                );

        try {

            return future.get();

        } catch (Exception e) {

            throw new RemoteException(
                    "Job execution failed.",
                    e
            );
        }
    }

    // --------------------------------------------------
    // Calculate job
    // --------------------------------------------------

    private JobResult executeJobCalculation(
            Job job) {

        long result = 0;

        if (job.type == JobType.MAX) {

            result =
                    Integer.MIN_VALUE;

            if (job.numbers != null) {

                for (int number :
                        job.numbers) {

                    if (number > result) {
                        result = number;
                    }
                }
            }

        } else if (
                job.type == JobType.PRIMESUM) {

            result = 0;

            for (int number =
                    job.start;
                    number <= job.end;
                    number++) {

                if (isPrime(number)) {

                    result += number;
                }
            }

        } else if (
                job.type == JobType.PRIMECOUNT) {

            result = 0;

            if (job.numbers != null) {

                for (int number :
                        job.numbers) {

                    if (isPrime(number)) {

                        result++;
                    }
                }
            }
        }

        return new JobResult(
                job.jobId,
                result
        );
    }

    // --------------------------------------------------
    // Prime test
    // --------------------------------------------------

    private boolean isPrime(int number) {

        if (number < 2) {
            return false;
        }

        if (number == 2) {
            return true;
        }

        if (number % 2 == 0) {
            return false;
        }

        for (int i = 3;
                i * i <= number;
                i += 2) {

            if (number % i == 0) {
                return false;
            }
        }

        return true;
    }

    // --------------------------------------------------
    // Local execution
    // --------------------------------------------------

    private JobResult executeLocal(
            Job job) {

        return executeJobCalculation(job);
    }

    // --------------------------------------------------
    // Shutdown
    // --------------------------------------------------

    public void shutdown() {

        pool.shutdownNow();
    }
}