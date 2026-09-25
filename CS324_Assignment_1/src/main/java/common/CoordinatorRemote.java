/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CoordinatorRemote extends Remote {

    JobResult submitJob(Job job)
            throws RemoteException;
}
