/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package worker;

import common.BootstrapRemote;
import common.WorkerInfo;
import common.WorkerRemote;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.List;
import java.util.Random;

public class WorkerNode {

    public static void main(String[] args)
            throws Exception {

        if (args.length < 2) {

            System.out.println(
                    "Usage:"
            );

            System.out.println(
                    "java worker.WorkerNode <ID> <PORT>"
            );

            return;
        }

        int id =
                Integer.parseInt(args[0]);

        int port =
                Integer.parseInt(args[1]);

        System.setProperty(
                "java.rmi.server.hostname",
                "127.0.0.1"
        );

        // ------------------------------------------------
        // Connect to Bootstrap
        // ------------------------------------------------

        Registry bootstrapRegistry =
                LocateRegistry.getRegistry(
                        "127.0.0.1",
                        1099
                );

        BootstrapRemote bootstrap =
                (BootstrapRemote)
                        bootstrapRegistry.lookup(
                                "BootstrapService"
                        );

        // ------------------------------------------------
        // Create Worker
        // ------------------------------------------------

        WorkerInfo info =
                new WorkerInfo(
                        id,
                        "127.0.0.1",
                        port,
                        0
                );

        WorkerService service =
                new WorkerService(
                        info,
                        bootstrap
                );

        // ------------------------------------------------
        // Create worker RMI registry
        // ------------------------------------------------

        Registry workerRegistry =
                LocateRegistry.createRegistry(
                        port
                );

        workerRegistry.rebind(
                "WorkerService",
                service
        );

        // ------------------------------------------------
        // Register with Bootstrap
        // ------------------------------------------------

        boolean registered =
                bootstrap.registerWorker(info);

        if (!registered) {

            System.out.println(
                    "Worker ID already exists."
            );

            return;
        }

        System.out.println();
        System.out.println(
                "================================="
        );

        System.out.println(
                "Worker " +
                id +
                " is running."
        );

        System.out.println(
                "Port: " +
                port
        );

        System.out.println(
                "================================="
        );

        // ------------------------------------------------
        // Wait for other workers
        // ------------------------------------------------

        Thread.sleep(1000);

        List<WorkerInfo> active =
                bootstrap.getActiveWorkers();

        if (active.size() > 1) {

            WorkerInfo selected =
                    null;

            // Randomly select another worker
            for (WorkerInfo worker :
                    active) {

                if (worker.id != id) {

                    selected = worker;
                    break;
                }
            }

            if (selected != null) {

                service.addNeighbour(
                        selected
                );

                try {

                    Registry otherRegistry =
                            LocateRegistry.getRegistry(
                                    selected.host,
                                    selected.port
                            );

                    WorkerRemote other =
                            (WorkerRemote)
                                    otherRegistry.lookup(
                                            "WorkerService"
                                    );

                    // Connect selected worker
                    // back to this worker
                    other.addNeighbour(info);

                } catch (Exception e) {

                    System.out.println(
                            "Could not create reverse connection."
                    );
                }
            }
        }

        // ------------------------------------------------
        // Start election manually
        // ------------------------------------------------

        System.out.println();

        System.out.println(
                "Worker " +
                id +
                " ready."
        );

        System.out.println(
                "Press ENTER to start election."
        );

        System.in.read();

        service.startElection();

        // ------------------------------------------------
        // Shutdown hook
        // ------------------------------------------------

        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(() -> {

                            try {

                                bootstrap.unregisterWorker(
                                        id
                                );

                                service.shutdown();

                            } catch (Exception ignored) {
                            }
                        })
                );

        // ------------------------------------------------
        // Keep worker alive
        // ------------------------------------------------

        while (true) {

            Thread.sleep(10000);
        }
    }
}
