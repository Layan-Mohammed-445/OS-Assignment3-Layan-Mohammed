import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

// ANSI Color Codes for enhanced terminal output
class Colors {
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String CYAN = "\u001B[36m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String BLUE = "\u001B[34m";
    public static final String RED = "\u001B[31m";
    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_GREEN = "\u001B[42m";
    public static final String WHITE = "\u001B[37m";
    public static final String BRIGHT_WHITE = "\u001B[97m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
}

// ⚠️ SHARED RESOURCES - These need synchronization! ⚠️
class SharedResources {
    // locks for independent counters (better concurrency)
    public static final ReentrantLock contextSwitchLock = new ReentrantLock();
    public static final ReentrantLock completedProcessLock = new ReentrantLock();
    public static final ReentrantLock waitingTimeLock = new ReentrantLock();
    public static final ReentrantLock logLock = new ReentrantLock();

    // Binary semaphore for CPU access control
    public static final Semaphore cpuSemaphore = new Semaphore(1);

    public static int contextSwitchCount = 0;
    public static int completedProcessCount = 0;
    public static long totalWaitingTime = 0;
    public static List<String> executionLog = new ArrayList<>();

    // Method to increment context switch counter
    public static void incrementContextSwitch() {
        contextSwitchLock.lock();
        try {
            contextSwitchCount++;
        } finally {
            contextSwitchLock.unlock();
        }
    }

    // Method to increment completed process counter
    public static void incrementCompletedProcess() {
        completedProcessLock.lock();
        try {
            completedProcessCount++;
        } finally {
            completedProcessLock.unlock();
        }
    }

    // Method to add waiting time
    public static void addWaitingTime(long time) {
        waitingTimeLock.lock();
        try {
            totalWaitingTime += time;
        } finally {
            waitingTimeLock.unlock();
        }
    }

    // Method to log execution
    public static void logExecution(String message) {
        logLock.lock();
        try {
            executionLog.add(message);
        } finally {
            logLock.unlock();
        }
    }
}

// Class representing a process that implements Runnable to be run by a thread
class Process implements Runnable {
    private String name;
    private int burstTime;
    private int timeQuantum;
    private int remainingTime;
    private long creationTime;
    private long startTime;
    private long completionTime;
    private int priority;

    public Process(String name, int burstTime, int timeQuantum, int priority) {
        this.name = name;
        this.burstTime = burstTime;
        this.timeQuantum = timeQuantum;
        this.remainingTime = burstTime;
        this.priority = priority;
        this.creationTime = System.currentTimeMillis();
        this.startTime = -1;
    }

    @Override
    public void run() {
        try {
            SharedResources.cpuSemaphore.acquire();
            try {
                if (startTime == -1) {
                    startTime = System.currentTimeMillis();
                }

                // Increment context switch counter
                SharedResources.incrementContextSwitch();

                int runTime = Math.min(timeQuantum, remainingTime);

                String quantumBar = createProgressBar(0, 15);
                String message = "  ▶ " + name + " (Priority: " + priority + ") executing quantum [" + runTime + "ms]";
                System.out.println(Colors.BRIGHT_GREEN + message + Colors.RESET);

                // Log execution
                SharedResources.logExecution(name + " started quantum execution");

                try {
                    int steps = 5;
                    int stepTime = runTime / steps;

                    for (int i = 1; i <= steps; i++) {
                        Thread.sleep(stepTime);
                        int quantumProgress = (i * 100) / steps;
                        quantumBar = createProgressBar(quantumProgress, 15);
                        System.out.print("\r  " + Colors.YELLOW + "⚡" + Colors.RESET +
                                " Quantum progress: " + quantumBar);
                    }
                    System.out.println();

                } catch (InterruptedException e) {
                    System.out.println(Colors.RED + "\n  ✗ " + name + " was interrupted." + Colors.RESET);
                }

                remainingTime -= runTime;
                int overallProgress = (int) (((double) (burstTime - remainingTime) / burstTime) * 100);
                String overallProgressBar = createProgressBar(overallProgress, 20);

                System.out.println(Colors.YELLOW + "  ⏸ " + Colors.CYAN + name + Colors.RESET +
                        " completed quantum " + Colors.BRIGHT_YELLOW + runTime + "ms" + Colors.RESET +
                        " │ Overall progress: " + overallProgressBar);
                System.out.println(Colors.MAGENTA + "     Remaining time: " + remainingTime + "ms" + Colors.RESET);

                if (remainingTime > 0) {
                    System.out.println(Colors.BLUE + "  ↻ " + Colors.CYAN + name + Colors.RESET +
                            " yields CPU for context switch" + Colors.RESET);
                    SharedResources.logExecution(name + " yielded CPU");
                } else {
                    completionTime = System.currentTimeMillis();
                    long waitingTime = (completionTime - creationTime) - burstTime;
                    SharedResources.addWaitingTime(waitingTime);
                    SharedResources.incrementCompletedProcess();
                    SharedResources.logExecution(name + " completed execution");
                    System.out.println(Colors.BRIGHT_GREEN + "  ✓ " + Colors.BOLD + Colors.CYAN + name +
                            Colors.RESET + Colors.BRIGHT_GREEN + " finished execution!" +
                            Colors.RESET);
                }
                System.out.println();

            } finally {
                SharedResources.cpuSemaphore.release();
            }
        } catch (InterruptedException e) {
            System.out.println(Colors.RED + "  ✗ " + name + " semaphore interrupted." + Colors.RESET);
        }
    }

