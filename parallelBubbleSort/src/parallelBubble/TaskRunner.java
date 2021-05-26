package parallelBubble;

import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.locks.ReentrantLock;

public class TaskRunner {

    private static final int MAX_RANDOM = 1_000_000_000;

    public static void main(String[] args) {

        boolean dump = false;

        if (args.length < 2) {
            System.out.println("parallelBubble.TaskRunner <arraySize> <numThreads>");
            System.exit(1);
        }

        if (args.length == 3 && args[2].compareTo("-dump") == 0) {
            dump = true;
        }

        // these are used to time the operations
        long ts_b;
        long ts_e;

        int arraySize = Integer.parseInt(args[0]);
        int numThreads = Integer.parseInt(args[1]);
        int chunkSize = arraySize / numThreads;

        // System.out.printf("DEBUG: arraySize=%d, numThreads=%d, chunkSize=%d\n", arraySize, numThreads, chunkSize);

        Thread[] threads = new Thread[numThreads]; // thread pool
        long[] arr = new long[arraySize];
        ReentrantLock[] reentrantLocks; // critical section pool
        GlobalNum globalNum = new GlobalNum(arraySize - 1); // keeps track of how many elements to sort

        if (arraySize % numThreads == 0) {
            reentrantLocks = new ReentrantLock[numThreads];

            for (int i = 0; i < numThreads; i++) {
                reentrantLocks[i] = new ReentrantLock(true);
            }
        } else {
            reentrantLocks = new ReentrantLock[numThreads + 1];

            for (int i = 0; i < numThreads + 1; i++) {
                reentrantLocks[i] = new ReentrantLock(true);
            }
        }

        // populating raw data
        ts_b = Calendar.getInstance().getTimeInMillis();
        for (int i = 0; i < arraySize; i++) {
            arr[i] = (int) (Math.random() * MAX_RANDOM);
        }
        ts_e = Calendar.getInstance().getTimeInMillis();

        System.out.printf("populating arr[] finished in: %d millis\n", ts_e - ts_b);

        // dumping raw data (if requested)
        if (dump) {
            for (int i = 0; i < arraySize; i++) {
                System.out.printf("%16d", arr[i]);
            }
            System.out.println();
        }

        // making actual work (FORK)
        ts_b = Calendar.getInstance().getTimeInMillis();

        for (int i = 0; i < numThreads; i++) {
            BubbleRunnable bubbleRunnable = new BubbleRunnable(globalNum, reentrantLocks, arr, chunkSize);
            threads[i] = new Thread(bubbleRunnable);
            threads[i].start();
        }

        // waiting for threads to finish (JOIN)
        for (int i = 0; i < numThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                System.out.println("ERROR");
                System.out.println(Arrays.toString(e.getStackTrace()));
            }
        }

        ts_e = Calendar.getInstance().getTimeInMillis();
        System.out.printf("job finished in: %d millis\n", ts_e - ts_b);

        // dumping sorted data (if requested)
        if (dump) {
            for (int i = 0; i < arraySize; i++) {
                System.out.printf("%16d", arr[i]);
            }
            System.out.println();
        }
    }
}
