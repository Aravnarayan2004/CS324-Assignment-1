/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WorkerRemote extends Remote {

    WorkerInfo getWorkerInfo()
            throws RemoteException;

    void addNeighbour(WorkerInfo worker)
            throws RemoteException;

    void receiveElection(String electionId, int sender)
            throws RemoteException;

    void receiveCoordinator(String electionId, int coordinatorId)
            throws RemoteException;

    JobResult executeJob(Job job)
            throws RemoteException;
}