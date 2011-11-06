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
package goldengate.common.exception;

/**
 * Exception for Finite State Machine support
 * 
 * @author Frederic Bregier
 *
 */
public class IllegalFiniteStateException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 8731284958857363751L;


    /**
     * 
     */
    public IllegalFiniteStateException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public IllegalFiniteStateException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param s
     */
    public IllegalFiniteStateException(String s) {
        super(s);
    }

    /**
     * @param cause
     */
    public IllegalFiniteStateException(Throwable cause) {
        super(cause);
    }

}