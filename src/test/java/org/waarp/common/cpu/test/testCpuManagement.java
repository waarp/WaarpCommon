/**
 * This file is part of Waarp Project.
 * 
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author tags. See the
 * COPYRIGHT.txt in the distribution for a full listing of individual contributors.
 * 
 * All Waarp Project is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Waarp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Waarp . If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.waarp.common.cpu.test;

import org.waarp.common.cpu.CpuManagement;

/**
 * @author "Frederic Bregier"
 * 
 */
public class testCpuManagement {

    public static void main(String[] args) {
        long total = 0;
        CpuManagement cpuManagement;
        try {
            cpuManagement = new CpuManagement();
        } catch (UnsupportedOperationException e) {
            System.err.println(e);
            return;
        }
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
        System.err.println("LA: " + cpuManagement.getLoadAverage());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {}
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
        System.err.println("LA: " + cpuManagement.getLoadAverage());
    }

}
