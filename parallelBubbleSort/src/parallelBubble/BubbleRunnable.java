package parallelBubble;

import java.util.concurrent.locks.ReentrantLock;

public class BubbleRunnable implements Runnable {

    private final GlobalNum globalNum;
    private final ReentrantLock[] reentrantLocks;
    private final long[] arr;
    private final int chunkSize;
    private boolean done;
    private boolean exch;

    public BubbleRunnable(GlobalNum globalNum, ReentrantLock[] reentrantLocks, long[] arr, int chunkSize) {
        this.globalNum = globalNum; // g
        this.reentrantLocks = reentrantLocks; // cl
        this.arr = arr;
        this.chunkSize = chunkSize;

        this.done = false;
        this.exch = false;
    }

    public long _gts() {
        return System.nanoTime();
    }

    public void run() {
        int critialSection; // k
        int releasePoint;
        long _lt_total = 0;
        long _ts_b = _gts();
        String _name = Thread.currentThread().getName();

        while (!done) {
            critialSection = 0; // default critical section is 0
            exch = false; // default assumption is for sorted chunk
            releasePoint = chunkSize;

            long _ts_k_b = _gts();
            reentrantLocks[critialSection].lock(); // take the lock for the current critical section
            long _ts_k_e = _gts();

            _lt_total += _ts_k_e - _ts_k_b;

            for (int j = 0; j < globalNum.n; j++) { // making internal interaction

                if (arr[j] > arr[j + 1]) { // we have to swap them
                    long tmp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = tmp;
//                    BigDecimal r = new BigDecimal(arr[j]).multiply(new BigDecimal(arr[j + 1]));
                    exch = true;
                }

                if (j == releasePoint - 1) { // we are over the release point
                    reentrantLocks[critialSection].unlock();
                    ++critialSection;

                    _ts_k_b = _gts();
                    reentrantLocks[critialSection].lock();
                    _ts_k_e = _gts();

                    _lt_total += _ts_k_e - _ts_k_b;

                    releasePoint += chunkSize;
                }
            }

            reentrantLocks[critialSection].unlock(); // unlocking the final critical section

            if (!exch) {
                done = true;
            }
        }

        long _ts_e = _gts();

        double _total_time = (_ts_e - _ts_b) / 1_000_000.0;
        double _total_lock_time = (double) _lt_total / 1_000_000.0;

        System.out.printf("%s.done: total time (millis) -> %f, spin time (millis) -> %f, lock time (millis) -> %f\n",
                _name, _total_time, (_total_time - _total_lock_time), _total_lock_time);

    }
}
