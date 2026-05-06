# Assignment 3 - Complete Documentation

**Student Name**: [Layan Mohammed Aljuhayyim]  
**Student ID**: [445052092]  
**Date Submitted**: [Submission Date]

---

## 🎥 VIDEO DEMONSTRATION LINK (REQUIRED)

> **⚠️ IMPORTANT: This section is REQUIRED for grading!**
> 
> Upload your 3-5 minute video to your **PERSONAL Gmail Google Drive** (NOT university email).
> Set sharing to "Anyone with the link can view".
> Test the link in incognito/private mode before submitting.

**Video Link**: [Paste your personal Gmail Google Drive link here]

**Video filename**: `[YourStudentID]_Assignment3_Synchronization.mp4`

**Verification**:
- [ ] Link is accessible (tested in incognito mode)
- [ ] Video is 3-5 minutes long
- [ ] Video shows code walkthrough and commits
- [ ] Video has clear audio
- [ ] Uploaded to PERSONAL Gmail (not @std.psau.edu.sa)

---

## Part 1: Development Log (1 mark)

Document your development process with **minimum 3 entries** showing progression:

### Entry 1 - [May 6, 2026, 9:27 PM]
**What I implemented**: I changed the student ID in the code from the default ID to my own student ID.

**Challenges encountered**: 
There were no major challenges because it was only changing one variable.
**How I solved it**: 
I found the studentID variable in the main() method and replaced it with my actual ID.
**Testing approach**: 
I ran the program and checked that my student ID appeared correctly in the github.
**Time spent**: 
10 minutes
---

### Entry 2 - [May 6, 2026, 9:40 PM]
**What I implemented**: 
I added ReentrantLock to protect the shared resources like context switches, completed processes, waiting time, and execution log.
**Challenges encountered**: 
At first I wasn’t sure if I should use one lock or multiple locks. and i had to check all the errors.
**How I solved it**: 
I decided to use separate locks for each shared resource because it makes the program more organized and allows better concurrency.
**Testing approach**: 
I tested by running the program and checking that all counters updated correctly.
**Time spent**: 
40 minutes
---

### Entry 3 - [May 6, 2026, 10:30 PM]
**What I implemented**: 
I added a Semaphore to control CPU access so only one process can use the CPU at a time.
**Challenges encountered**: 
I was confused at first about where to place acquire() and release() and if my logical code was fit for the rest of the code.
**How I solved it**: 
I put acquire() before the process starts execution and release() inside finally to make sure it always gets released.
**Testing approach**: 
I ran the simulation and checked that processes executed one at a time.
**Time spent**: 
55 minutes
---

### Entry 4 - [Date, Time]
**What I implemented**: 

**Challenges encountered**: 

**How I solved it**: 

**Testing approach**: 

**Time spent**: 

---

### Entry 5 - [Date, Time]
**What I implemented**: 

**Challenges encountered**: 

**How I solved it**: 

**Testing approach**: 

**Time spent**: 

---

## Part 2: Technical Questions (1 mark)

### Question 1: Race Conditions
**Q**: Identify and explain TWO race conditions in the original code. For each:
- What shared resource is affected?
- Why is concurrent access a problem?
- What incorrect behavior could occur?

**Your Answer**:

[The first race condition in the original code was in contextSwitchCount++. This variable is shared by all threads, and if two threads update it at the same time, one update could be lost. For example, if two threads read the value 5 at the same time, both may increase it to 6 instead of reaching 7. This causes wrong statistics in the program.

The second race condition was in executionLog.add(message). The shared resource here is the ArrayList executionLog, and ArrayList is not thread-safe. If multiple threads try to add messages at the same time, some log entries may be lost or stored in the wrong order. This can make the execution history incorrect and confusing.]

---

### Question 2: Locks vs Semaphores
**Q**: Explain the difference between ReentrantLock and Semaphore. Where did you use each in your code and why?

**Your Answer**:

[ReentrantLock is used to protect a specific part of the code so only one thread can access it at a time. A Semaphore controls access to a resource by allowing a certain number of threads at the same time. In my code, I used ReentrantLock for the shared counters and execution log because these are critical sections that need safe updates. For example, in incrementContextSwitch() I used contextSwitchLock.lock() and unlock(). I used a Semaphore for CPU access because only one process should use the CPU at a time, so I used cpuSemaphore.acquire() and release(). This way locks protect data, and semaphores control resource usage]

