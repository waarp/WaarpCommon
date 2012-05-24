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
package goldengate.common.utility;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Frederic Bregier
 *
 */
public class GgThreadFactory implements ThreadFactory {
    private String GlobalName;
    private AtomicLong counter = new AtomicLong();
    
    public GgThreadFactory(String globalName) {
        GlobalName = globalName+"-";
    }

    public Thread newThread(Runnable arg0) {
        Thread thread = new Thread(arg0);
        thread.setName(GlobalName+counter.incrementAndGet());
        return thread;
    }
}
