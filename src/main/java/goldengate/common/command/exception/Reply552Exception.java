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
package goldengate.common.command.exception;

import goldengate.common.command.ReplyCode;

/**
 * 552 Requested file action aborted. Exceeded storage allocation (for current
 * directory or dataset).
 *
 * @author Frederic Bregier
 *
 */
public class Reply552Exception extends CommandAbstractException {

    /**
     * serialVersionUID of long:
     */
    private static final long serialVersionUID = 552L;

    /**
     * 552 Requested file action aborted. Exceeded storage allocation (for
     * current directory or dataset).
     *
     * @param message
     */
    public Reply552Exception(String message) {
        super(
                ReplyCode.REPLY_552_REQUESTED_FILE_ACTION_ABORTED_EXCEEDED_STORAGE,
                message);
    }

}
