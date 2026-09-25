/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package common;

import java.io.Serializable;

public class JobResult implements Serializable {

    public String jobId;
    public long result;

    public JobResult(String jobId, long result) {
        this.jobId = jobId;
        this.result = result;
    }

    @Override
    public String toString() {
        return jobId + " -> " + result;
    }
}