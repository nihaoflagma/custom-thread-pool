package executor;

public class RejectedExecutionHandlerImpl {
    public void rejected(Runnable task) {
        System.out.println("[Rejected] Task " + task + " was rejected due to overload!");
    }
}
