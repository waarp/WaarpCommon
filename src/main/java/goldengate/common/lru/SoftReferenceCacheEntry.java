/**
 * This file is part of GoldenGate Project (named also GoldenGate or GG).
 * 
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author
 * tags. See the COPYRIGHT.txt in the distribution for a full listing of
 * individual contributors.
 * 
 * All GoldenGate Project is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * GoldenGate is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * GoldenGate . If not, see <http://www.gnu.org/licenses/>.
 */
package goldengate.common.lru;

import java.lang.ref.SoftReference;

/**
 * Cache entry which uses SoftReference to store value
 * 
 * @author Frederic Bregier
 * @author Damian Momot
 */
class SoftReferenceCacheEntry<V> implements InterfaceLruCacheEntry<V> {

    private final SoftReference<V> valueReference;

    private final long expirationTime;

    /**
     * Creates LruCacheEntry with desired ttl
     * 
     * @param value
     * @param ttl
     *            time to live in milliseconds
     * @throws IllegalArgumentException
     *             if ttl is not positive
     */
    SoftReferenceCacheEntry(V value, long ttl) {
        if (ttl <= 0)
            throw new IllegalArgumentException("ttl must be positive");

        valueReference = new SoftReference<V>(value);
        expirationTime = System.currentTimeMillis() + ttl;
    }

    /**
     * Returns value if entry is valid, null otherwise.
     * 
     * Entry is invalid if SoftReference is cleared or entry has expired
     * 
     * @return value if entry is valid
     */
    @Override
    public V getValue() {
        V value = null;

        // check expiration time
        if (System.currentTimeMillis() <= expirationTime) {
            value = valueReference.get();

            // if (value == null)
            // logger.warn("SoftReferency.get() returned null - probably JVM runs out of memory");
        }

        return value;
    }

    @Override
    public boolean isStillValid(long timeRef) {
        return (timeRef <= expirationTime);
    }

}
