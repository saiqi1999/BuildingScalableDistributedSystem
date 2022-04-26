package populateNeed;

import org.apache.commons.cli.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

/**
 * we run the main method and change phases here, got synchronized method to calculate all successful/failed request
 * COMMAND LINE ARGUMENTS FOR MAIN():
 * -i ip address (and port), a string like 127.0.0.1:8080, required
 * -t number of threads, default 200
 * -n number of skiers, default 10000
 * -p number per lift, default 40
 * -m mean ride number of the skiers, default 10
 */
public class Starter {
    private static ArrayList<ResponseRecord> wholeList;

    public static synchronized void mergeList(ArrayList<ResponseRecord> cacheList) {
        wholeList.addAll(cacheList);
    }

    public static void main(String[] args) {
        ArgumentParser parser = new ArgumentParser();
        try {
            parser.parse(args);
        } catch (ParseException e) {
            //gracefully exit
            e.printStackTrace();
            return;
        }
        int totalThreads = parser.getNumThreads();
        int numSkiers = parser.getNumSkiers();
        long startTime = System.currentTimeMillis();
        int meanRuns = parser.getMeanRides();
        String ip = parser.getServerIp();
        HashSet<Thread> threadsReg = new HashSet<>();
        wholeList = new ArrayList<>();

        /* //-----------TEST FOR SINGLE THREAD-----------
        CountDownLatch latch = new CountDownLatch(1);
        Thread test = new Thread(new populateNeed.QueryProcess(latch, ip, 1, 111, 1, 11, 20464));
        test.start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long wallTime = System.currentTimeMillis()-startTime;
        System.out.println("all complete in single thread: " + wallTime + " milliseconds (20464 requests)");*/

        //enter START phase
        int numThreads = totalThreads / 4;
        int latchNum = numThreads % 5 == 0 ? numThreads / 5 : numThreads / 5 + 1;
        CountDownLatch latch1 = new CountDownLatch(latchNum);//20% of 1/4 threads;  check round up

        for (int i = 0; i < numThreads; i++) {
            int idStart = numSkiers / numThreads * i + 1;
            int idEnd = numSkiers / numThreads * (i + 1);
            int phaseStart = 1;
            int phaseEnd = 90;
            int scheduledTimes = (meanRuns / 5) * (numSkiers / numThreads);
            Thread t = new Thread(new QueryProcess(latch1, ip, idStart, idEnd, phaseStart, phaseEnd, scheduledTimes));
            threadsReg.add(t);
            t.start();
        }
        try {
            latch1.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("complete phase 1: " + (System.currentTimeMillis() - startTime) + " milliseconds");

        //enter PEAK phase
        numThreads = totalThreads;
        latchNum = numThreads % 5 == 0 ? numThreads / 5 : numThreads / 5 + 1;
        CountDownLatch latch2 = new CountDownLatch(latchNum);//20% of total threads
        for (int i = 0; i < numThreads; i++) {
            int idStart = numSkiers / numThreads * i + 1;
            int idEnd = numSkiers / numThreads * (i + 1);
            int phaseStart = 91;
            int phaseEnd = 360;
            int scheduledTimes = (meanRuns * 3 / 5) * (numSkiers / numThreads);
            Thread t = new Thread(new QueryProcess(latch2, ip, idStart, idEnd, phaseStart, phaseEnd, scheduledTimes));
            threadsReg.add(t);
            t.start();
        }
        try {
            latch2.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("complete phase 2: " + (System.currentTimeMillis() - startTime) + " milliseconds");

        //enter COOL-DOWN phase
        numThreads = totalThreads / 10;
        CountDownLatch latch3 = new CountDownLatch(numThreads / 5);//20% of total threads
        for (int i = 0; i < numThreads; i++) {
            int idStart = numSkiers / numThreads * i + 1;
            int idEnd = numSkiers / numThreads * (i + 1);
            int phaseStart = 361;
            int phaseEnd = 420;
            int scheduledTimes = (meanRuns / 5) * (numSkiers / numThreads);
            Thread t = new Thread(new QueryProcess(latch3, ip, idStart, idEnd, phaseStart, phaseEnd, scheduledTimes));
            threadsReg.add(t);
            t.start();
        }
        System.out.println("complete phase 3: " + (System.currentTimeMillis() - startTime) + " milliseconds");

        //wait for all threads to complete
        for (Thread t : threadsReg) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long wallTime = System.currentTimeMillis() - startTime;
        System.out.println("all complete: " + wallTime + " milliseconds");

        //write to a record .csv file
        File dir = new File("src/main/resources/output");
        if (!dir.isDirectory()) dir.mkdirs();
        File f = new File("src/main/resources/output/record.csv");
        try {
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(f)
                    )
            );
            writer.write("methodType, responseCode, startTime, latency\n");
            for (ResponseRecord responseRecord : wholeList) {
                writer.write(responseRecord.toString());
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //calculate mean, median, p99, etc
        int size = wholeList.size();
        System.out.println("\ndata size: " + size);
        ArrayList<Long> latencies = new ArrayList<>();
        Long sum = 0L;
        int successNum = 0;
        int failNum = 0;
        for (ResponseRecord r : wholeList) {
            latencies.add(r.getLatency());
            sum += r.getLatency();
            if(r.getResponseCode()!=201)failNum++;
            else successNum++;
        }
        System.out.println("successes: "+successNum);
        System.out.println("fail: "+failNum);
        System.out.println("total wall time: "+wallTime);
        System.out.println("throughput/s: "+size/(wallTime/1000));
        System.out.println("mean: " + sum / size + " milliseconds");
        Collections.sort(latencies);
        System.out.println("median: " + latencies.get(size / 2) + " milliseconds");
        System.out.println("p99: " + latencies.get(size * 99 / 100) + " milliseconds");
        System.out.println("min: " + latencies.get(0) + " milliseconds");
        System.out.println("max: " + latencies.get(latencies.size()-1) + " milliseconds");

    }


}
