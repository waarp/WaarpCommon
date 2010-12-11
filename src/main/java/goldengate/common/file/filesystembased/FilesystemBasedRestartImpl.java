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
package goldengate.common.file.filesystembased;

import goldengate.common.exception.NoRestartException;
import goldengate.common.file.Restart;
import goldengate.common.file.SessionInterface;

/**
 * Restart implementation for Filesystem Based
 *
 * @author Frederic Bregier
 *
 */
public abstract class FilesystemBasedRestartImpl extends Restart {
    /**
     * Valid Position for the next current file
     */
    protected long position = -1;

    /**
     * @param session
     */
    public FilesystemBasedRestartImpl(SessionInterface session) {
        super(session);
    }

    @Override
    public long getPosition() throws NoRestartException {
        if (isSet()) {
            setSet(false);
            return position;
        }
        throw new NoRestartException("Restart is not set");
    }
}
