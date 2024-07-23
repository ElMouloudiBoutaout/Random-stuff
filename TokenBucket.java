import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TokenBucket {
    private static final double MAX_BUCKET_SIZE = 3.0;
    private static final int REFILL_RATE = 1;

    private double currentBucketSize;
    private long lastRefillTimestamp;
    private final ScheduledExecutorService scheduleAtFixedRate;


    public TokenBucket() {
        this.scheduleAtFixedRate = Executors.newSingleThreadScheduledExecutor();;
        this.currentBucketSize = MAX_BUCKET_SIZE;
        this.lastRefillTimestamp = System.nanoTime();
        scheduleRefillTask();
    }

    public synchronized boolean allowRequest() {
        if (currentBucketSize >= 1) {
            currentBucketSize -= 1;
            return true;
        }

        return false;
    }

    private void refill() {
        long nowTime = System.nanoTime();
        double tokensToAdd = (nowTime - lastRefillTimestamp) * REFILL_RATE / 1e9;
        currentBucketSize = Math.min(currentBucketSize + tokensToAdd, MAX_BUCKET_SIZE);
        lastRefillTimestamp = nowTime;
    }

    private void scheduleRefillTask() {
        Runnable refillTask = this::refill;
        long initialDelay = 0;
        long period = 3; // in seconds
        scheduleAtFixedRate.scheduleAtFixedRate(refillTask, initialDelay, period, TimeUnit.SECONDS);
    }

    public void shutdownScheduler() {
        scheduleAtFixedRate.shutdown();
    }

    public static void main(String[] args) throws InterruptedException {
        TokenBucket obj = new TokenBucket();


        System.out.printf("Request processed: %b\n", obj.allowRequest()); // true
        System.out.printf("Request processed: %b\n", obj.allowRequest()); // true
        System.out.printf("Request processed: %b\n", obj.allowRequest()); // true
        System.out.printf("Request processed: %b\n", obj.allowRequest()); // false, request dropped

        // Wait for some time to see the refill in action
        Thread.sleep(5000);

        System.out.printf("Request processed: %b\n", obj.allowRequest()); // true, after refill
        System.out.printf("Request processed: %b\n", obj.allowRequest()); // true
        System.out.printf("Request processed: %b\n", obj.allowRequest()); // true
        System.out.printf("Request processed: %b\n", obj.allowRequest()); // false

        obj.shutdownScheduler();
    }
}
