/**
 * 应用程序执行任务线程组
 */
package com.qy.sp.fee.common.tools;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskExecutor {
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = 3 * CPU_COUNT + 1;
    private static TaskExecutor instance = new TaskExecutor();
    private ScheduledThreadPoolExecutor service = new ScheduledThreadPoolExecutor(CORE_POOL_SIZE);
    private Map<Runnable, ScheduledFuture<?>> futures = new ConcurrentHashMap<Runnable, ScheduledFuture<?>>();

    private TaskExecutor() {
    	setMaxSize(50);
    }
    public void setMaxSize(int maxSize){
    	service.setMaximumPoolSize(maxSize);
    }
    public void addScheduled(Runnable command, long initialDelay, long period, TimeUnit unit) {
        if (!futures.containsKey(command)) {
            ScheduledFuture<?> future = service.scheduleAtFixedRate(command, initialDelay, period, unit);
            futures.put(command, future);
        }
    }

    public void removeScheduled(Runnable r) {
        ScheduledFuture<?> f = this.futures.get(r);
        if (f != null) {
            f.cancel(true);
            this.futures.remove(r);
        }
    }

    public Future<?> execute(Runnable r) {
        return this.service.submit(r);
    }

    public Future<?> executeDelay(Runnable r, long delayTime) {
        return this.service.schedule(r, delayTime, TimeUnit.MILLISECONDS);
    }
    public void executeNewThread(Runnable runnable){
        new Thread(runnable).start();
    }
    public void executeSpecialTime(Runnable r, long specialTime) {
        long currentSecond = System.currentTimeMillis();
        if (specialTime > currentSecond) {
            long delayTime = specialTime - currentSecond;
            this.service.schedule(r, delayTime, TimeUnit.SECONDS);
        }
    }

    public static TaskExecutor getInstance() {
        return instance;
    }

    public void shutdown() {
        for (Runnable r : futures.keySet()) {
            removeScheduled(r);
        }
        futures.clear();
    }

    public ScheduledExecutorService getService() {
        return service;
    }
}
