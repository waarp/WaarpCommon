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
package org.waarp.common.role;

/**
 * Role to be used in Waarp projects
 * 
 * FIXME not functional for the moment: early stage development
 * 
 * @author Frederic Bregier
 *
 */
public class RoleDefault {
    public static byte NOACCESS = ((byte) 0);
    public static byte READONLY = ((byte) 1); 
    public static byte TRANSFER = ((byte) 2);
    public static byte RULE = ((byte) 4); 
    public static byte HOST = ((byte) 8);
    public static byte LIMIT = ((byte) 16); 
    public static byte SYSTEM = ((byte) 32);
    public static byte UNUSED1 = ((byte) 64);
    public static byte UNUSED2 = ((byte) -128);
    
    private byte role;
    
    public RoleDefault() {
        this.role = NOACCESS;
    }
    
    public void addRole(byte newrole) {
        this.role |= newrole;
    }
    
    public void setRole(byte newrole) {
        this.role = newrole;
    }
    
    public void clear() {
        this.role = NOACCESS;
    }
    
    public byte getRole() {
        return this.role;
    }
    public boolean hasReadOnly() {
        return (role&READONLY)!=0;
    }
    public boolean hasTransfer() {
        return (role&TRANSFER)!=0;
    }
    public boolean hasRule() {
        return (role&RULE)!=0;
    }
    public boolean hasHost() {
        return (role&HOST)!=0;
    }
    public boolean hasLimit() {
        return (role&LIMIT)!=0;
    }
    public boolean hasSystem() {
        return (role&SYSTEM)!=0;
    }
    public boolean hasUnused1() {
        return (role&UNUSED1)!=0;
    }
    public boolean hasUnused2() {
        return (role&UNUSED2)!=0;
    }
    
    
    public static boolean HasReadOnly(byte role) {
        return (role&READONLY)!=0;
    }
    public static boolean HasTransfer(byte role) {
        return (role&TRANSFER)!=0;
    }
    public static boolean HasRule(byte role) {
        return (role&RULE)!=0;
    }
    public static boolean HasHost(byte role) {
        return (role&HOST)!=0;
    }
    public static boolean HasLimit(byte role) {
        return (role&LIMIT)!=0;
    }
    public static boolean HasSystem(byte role) {
        return (role&SYSTEM)!=0;
    }
    public static boolean HasUnused1(byte role) {
        return (role&UNUSED1)!=0;
    }
    public static boolean HasUnused2(byte role) {
        return (role&UNUSED2)!=0;
    }
    
}
