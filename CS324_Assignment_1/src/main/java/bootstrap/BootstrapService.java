/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bootstrap;

import common.BootstrapRemote;
import common.WorkerInfo;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class BootstrapService
        extends UnicastRemoteObject
        implements BootstrapRemote {

    private final List<WorkerInfo> workers;

    public BootstrapService() throws RemoteException {
        super();
        workers = new ArrayList<>();
    }

    @Override
    public synchronized boolean registerWorker(
            WorkerInfo worker) throws RemoteException {

        for (WorkerInfo w : workers) {
            if (w.id == worker.id) {
                System.out.println(
                        "Worker ID " + worker.id +
                        " already exists."
                );
                return false;
            }
        }

        workers.add(worker);

        System.out.println(
                "Worker " + worker.id +
                " registered at " +
                worker.host + ":" +
                worker.port
        );

        return true;
    }

    @Override
    public synchronized void unregisterWorker(
            int workerId) throws RemoteException {

        for (int i = 0; i < workers.size(); i++) {

            if (workers.get(i).id == workerId) {

                workers.remove(i);

                System.out.println(
                        "Worker " + workerId +
                        " unregistered."
                );

                return;
            }
        }
    }

    @Override
    public synchronized void updateWorker(
            WorkerInfo worker) throws RemoteException {

        for (int i = 0; i < workers.size(); i++) {

            if (workers.get(i).id == worker.id) {

                workers.set(i, worker);

                System.out.println(
                        "Updated Worker " +
                        worker.id +
                        " JAC=" +
                        worker.jac
                );

                return;
            }
        }
    }

    @Override
    public synchronized List<WorkerInfo> getActiveWorkers()
            throws RemoteException {

        return new ArrayList<>(workers);
    }
}