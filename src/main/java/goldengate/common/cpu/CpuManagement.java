/**
   This file is part of GoldenGate Project (named also GoldenGate or GG).

   Copyright 2009, Frederic Bregier, and individual contributors by the @author
   tags. See the COPYRIGHT.txt in the distribution for a full listing of
   individual contributors.

   All GoldenGate Project is free software: you can redistribute it and/or 
   modify it under the terms of the GNU General Public License as published 
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   GoldenGate is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with GoldenGate .  If not, see <http://www.gnu.org/licenses/>.
 */
package goldengate.common.cpu;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * @author bregier
 * 
 */
public class CpuManagement implements CpuManagementInterface {
    OperatingSystemMXBean osBean;

    /**
     * 
     * @throws UnsupportedOperationException
     *             if System Load Average is not supported
     */
    public CpuManagement() throws UnsupportedOperationException {
        osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean.getSystemLoadAverage() < 0) {
            osBean = null;
            throw new UnsupportedOperationException(
                    "System Load Average not supported");
        }
    }

    /**
     * 
     * @return the load average
     */
    public double getLoadAverage() {
        return osBean.getSystemLoadAverage();
    }

    public static void main(String[] args) {
        long total = 0;
        CpuManagement cpuManagement = new CpuManagement();
        System.err.println("LA: " + cpuManagement.getLoadAverage());
        for (int i = 0; i < 1000 * 1000 * 1000; i ++) {
            // keep ourselves busy for a while ...
            // note: we had to add some "work" into the loop or Java 6
            // optimizes it away. Thanks to Daniel Einspanjer for
            // pointing that out.
            total += i;
            total *= 10;
        }
        System.err.println("LA: " + cpuManagement.getLoadAverage());

        total = 0;
        for (int i = 0; i < 1000 * 1000 * 1000; i ++) {
            // keep ourselves busy for a while ...
            // note: we had to add some "work" into the loop or Java 6
            // optimizes it away. Thanks to Daniel Einspanjer for
            // pointing that out.
            total += i;
            total *= 10;
        }
        System.err.println("LA: " + cpuManagement.getLoadAverage());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
        }
        System.err.println("LA: " + cpuManagement.getLoadAverage());

        total = 0;
        for (int i = 0; i < 1000 * 1000 * 1000; i ++) {
            // keep ourselves busy for a while ...
            // note: we had to add some "work" into the loop or Java 6
            // optimizes it away. Thanks to Daniel Einspanjer for
            // pointing that out.
            total += i;
            total *= 10;
        }
        System.err.println("LA: " + cpuManagement.getLoadAverage());
    }
}
