/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package common;

import java.io.Serializable;

public class WorkerInfo implements Serializable {

    public int id;
    public String host;
    public int port;
    public int jac;

    public WorkerInfo(int id, String host, int port, int jac) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.jac = jac;
    }

    @Override
    public String toString() {
        return "Worker " + id +
               " [" + host + ":" + port +
               ", JAC=" + jac + "]";
    }
}