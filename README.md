# CS324 Distributed System - RMI Starter Project

## Requirements
Java JDK 8+ and a terminal/command prompt.

## Compile
From the project folder:

```text
mkdir out
javac -d out src/main/java/common/*.java src//main/java/bootstrap/*.java src//main/java/worker/*.java src//main/java/client/*.java
```

## Run
Open separate terminals.

### 1. Bootstrap
```text
java -cp out bootstrap.BootstrapNode
```

### 2. Workers
Run at least 3 workers:
```text
java -cp out worker.WorkerNode 1 2001
java -cp out worker.WorkerNode 2 2002
java -cp out worker.WorkerNode 3 2003
java -cp out worker.WorkerNode 4 2004

```
Each worker waits for ENTER, then starts an election.

### 3. Client GUI
```text
java -cp out client.ClientGUI
```
Set the coordinator port to the worker that was elected, then submit jobs. Multiple submissions can run concurrently in the GUI.

## Job types
- MAX: enter comma-separated integers.
- PRIMECOUNT: enter comma-separated integers.
- PRIMESUM: enter start and end values.

## Notes
This project demonstrates Java RMI, a separate bootstrap process, worker processes, neighbour connections, election messages, coordinator propagation, JAC tracking, a five-job coordinator term, threaded job execution, and a Swing GUI.
