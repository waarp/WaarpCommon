/**
   This file is part of Waarp Project.

   Copyright 2009, Frederic Bregier, and individual contributors by the @author
   tags. See the COPYRIGHT.txt in the distribution for a full listing of
   individual contributors.

   All Waarp Project is free software: you can redistribute it and/or 
   modify it under the terms of the GNU General Public License as published 
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Waarp is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Waarp .  If not, see <http://www.gnu.org/licenses/>.
 */
package org.waarp.common.utility;


import java.lang.management.ManagementFactory;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * UUID Generator (also Global UUID Generator) <br>
 * <br>
 * Inspired from com.groupon locality-uuid which used combination of internal counter value - process id - 
 * fragment of MAC address and Timestamp. see https://github.com/groupon/locality-uuid.java <br>
 * <br>
 * But force sequence and take care of errors and improves some performance issues
 *  
 * @author "Frederic Bregier"
 *
 */
public final class UUID {
	
    /**
     * Random Generator 
     */
    private static final ThreadLocalRandom RANDOM			= ThreadLocalRandom.current();
    /**
     * So MAX value on 2 bytes
     */
    private static final int MAX_PID			= 65536;
    /**
     * Version to store (to check correctness if future algorithm)
     */
    private static final char VERSION			= 'c';
    /**
     * HEX_CHARS
     */
    private static final char[] HEX_CHARS = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
		'a', 'b', 'c', 'd', 'e', 'f', };
    /**
     * VERSION_DEC
     */
    private static final int VERSION_DEC = asByte(VERSION, '0');
	/**
	 * 2 bytes value maximum
	 */
    private static final int JVMPID = jvmProcessId();
    /**
     * Try to get Mac Address but could be also changed dynamically
     */
    private static byte[] MAC = macAddress();
    /**
     * Counter part
     */
    private static final AtomicInteger COUNTER = new AtomicInteger(RANDOM.nextInt());
    
    /**
     * real UUID
     */
    private final byte[] uuid;

    
    /**
     * Up to the 6 first bytes will be used. If Null or less than 6 bytes, extra bytes will
     * be randomly generated.
     * 
     * @param mac the MAC address in byte format (up to the 6 first bytes will be used)
     */
    public static synchronized void setMAC(final byte []mac) {
    	if (mac == null) {
        	MAC = getRandom(6);
    	} else {
	    	MAC = Arrays.copyOf(mac, 6);
	    	for (int i = mac.length; i < 6; i++) {
	    		MAC[i] = (byte) RANDOM.nextInt(256);
	    	}
    	}
    }
    
    /**
     * Constructor that generates a new UUID using the current process id, MAC address, and timestamp
     */
    public UUID() {
        //long time = new Date().getTime();
    	final long time = System.currentTimeMillis();
        uuid = new byte[16];

        // atomically add a large prime number to the count and get the previous value
        final int count = COUNTER.incrementAndGet();

        // switch the order of the count in 3 bit segments and place into uuid
        uuid[0] = (byte) (((count & 0x0F) << 4) | ((count & 0xF0) >> 4));
        uuid[1] = (byte) (((count & 0xF00) >> 4) | ((count & 0xF000) >> 12));
        uuid[2] = (byte) (((count & 0xF0000) >> 12) | ((count & 0xF00000) >> 20));
        //uuid[3] = (byte) (((count & 0xF000000) >> 20) | ((count & 0xF0000000) >> 28));

        // copy pid to uuid
        uuid[3]  = (byte) (JVMPID >> 8);
        uuid[4]  = (byte) (JVMPID);


        // place UUID version (hex 'c') in first four bits and piece of MAC in
        // the second four bits
        uuid[5]  = (byte) (VERSION_DEC | (0x0F & MAC[1]));
        // copy rest of mac address into uuid
        uuid[6]  = MAC[2];
        uuid[7]  = MAC[3];
        uuid[8]  = MAC[4];
        uuid[9]  = MAC[5];

        // copy timestamp into uuid
        uuid[10] = (byte) (time >> 40);
        uuid[11] = (byte) (time >> 32);
        uuid[12] = (byte) (time >> 24);
        uuid[13] = (byte) (time >> 16);
        uuid[14] = (byte) (time >> 8);
        uuid[15] = (byte) (time);
    }

    /**
     * Constructor that takes a byte array as this UUID's content
     * @param bytes UUID content
     */
    public UUID(final byte[] bytes) {
        if (bytes.length != 16)
            throw new RuntimeException("Attempted to parse malformed UUID: " + Arrays.toString(bytes));

        uuid = Arrays.copyOf(bytes, 16);
    }

    public UUID(final String idsource) {
    	final String id = idsource.trim();

        if (id.length() != 36)
            throw new RuntimeException("Attempted to parse malformed UUID: " + id);

        uuid = new byte[16];
        final char[] chars = id.toCharArray();

        // Counter
        uuid[0]  = asByte(chars[0],  chars[1]);
        uuid[1]  = asByte(chars[2],  chars[3]);
        uuid[2]  = asByte(chars[4],  chars[5]);
        // PID
        uuid[3]  = asByte(chars[7],  chars[8]);
        uuid[4]  = asByte(chars[9],  chars[10]);
        // Version & MAC
        uuid[5]  = asByte(chars[12], chars[13]);
        // MAC
        uuid[6]  = asByte(chars[15], chars[16]);
        uuid[7]  = asByte(chars[17], chars[18]);
        uuid[8]  = asByte(chars[19], chars[20]);
        uuid[9]  = asByte(chars[21], chars[22]);
        // Timestamp
        uuid[10] = asByte(chars[24], chars[25]);
        uuid[11] = asByte(chars[26], chars[27]);
        uuid[12] = asByte(chars[28], chars[29]);
        uuid[13] = asByte(chars[30], chars[31]);
        uuid[14] = asByte(chars[32], chars[33]);
        uuid[15] = asByte(chars[34], chars[35]);
    }

    private static final byte asByte(char a, char b) {
		if (a >= HEX_CHARS[10]) {
			a -= HEX_CHARS[10] - 10;
		} else {
			a -= HEX_CHARS[0];
		}
		if (b >= HEX_CHARS[10]) {
			b -= HEX_CHARS[10] - 10;
		} else {
			b -= HEX_CHARS[0];
		}
		return (byte) ((a << 4) + b);
    }

    @Override
    public String toString() {
    	final char[] id = new char[36];

        // split each byte into 4 bit numbers and map to hex characters
        // Counter
        id[0]  = HEX_CHARS[(uuid[0]  & 0xF0) >> 4];
        id[1]  = HEX_CHARS[(uuid[0]  & 0x0F)];
        id[2]  = HEX_CHARS[(uuid[1]  & 0xF0) >> 4];
        id[3]  = HEX_CHARS[(uuid[1]  & 0x0F)];
        id[4]  = HEX_CHARS[(uuid[2]  & 0xF0) >> 4];
        id[5]  = HEX_CHARS[(uuid[2]  & 0x0F)];
        id[6]  = '-';
        // PID
        id[7]  = HEX_CHARS[(uuid[3]  & 0xF0) >> 4];
        id[8]  = HEX_CHARS[(uuid[3]  & 0x0F)];
        id[9]  = HEX_CHARS[(uuid[4]  & 0xF0) >> 4];
        id[10] = HEX_CHARS[(uuid[4]  & 0x0F)];
        id[11] = '-';
        // Version & Timestamp
        id[12] = HEX_CHARS[(uuid[5]  & 0xF0) >> 4];
        id[13] = HEX_CHARS[(uuid[5]  & 0x0F)];
        id[14]  = '-';
        // MAC
        id[15] = HEX_CHARS[(uuid[6]  & 0xF0) >> 4];
        id[16] = HEX_CHARS[(uuid[6]  & 0x0F)];
        id[17] = HEX_CHARS[(uuid[7]  & 0xF0) >> 4];
        id[18] = HEX_CHARS[(uuid[7]  & 0x0F)];
        id[19] = HEX_CHARS[(uuid[8]  & 0xF0) >> 4];
        id[20] = HEX_CHARS[(uuid[8]  & 0x0F)];
        id[21] = HEX_CHARS[(uuid[9]  & 0xF0) >> 4];
        id[22] = HEX_CHARS[(uuid[9]  & 0x0F)];
        id[23] = '-';
        // Timestamp
        id[24] = HEX_CHARS[(uuid[10] & 0xF0) >> 4];
        id[25] = HEX_CHARS[(uuid[10] & 0x0F)];
        id[26] = HEX_CHARS[(uuid[11] & 0xF0) >> 4];
        id[27] = HEX_CHARS[(uuid[11] & 0x0F)];
        id[28] = HEX_CHARS[(uuid[12] & 0xF0) >> 4];
        id[29] = HEX_CHARS[(uuid[12] & 0x0F)];
        id[30] = HEX_CHARS[(uuid[13] & 0xF0) >> 4];
        id[31] = HEX_CHARS[(uuid[13] & 0x0F)];
        id[32] = HEX_CHARS[(uuid[14] & 0xF0) >> 4];
        id[33] = HEX_CHARS[(uuid[14] & 0x0F)];
        id[34] = HEX_CHARS[(uuid[15] & 0xF0) >> 4];
        id[35] = HEX_CHARS[(uuid[15] & 0x0F)];

        return new String(id);
    }

    /**
     * copy the uuid of this UUID, so that it can't be changed, and return it
     * @return raw byte array of UUID
     */
    public byte[] getBytes() {
        return Arrays.copyOf(uuid, 16);
    }

    /**
     * extract version field as a hex char from raw UUID bytes
     * @return version char
     */
    public char getVersion() {
        return HEX_CHARS[(uuid[5] & 0xF0) >> 4];
    }

    /**
     * extract process id from raw UUID bytes and return as int
     * @return id of process that generated the UUID, or -1 for unrecognized format
     */
    public int getProcessId() {
        if (getVersion() != VERSION)
            return -1;

        return ((uuid[3] & 0xFF) << 8) | (uuid[4] & 0xFF);
    }

    /**
     * extract timestamp from raw UUID bytes and return as int
     * @return millisecond UTC timestamp from generation of the UUID, or -1 for unrecognized format
     */
    public long getTimestamp() {
        if (getVersion() != VERSION)
            return -1;

        long time;
        time  = ((long)uuid[10] & 0xFF) << 40;
        time |= ((long)uuid[11] & 0xFF) << 32;
        time |= ((long)uuid[12] & 0xFF) << 24;
        time |= ((long)uuid[13] & 0xFF) << 16;
        time |= ((long)uuid[14] & 0xFF) << 8;
        time |= ((long)uuid[15] & 0xFF);
        return time;
    }

    /**
     * extract MAC address fragment from raw UUID bytes, setting missing values to 0,
     * thus the first 3 bytes will be 0, followed by 3 bytes
     * of the active MAC address when the UUID was generated
     * @return byte array of UUID fragment, or null for unrecognized format
     */
    public byte[] getMacFragment() {
        if (getVersion() != VERSION)
            return null;

        final byte[] x = new byte[6];

        x[0] = 0;
        x[1] = (byte) (uuid[5] & 0x0F);
        x[2] = uuid[6];
        x[3] = uuid[7];
        x[4] = uuid[8];
        x[5] = uuid[9];

        return x;
    }

    @Override
    public boolean equals(Object o) {
    	if (o == null || !(o instanceof UUID)) return false;
        return (this == o) || Arrays.equals(this.uuid, ((UUID) o).uuid);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(uuid);
    }

    /**
     * 
     * @param length
     * @return a byte array with random values
     */
    public static final byte[] getRandom(final int length) {
    	final byte[] result = new byte[length];
    	for (int i = 0; i < length; i++) {
    		result[i] = (byte) RANDOM.nextInt(256);
    	}
    	return result;
    }
    
    /**
     * 
     * @return the mac address if possible, else random values
     */
    private static byte[] macAddress() {
        try {
        	byte[] mac = null;
            Enumeration<NetworkInterface> enumset = NetworkInterface.getNetworkInterfaces();
            while (enumset.hasMoreElements()) {
            	mac = enumset.nextElement().getHardwareAddress();
            	if (mac != null && mac.length >= 6) {
            		break;
            	} else {
            		mac = null;
            	}
            }
            // if the machine is not connected to a network it has no active MAC address
            if (mac == null || mac.length < 6) {
            	System.err.println("No MAC Address found");
                mac = getRandom(6);
            }
            return mac;
        } catch (Exception e) {
        	System.err.println("Could not get MAC address");
        	e.printStackTrace();
            return getRandom(6);
        }
    }

    // pulled from http://stackoverflow.com/questions/35842/how-can-a-java-program-get-its-own-process-id
    public static int jvmProcessId() {
        // Note: may fail in some JVM implementations
        // something like '<pid>@<hostname>', at least in SUN / Oracle JVMs
        final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final int index = jvmName.indexOf('@');

        if (index < 1) {
        	System.err.println("Could not get JVMPID");
        	return RANDOM.nextInt();
        }
        try {
            return Integer.parseInt(jvmName.substring(0, index)) % MAX_PID;
        } catch (NumberFormatException e) {
        	System.err.println("Could not get JVMPID");
        	e.printStackTrace();
        	return RANDOM.nextInt();
        }
    }
    
    @SuppressWarnings("unused")
	public static void main(String[] args) {
    	if (args.length > 0) {
    		setMAC(args[0].getBytes());
    	}
    	long pseudoMax = Long.MAX_VALUE >> 16;
    	System.out.println(new Date(pseudoMax));
        System.out.println(new UUID().toString());
        
        final int n = 100000000;

        for (int i = 0; i < n; i++) {
            UUID uuid = new UUID();
        }
        
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            UUID uuid = new UUID();
        }
        long stop = System.currentTimeMillis();
        System.out.println("TimeW = "+(stop-start)+" so "+(n*1000/(stop-start))+" Uuids/s");
        
        start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            UUID uuid = new UUID();
            uuid.toString();
        }
        stop = System.currentTimeMillis();
        System.out.println("TimeW+toString = "+(stop-start)+" so "+(n*1000/(stop-start))+" Uuids/s");

        start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            UUID uuid = new UUID();
            uuid = new UUID(uuid.toString());
        }
        stop = System.currentTimeMillis();
        System.out.println("TimeW+reloadFromtoString = "+(stop-start)+" so "+(n*1000/(stop-start))+" Uuids/s");

        int count = 0;
        start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            UUID uuid = new UUID();
            UUID uuid2 = new UUID(uuid.toString());
            if (uuid2.equals(uuid)) count++;
        }
        stop = System.currentTimeMillis();
        System.out.println("TimeWAndTest = "+(stop-start)+" so "+(n*1000/(stop-start))+" Uuids/s "+count);

        start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            UUID uuid = new UUID();
            uuid.getBytes();
        }
        stop = System.currentTimeMillis();
        System.out.println("TimeW+getBytes = "+(stop-start)+" so "+(n*1000/(stop-start))+" Uuids/s");

        start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            UUID uuid = new UUID();
            uuid = new UUID(uuid.getBytes());
        }
        stop = System.currentTimeMillis();
        System.out.println("TimeW+reloadFromgetBytes = "+(stop-start)+" so "+(n*1000/(stop-start))+" Uuids/s");

        count = 0;
        start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            UUID uuid = new UUID();
            UUID uuid2 = new UUID(uuid.getBytes());
            if (uuid2.equals(uuid)) count++;
        }
        stop = System.currentTimeMillis();
        System.out.println("TimeWAndTest = "+(stop-start)+" so "+(n*1000/(stop-start))+" Uuids/s "+count);
    }
}

