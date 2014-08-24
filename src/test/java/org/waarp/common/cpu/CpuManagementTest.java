package org.waarp.common.cpu;

import static org.junit.Assert.*;

import org.junit.Test;

public class CpuManagementTest {

    @Test
    public void testGetLoadAverage() {
        long total = 0;
        CpuManagement cpuManagement = new CpuManagement();
        double max = 0.0;
        System.err.println("LA: " + cpuManagement.getLoadAverage());
        for (int i = 0; i < 1000 * 1000 * 1000; i++) {
            // keep ourselves busy for a while ...
            // note: we had to add some "work" into the loop or Java 6
            // optimizes it away. Thanks to Daniel Einspanjer for
            // pointing that out.
            total += i;
            total *= 10;
        }
        if (total <= 0)
            System.out.println(total);
        System.err.println("LA: " + cpuManagement.getLoadAverage());
        total = 0;
        for (int i = 0; i < 1000 * 1000 * 1000; i++) {
            // keep ourselves busy for a while ...
            // note: we had to add some "work" into the loop or Java 6
            // optimizes it away. Thanks to Daniel Einspanjer for
            // pointing that out.
            total += i;
            total *= 10;
        }
        if (total <= 0)
            System.out.println(total);
        max = cpuManagement.getLoadAverage();
        System.err.println("LA: " + max);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
        double min = cpuManagement.getLoadAverage();
        System.err.println("LA: " + min);
        // Not checking since not as precise: assertTrue("Max > current: " + max + " >? " + min, max > min);

        total = 0;
        for (int i = 0; i < 1000 * 1000 * 1000 * 1000; i++) {
            // keep ourselves busy for a while ...
            // note: we had to add some "work" into the loop or Java 6
            // optimizes it away. Thanks to Daniel Einspanjer for
            // pointing that out.
            total += i;
            total *= 10;
        }
        if (total <= 0)
            System.out.println(total);
        max = cpuManagement.getLoadAverage();
        System.err.println("LA: " + max);
        // Not checking since not as precise: assertTrue("Min < current: " + min + " <? " + max, max >= min);
    }

    @Test
    public void testSysmonGetLoadAverage() {
        long total = 0;
        CpuManagementSysmon cpuManagement = new CpuManagementSysmon();
        double max = 0.0;
        System.err.println("LAs: " + cpuManagement.getLoadAverage());
        for (int i = 0; i < 1000 * 1000 * 1000; i++) {
            // keep ourselves busy for a while ...
            // note: we had to add some "work" into the loop or Java 6
            // optimizes it away. Thanks to Daniel Einspanjer for
            // pointing that out.
            total += i;
            total *= 10;
        }
        if (total <= 0)
            System.out.println(total);
        System.err.println("LAs: " + cpuManagement.getLoadAverage());
        total = 0;
        for (int i = 0; i < 1000 * 1000 * 1000; i++) {
            // keep ourselves busy for a while ...
            // note: we had to add some "work" into the loop or Java 6
            // optimizes it away. Thanks to Daniel Einspanjer for
            // pointing that out.
            total += i;
            total *= 10;
        }
        if (total <= 0)
            System.out.println(total);
        max = cpuManagement.getLoadAverage();
        System.err.println("LAs: " + max);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        double min = cpuManagement.getLoadAverage();
        System.err.println("LAs: " + min);
        assertTrue("Max > current: " + max + " >? " + min, max > min);

        total = 0;
        for (int i = 0; i < 1000 * 1000 * 1000; i++) {
            // keep ourselves busy for a while ...
            // note: we had to add some "work" into the loop or Java 6
            // optimizes it away. Thanks to Daniel Einspanjer for
            // pointing that out.
            total += i;
            total *= 10;
        }
        if (total <= 0)
            System.out.println(total);
        max = cpuManagement.getLoadAverage();
        System.err.println("LAs: " + max);
        assertTrue("Min < current: " + min + " <? " + max, max > min);
    }

}
