package gollorum.signpost.utils;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class Delay {

    private static class Task {
        private final Supplier<Boolean> canRun;
        private final Runnable run;

        public boolean canRun() { return canRun.get(); }
        public void run() { run.run(); }

        private Task(Supplier<Boolean> canRun, Runnable run) {
            this.canRun = canRun;
            this.run = run;
        }
    }

    private static class ClientTickHandler implements ClientTickEvent.Client {
        @Override
        public void tick(Minecraft instance) {
            Task[] tasks = clientTasks.toArray(new Task[0]);
            clientTasks.clear();
            for(Task task: tasks) {
                if(task.canRun()) task.run();
                else clientTasks.add(task);
            }
        }
    }

    private static class ServerTickHandler implements TickEvent.Server {
        @Override
        public void tick(MinecraftServer instance) {
            Task[] tasks = serverTasks.toArray(new Task[0]);
            serverTasks.clear();
            for(Task task: tasks) {
                if(task.canRun()) task.run();
                else serverTasks.add(task);
            }
        }
    }

    private static final List<Task> serverTasks = new ArrayList<>();
    private static final List<Task> clientTasks = new ArrayList<>();

    public static void registerServer() {
        TickEvent.SERVER_PRE.register(new ServerTickHandler());
    }

    public static void registerClient() {
        ClientTickEvent.CLIENT_PRE.register(new ClientTickHandler());
    }

    public static void onClientForFrames(int frames, Runnable run) {
        Delay.forFrames(frames, true, run);
    }

    public static void onServerForFrames(int frames, Runnable run) {
        Delay.forFrames(frames, false, run);
    }

    public static void forFrames(int frames, boolean onClient, Runnable run) {
        if(frames == 0) run.run();
        else {
            AtomicInteger framesLeft = new AtomicInteger(frames);
            (onClient ? clientTasks : serverTasks).add(new Task(() -> framesLeft.decrementAndGet() <= 0, run));
        }
    }

    public static void onServerUntil(Supplier<Boolean> canRun, Runnable run) {
        if(canRun.get()) run.run();
        else serverTasks.add(new Task(canRun, run));
    }

    public static void onClientUntil(Supplier<Boolean> canRun, Runnable run) {
        if(canRun.get()) run.run();
        else clientTasks.add(new Task(canRun, run));
    }

    public static void until(Supplier<Boolean> canRun, Runnable run, boolean onClient) {
        if(onClient)
            onClientUntil(canRun, run);
        else
            onServerUntil(canRun, run);
    }

    public static void onServerUntil(Supplier<Boolean> canRun, Runnable run, int timeoutFrames, Optional<Runnable> onTimeOut) {
        if(canRun.get()) run.run();
        else delayUntil(canRun, run, timeoutFrames, serverTasks, onTimeOut);
    }

    public static void onClientUntil(Supplier<Boolean> canRun, Runnable run, int timeoutFrames, Optional<Runnable> onTimeOut) {
        if(canRun.get()) run.run();
        else delayUntil(canRun, run, timeoutFrames, clientTasks, onTimeOut);
    }

    public static <T> void onServerUntilIsPresent(Supplier<Optional<T>> supplier, Consumer<T> run, int timeoutFrames, Optional<Runnable> onTimeOut) {
        AtomicReference<Optional<T>> result = new AtomicReference<>(supplier.get());
        if(result.get().isPresent()) run.accept(result.get().get());
        else delayUntil(
            () -> {
                result.set(supplier.get());
                return result.get().isPresent();
            },
            () -> run.accept(result.get().get()),
            timeoutFrames,
            serverTasks,
            onTimeOut
        );
    }

    public static <T> void onClientUntilIsPresent(Supplier<Optional<T>> supplier, Consumer<T> run, int timeoutFrames, Optional<Runnable> onTimeOut) {
        AtomicReference<Optional<T>> result = new AtomicReference<>(supplier.get());
        if(result.get().isPresent()) run.accept(result.get().get());
        else delayUntil(
            () -> {
                result.set(supplier.get());
                return result.get().isPresent();
            },
            () -> run.accept(result.get().get()),
            timeoutFrames,
            clientTasks,
            onTimeOut
        );
    }

    public static void until(Supplier<Boolean> canRun, Runnable run, int timeoutFrames, boolean onClient, Optional<Runnable> onTimeOut) {
        if(onClient)
            onClientUntil(canRun, run, timeoutFrames, onTimeOut);
        else
            onServerUntil(canRun, run, timeoutFrames, onTimeOut);
    }

    public static <T> void untilIsPresent(Supplier<Optional<T>> supplier, Consumer<T> run, int timeoutFrames, boolean onClient, Optional<Runnable> onTimeOut) {
        if(onClient)
            onClientUntilIsPresent(supplier, run, timeoutFrames, onTimeOut);
        else
            onServerUntilIsPresent(supplier, run, timeoutFrames, onTimeOut);
    }

    private static void delayUntil(Supplier<Boolean> canRun, Runnable run, int timeoutFrames, List<Task> taskList, Optional<Runnable> onTimeOut) {
        AtomicInteger framesLeft = new AtomicInteger(timeoutFrames);
        taskList.add(new Task(
            () -> {
                framesLeft.set(framesLeft.get() - 1);
                return canRun.get() || framesLeft.get() < 0;
            },
            () -> {
                if(framesLeft.get() >= 0) run.run();
                else onTimeOut.ifPresent(Runnable::run);
            }
        ));
    }
}