    private String createProgressBar(int progress, int width) {
        int filled = (progress * width) / 100;
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < width; i++) {
            if (i < filled) {
                bar.append(Colors.GREEN + "█" + Colors.RESET);
            } else {
                bar.append(Colors.WHITE + "░" + Colors.RESET);
            }
        }
        bar.append("] ").append(progress).append("%");
        return bar.toString();
    }

    public void runToCompletion() {
        try {
            SharedResources.cpuSemaphore.acquire();
            try {
                System.out.println(Colors.BRIGHT_CYAN + "  ⚡ " + Colors.BOLD + Colors.CYAN + name +
                        Colors.RESET + Colors.BRIGHT_CYAN + " is the last process, running to completion" +
                        Colors.RESET + " [" + remainingTime + "ms]");
                Thread.sleep(remainingTime);
                remainingTime = 0;
                completionTime = System.currentTimeMillis();

                long waitingTime = (completionTime - creationTime) - burstTime;
                SharedResources.addWaitingTime(waitingTime);
                SharedResources.incrementCompletedProcess();

                System.out.println(Colors.BRIGHT_GREEN + "  ✓ " + Colors.BOLD + Colors.CYAN + name +
                        Colors.RESET + Colors.BRIGHT_GREEN + " finished execution!" + Colors.RESET);
                System.out.println();
            } finally {
                SharedResources.cpuSemaphore.release();
            }
        } catch (InterruptedException e) {
            System.out.println(Colors.RED + "  ✗ " + name + " was interrupted." + Colors.RESET);
        }
    }

    public String getName() {
        return name;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isFinished() {
        return remainingTime <= 0;
    }

    public long getWaitingTime() {
        if (completionTime > 0) {
            return (completionTime - creationTime) - burstTime;
        }
        return 0;
    }
}

public class SchedulerSimulationSync {
    public static void main(String[] args) {
        int studentID = 445052092;

        Random random = new Random(studentID);

        int timeQuantum = 2000 + random.nextInt(4) * 1000;
        int numProcesses = 10 + random.nextInt(11);

        Queue<Thread> processQueue = new LinkedList<>();
        Map<Thread, Process> processMap = new HashMap<>();
        List<Process> allProcesses = new ArrayList<>();

        // Create processes with priorities
        for (int i = 1; i <= numProcesses; i++) {
            int burstTime = timeQuantum / 2 + random.nextInt(2 * timeQuantum + 1);
            int priority = 1 + random.nextInt(5);

            Process process = new Process("P" + i, burstTime, timeQuantum, priority);
            allProcesses.add(process);
            addProcessToQueue(process, processQueue, processMap);
        }

        // Scheduler loop
        while (!processQueue.isEmpty()) {
            Thread currentThread = processQueue.poll();

            currentThread.start();

            try {
                currentThread.join();
            } catch (InterruptedException e) {
                System.out.println("Main thread interrupted.");
            }

            Process process = processMap.get(currentThread);

            if (!process.isFinished()) {
                if (!processQueue.isEmpty()) {
                    addProcessToQueue(process, processQueue, processMap);
                } else {
                    process.runToCompletion();
                }
            }
        }

        printStatistics(allProcesses, timeQuantum);
    }

    public static void addProcessToQueue(Process process, Queue<Thread> processQueue,
                                         Map<Thread, Process> processMap) {
        Thread thread = new Thread(process);
        processQueue.add(thread);
        processMap.put(thread, process);
    }

    public static void printStatistics(List<Process> processes, int timeQuantum) {
        System.out.println("Total Context Switches: " + SharedResources.contextSwitchCount);
        System.out.println("Total Completed Processes: " + SharedResources.completedProcessCount);
        System.out.println("Total Waiting Time: " + SharedResources.totalWaitingTime + "ms");
        System.out.println("Average Waiting Time: " +
                (SharedResources.totalWaitingTime / processes.size()) + "ms");

        for (Process p : processes) {
            System.out.println(
                    p.getName() + " | Priority: " + p.getPriority() +
                            " | Burst: " + p.getBurstTime() +
                            " | Waiting: " + p.getWaitingTime()
            );
        }

        System.out.println("Total log entries: " + SharedResources.executionLog.size());
    }
}