---

### Question 3: Deadlock Prevention
**Q**: What is deadlock? Explain TWO prevention techniques and what you did to prevent deadlocks in your code.

**Your Answer**:

[Deadlock is when two or more threads are waiting for each other forever and cannot continue. One prevention technique is using try-finally blocks to make sure locks or semaphores are always released even if an error happens. In my code, I used finally with both locks and semaphore release. Another prevention technique is avoiding nested locks or keeping lock usage simple. In my code, I used separate locks for different resources and did not lock them inside each other, which reduces deadlock risk. These two methods help keep the program safe and running correctly.]

---

### Question 4: Lock Granularity Design Decision 
**Q**: For Task 1 (protecting the three counters), explain your lock design choice:
- Did you use ONE lock for all three counters (coarse-grained) OR separate locks for each counter (fine-grained)?
- Explain WHY you made this choice
- What are the trade-offs between the two approaches?
- Given that the three counters are independent, which approach provides better concurrency and why?

**Your Answer**:

[I used separate locks for each counter, which is called fine-grained locking. I made this choice because the counters (contextSwitchCount, completedProcessCount, and totalWaitingTime) are independent and do not affect each other. This means one thread can update one counter while another thread updates another counter at the same time. If I used one lock for all counters (coarse-grained), it would be simpler but slower because every thread would have to wait even if they are using different counters. The advantage of coarse-grained locking is easier code management, but the disadvantage is less concurrency. Fine-grained locking gives better performance because it allows more parallel work. Since my counters are independent, fine-grained locking provides better concurrency and efficiency.]

---

## Part 3: Synchronization Analysis (1 mark)

### Critical Section #1: Counter Variables

**Which variables**: 
The shared counter variables are: contextSwitchCount,completedProcessCount,totalWaitingTime.
**Why they need protection**: 
These variables are shared between all threads. If two threads update them at the same time, the values may become incorrect because one update could overwrite another.
**Synchronization mechanism used**: 
I used ReentrantLock with separate locks for each counter.
**Code snippet**:
```java
public static final ReentrantLock contextSwitchLock = new ReentrantLock();
public static final ReentrantLock completedProcessLock = new ReentrantLock();
public static final ReentrantLock waitingTimeLock = new ReentrantLock();

public static void incrementContextSwitch() {
    contextSwitchLock.lock();
    try {
        contextSwitchCount++;
    } finally {
        contextSwitchLock.unlock();
    }
}

public static void incrementCompletedProcess() {
    completedProcessLock.lock();
    try {
        completedProcessCount++;
    } finally {
        completedProcessLock.unlock();
    }
}

public static void addWaitingTime(long time) {
    waitingTimeLock.lock();
    try {
        totalWaitingTime += time;
    } finally {
        waitingTimeLock.unlock();
    }
}
```

**Justification**: 
I used separate locks because each counter is independent. This allows multiple threads to update different counters at the same time without blocking each other.
---

### Critical Section #2: Execution Log

**What resource**: 
The shared resource is the executionLog list.
**Why it needs protection**: 
The execution log is an ArrayList, and ArrayList is not thread-safe. If multiple threads add messages at the same time, data may be lost or stored incorrectly.
**Synchronization mechanism used**: 
I used ReentrantLock.
**Code snippet**:
```java
public static final ReentrantLock logLock = new ReentrantLock();

public static void logExecution(String message) {
    logLock.lock();
    try {
        executionLog.add(message);
    } finally {
        logLock.unlock();
    }
}
```

**Justification**: 
The lock makes sure only one thread can add to the log at a time, which keeps the log correct and prevents missing entries.
---

### Critical Section #3: CPU Semaphore

**Purpose of semaphore**: 
The semaphore controls access to the CPU so only one process can execute at a time.
**Number of permits and why**: 
I used 1 permit because the simulation should behave like one CPU handling one process at a time.
**Where implemented**: 
It is implemented in both run() and runToCompletion().
**Code snippet**:
```java
public static final Semaphore cpuSemaphore = new Semaphore(1);

SharedResources.cpuSemaphore.acquire();

try {
    // process execution
} finally {
    SharedResources.cpuSemaphore.release();
}
```

