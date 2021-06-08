package io.seata.core.concurrent;

import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.*;

/**
 * @author: xielongfei
 * @date: 2021/05/10 10:54
 * @description:
 */
public class FutureTest {

    @Test
    public void test4() throws Exception {
        //ForkJoinPool pool=new ForkJoinPool();
        CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName());
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
            }
            return "1";
        });
        System.out.println("f1:" + f1);

        //cf关联的异步任务的返回值作为方法入参，传入到thenApply的方法中
        //thenApply这里实际创建了一个新的CompletableFuture实例
        CompletableFuture<String> f5 = f1.thenApply(r -> {
            System.out.println(r + " f5 " + Thread.currentThread().getName());
            return "f5";
        });
        System.out.println("f5:" + f5);
        CompletableFuture<String> f6 = f5.thenApply(r -> {
            System.out.println(r + " f6 " + Thread.currentThread().getName());
            return "f6";
        });
        System.out.println("f6:" + f6);
        CompletableFuture<String> f2 = f1.thenApply(r -> {
            System.out.println(r + " f2 " + Thread.currentThread().getName());
            return r + "2";
        });
        System.out.println("f2:" + f2);
        CompletableFuture<String> f3 = f2.thenApply(r -> {
            System.out.println(r + " f3 " + Thread.currentThread().getName());
            return "f3";
        });
        System.out.println("f3:" + f3);
        CompletableFuture<String> f4 = f3.thenApply(r -> {
            System.out.println(r + " f4 " + Thread.currentThread().getName());
            return "f3";
        });
        System.out.println("f4:" + f4);

        //等待子任务执行完成
        System.out.println("run result->" + f1.get());
    }

    @Test
    public void test2() throws Exception {
        // 创建异步执行任务，有返回值
        CompletableFuture<Double> cf = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread() + " start,time->" + System.currentTimeMillis());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            if (false) {
                throw new RuntimeException("test");
            } else {
                System.out.println(Thread.currentThread() + " exit,time->" + System.currentTimeMillis());
                return 1.2;
            }
        });
        System.out.println("main thread start,time->" + System.currentTimeMillis());
        //等待子任务执行完成
        System.out.println("run result->" + cf.get());
        System.out.println("main thread exit,time->" + System.currentTimeMillis());
    }

    public static class Task implements Callable<String> {
        @Override
        public String call() {
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
            }
            String tid = String.valueOf(Thread.currentThread().getId());
            System.out.printf("Thread#%s %s: in call\n", tid, Thread.currentThread().getName());
            return tid;
        }
    }



    public static void main(String[] args) throws Exception {
        ExecutorService es = Executors.newFixedThreadPool(3);

        Future future1 = es.submit(new Task());
        Future future2 = es.submit(new Task());
        Future future3 = es.submit(new Task());
        Future future4 = es.submit(new Task());
        Field waiters1 = future1.getClass().getDeclaredField("waiters");
        Field waitersOffset1 = future1.getClass().getDeclaredField("waitersOffset");
        waiters1.setAccessible(true);
        waitersOffset1.setAccessible(true);
        int i = 0;
        while (i < 10) {
            i ++;
            System.out.println(waiters1.getName() + "  " + waiters1.get(future1) + "   " + waitersOffset1.get(future1));
        }

        System.out.println(future1.get());
        System.out.println(future2.get());
        System.out.println(future3.get());
        System.out.println(future4.get());

        Field waiters2 = future2.getClass().getDeclaredField("waiters");
        Field waitersOffset2 = future2.getClass().getDeclaredField("waitersOffset");
        waiters2.setAccessible(true);
        waitersOffset2.setAccessible(true);
        System.out.println(waiters2.getName() + "  " + waiters2.get(future1) + "   " + waitersOffset2.get(future1));


    }

    public void test3() {
        ExecutorService es = Executors.newFixedThreadPool(3);
        int i = 0;
        while (i < 1) {
            i++;
            //es.submit(new Task());
        }
        FutureTask future = (FutureTask) es.submit(new Task());
        try {
            sun.misc.Unsafe UNSAFE = null;
            try {
                Field field = Unsafe.class.getDeclaredField("theUnsafe");
                field.setAccessible(true);
                UNSAFE = (Unsafe) field.get(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }

            Class<?> k = future.getClass();
            Field waiters = k.getDeclaredField("waiters");
            long waitersOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("waiters"));
        } catch (Exception e) {
            throw new Error(e);
        }
        //System.out.println(future.get());
    }

    @Test
    public void test1() {
        int COUNT_BITS = Integer.SIZE - 3;
        int CAPACITY = (1 << COUNT_BITS) - 1;
        int RUNNING = -1 << COUNT_BITS;
        int SHUTDOWN = 0 << COUNT_BITS;
        int c = (RUNNING | 0) + 0;
        int workerCountOf = c & CAPACITY;
        boolean bool = c < SHUTDOWN;
        int runStateOf = c & ~CAPACITY;
        System.out.println(bool);
        System.out.println(COUNT_BITS + "   " + Integer.toBinaryString(COUNT_BITS));
        System.out.println(RUNNING + "   " + Integer.toBinaryString(RUNNING));
        System.out.println("shutdown " + SHUTDOWN + "   " + Integer.toBinaryString(SHUTDOWN));
        System.out.println(CAPACITY + "   " + Integer.toBinaryString(CAPACITY));
        System.out.println("~CAPACITY " + ~CAPACITY + "   " + Integer.toBinaryString(~CAPACITY));
        System.out.println(c + "   " + Integer.toBinaryString(c));
        System.out.println(workerCountOf + "   " + Integer.toBinaryString(workerCountOf));
        System.out.println(runStateOf + "   " + Integer.toBinaryString(runStateOf));
    }
}
