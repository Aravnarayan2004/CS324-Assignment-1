/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package common;

import java.io.Serializable;

public class Job implements Serializable {

    public String jobId;
    public JobType type;

    public int[] numbers;

    public int start;
    public int end;

    // MAX and PRIMECOUNT
    public Job(JobType type, int[] numbers) {
        this.jobId = "JOB-" + System.currentTimeMillis();
        this.type = type;
        this.numbers = numbers;
    }

    // PRIMESUM
    public Job(JobType type, int start, int end) {
        this.jobId = "JOB-" + System.currentTimeMillis();
        this.type = type;
        this.start = start;
        this.end = end;
    }
}