**Effect on program behavior**: 
The semaphore makes sure processes execute one by one instead of at the same time. This prevents CPU conflicts and keeps the scheduling behavior correct.
---

## Part 4: Testing and Verification (2 marks)

### Test 1: Consistency Check
**What I tested**: Running program multiple times to verify consistent results

**Testing procedure**: I compiled the program once and ran it 5 times to make sure the program worked correctly each time. I checked the final statistics like completed processes, context switches, and execution log.
```bash
# Commands used (run the program at least 5 times)
javac SchedulerSimulationSync.java
java SchedulerSimulationSync
java SchedulerSimulationSync
java SchedulerSimulationSync
java SchedulerSimulationSync
java SchedulerSimulationSync
```

**Results**: 
(Show that running multiple times produces consistent, correct results)
The program completed successfully in all runs. The number of completed processes always matched the total number of processes. The execution log was recorded correctly and there were no missing values or crashes.
**Why synchronization is necessary**: 
(Explain what race conditions COULD occur without synchronization, even if you didn't observe them. Explain which shared resources need protection and why.)
Without synchronization, race conditions could happen when multiple threads update shared variables like contextSwitchCount, completedProcessCount, and totalWaitingTime. For example, two threads could update the same counter at the same time and one update could be lost. The executionLog also needs protection because ArrayList is not thread-safe and multiple writes at once could cause incorrect log entries.
**Conclusion**: 
Synchronization made the program stable and consistent in every run.
---

### Test 2: Exception Testing
**What I tested**: Checking for ConcurrentModificationException

**Testing procedure**: 
I tested the program by running all processes and checking if the execution log and queue operations caused any concurrency errors.
**Results**: 
No ConcurrentModificationException happened during testing. The execution log stored all messages correctly.
**What this proves**: 
This proves that protecting the shared log with ReentrantLock made access safe and prevented concurrent modification problems.
---

### Test 3: Correctness Verification
**What I tested**: Verifying correct final values (total burst time, context switches, etc.)

**Expected values**: 
Completed processes should equal total number of processes.
Context switches should increase whenever a process gets CPU time.
Total waiting time should be greater than or equal to zero.
**Actual values**: 
Completed processes matched the total processes.
Context switch count increased correctly.
Total waiting time was calculated and displayed correctly.
**Analysis**: 
The actual values matched the expected. This shows the synchronization worked correctly and the counters stayed accurate.
---

### Test 4: Different Scenarios
**Scenario tested**: [Different number of processes and different time quantum values (generated by changing the student ID).]

**Purpose**: 
To check if the synchronization still works under different scheduling conditions.
**Results**: 
The program still worked correctly with different burst times, priorities, and process counts. All processes completed and the statistics remained correct.
**What I learned**: 
I learned that synchronization works in different situations and helps keep the program correct no matter how many processes are running.
---

## Part 5: Reflection and Learning

### What I learned about synchronization:

[I learned that synchronization is important when multiple threads share the same data. Without synchronization, race conditions can happen and cause wrong results. I learned how ReentrantLock can protect critical sections and make shared data safe. I also learned that Semaphore is useful for controlling access to limited resources like CPU execution. One challenge was understanding where to place lock(), unlock(), acquire(), and release(). Using try-finally helped make sure resources were always released safely. I also learned the difference between fine-grained and coarse-grained locking and why fine-grained locking can improve performance. Overall, this assignment helped me understand how thread synchronization works in real programs.]

---

### Real-world applications:

Give TWO examples where synchronization is critical:

**Example 1**: 

**Example 2**: 

---

### How I would explain synchronization to others:

[Explain to someone who just finished Assignment 1 - use simple terms and analogies]

---

## Part 6: GitHub Repository Information

**Repository URL**: 

**Number of commits**: 

**Commit messages**: 
1. 
2. 
3. 
4. 

---

## Summary

**Total time spent on assignment**: 

**Key takeaways**: 
1. 
2. 
3. 

**Most challenging aspect**: 

**What I'm most proud of**: 

---

**End of Documentation**
