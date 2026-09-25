/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface BootstrapRemote extends Remote {

    boolean registerWorker(WorkerInfo worker)
            throws RemoteException;

    void unregisterWorker(int workerId)
            throws RemoteException;

    void updateWorker(WorkerInfo worker)
            throws RemoteException;

    List<WorkerInfo> getActiveWorkers()
            throws RemoteException;
}