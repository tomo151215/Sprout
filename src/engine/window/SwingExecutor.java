package engine.window;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.swing.SwingUtilities;

final class SwingExecutor {
    private SwingExecutor() {
    }

    static void runAndWait(Runnable action) {
        callAndWait(() -> {
            action.run();
            return null;
        });
    }

    static <T> T callAndWait(Callable<T> action) {
        if (SwingUtilities.isEventDispatchThread()) {
            return callDirectly(action);
        }

        FutureTask<T> task = new FutureTask<>(action);
        SwingUtilities.invokeLater(task);

        try {
            return task.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for the Swing event thread.", e);
        } catch (ExecutionException e) {
            throw rethrow(e.getCause());
        }
    }

    private static <T> T callDirectly(Callable<T> action) {
        try {
            return action.call();
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Swing operation failed.", e);
        }
    }

    private static RuntimeException rethrow(Throwable cause) {
        if (cause instanceof RuntimeException runtimeException) {
            return runtimeException;
        }
        if (cause instanceof Error error) {
            throw error;
        }
        return new IllegalStateException("Swing operation failed.", cause);
    }
}
