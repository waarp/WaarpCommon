package org.waarp.common.role;

import static org.junit.Assert.*;

import org.junit.Test;
import org.waarp.common.role.RoleDefault.ROLE;

public class RoleDefaultTest {

    private void checkRole(RoleDefault role, ROLE rolecheck) {
        switch (rolecheck) {
            case HOST:
                assertTrue(role.hasHost());
                assertFalse(role.hasLimit());
                assertFalse(role.hasLogControl());
                assertFalse(role.hasReadOnly());
                assertFalse(role.hasRule());
                assertFalse(role.hasSystem());
                assertFalse(role.hasTransfer());
                assertFalse(role.hasUnused());
                assertFalse(role.isContaining(ROLE.READONLY));
                assertFalse(role.isContaining(ROLE.TRANSFER));
                assertFalse(role.isContaining(ROLE.RULE));
                assertTrue(role.isContaining(ROLE.HOST));
                assertFalse(role.isContaining(ROLE.LIMIT));
                assertFalse(role.isContaining(ROLE.SYSTEM));
                assertFalse(role.isContaining(ROLE.LOGCONTROL));
                assertFalse(role.isContaining(ROLE.UNUSED));
                assertFalse(role.isContaining(ROLE.PARTNER));
                assertFalse(role.isContaining(ROLE.CONFIGADMIN));
                assertFalse(role.isContaining(ROLE.FULLADMIN));
                assertFalse(role.hasNoAccess());
                assertFalse(role.isContaining(ROLE.NOACCESS));
                break;
            case LIMIT:
                assertFalse(role.hasHost());
                assertTrue(role.hasLimit());
                assertFalse(role.hasLogControl());
                assertFalse(role.hasReadOnly());
                assertFalse(role.hasRule());
                assertFalse(role.hasSystem());
                assertFalse(role.hasTransfer());
                assertFalse(role.hasUnused());
                assertFalse(role.isContaining(ROLE.READONLY));
                assertFalse(role.isContaining(ROLE.TRANSFER));
                assertFalse(role.isContaining(ROLE.RULE));
                assertFalse(role.isContaining(ROLE.HOST));
                assertTrue(role.isContaining(ROLE.LIMIT));
                assertFalse(role.isContaining(ROLE.SYSTEM));
                assertFalse(role.isContaining(ROLE.LOGCONTROL));
                assertFalse(role.isContaining(ROLE.UNUSED));
                assertFalse(role.isContaining(ROLE.PARTNER));
                assertFalse(role.isContaining(ROLE.CONFIGADMIN));
                assertFalse(role.isContaining(ROLE.FULLADMIN));
                assertFalse(role.hasNoAccess());
                assertFalse(role.isContaining(ROLE.NOACCESS));
                break;
            case LOGCONTROL:
                assertFalse(role.hasHost());
                assertFalse(role.hasLimit());
                assertTrue(role.hasLogControl());
                assertFalse(role.hasReadOnly());
                assertFalse(role.hasRule());
                assertFalse(role.hasSystem());
                assertFalse(role.hasTransfer());
                assertFalse(role.hasUnused());
                assertFalse(role.isContaining(ROLE.READONLY));
                assertFalse(role.isContaining(ROLE.TRANSFER));
                assertFalse(role.isContaining(ROLE.RULE));
                assertFalse(role.isContaining(ROLE.HOST));
                assertFalse(role.isContaining(ROLE.LIMIT));
                assertFalse(role.isContaining(ROLE.SYSTEM));
                assertTrue(role.isContaining(ROLE.LOGCONTROL));
                assertFalse(role.isContaining(ROLE.UNUSED));
                assertFalse(role.isContaining(ROLE.PARTNER));
                assertFalse(role.isContaining(ROLE.CONFIGADMIN));
                assertFalse(role.isContaining(ROLE.FULLADMIN));
                assertFalse(role.hasNoAccess());
                assertFalse(role.isContaining(ROLE.NOACCESS));
                break;
            case NOACCESS:
                assertFalse(role.hasHost());
                assertFalse(role.hasLimit());
                assertFalse(role.hasLogControl());
                assertFalse(role.hasReadOnly());
                assertFalse(role.hasRule());
                assertFalse(role.hasSystem());
                assertFalse(role.hasTransfer());
                assertFalse(role.hasUnused());
                assertFalse(role.isContaining(ROLE.READONLY));
                assertFalse(role.isContaining(ROLE.TRANSFER));
                assertFalse(role.isContaining(ROLE.RULE));
                assertFalse(role.isContaining(ROLE.HOST));
                assertFalse(role.isContaining(ROLE.LIMIT));
                assertFalse(role.isContaining(ROLE.SYSTEM));
                assertFalse(role.isContaining(ROLE.LOGCONTROL));
                assertFalse(role.isContaining(ROLE.UNUSED));
                assertFalse(role.isContaining(ROLE.PARTNER));
                assertFalse(role.isContaining(ROLE.CONFIGADMIN));
                assertFalse(role.isContaining(ROLE.FULLADMIN));
                assertTrue(role.hasNoAccess());
                assertTrue(role.isContaining(ROLE.NOACCESS));
                break;
            case READONLY:
                assertFalse(role.hasHost());
                assertFalse(role.hasLimit());
                assertFalse(role.hasLogControl());
                assertTrue(role.hasReadOnly());
                assertFalse(role.hasRule());
                assertFalse(role.hasSystem());
                assertFalse(role.hasTransfer());
                assertFalse(role.hasUnused());
                assertTrue(role.isContaining(ROLE.READONLY));
                assertFalse(role.isContaining(ROLE.TRANSFER));
                assertFalse(role.isContaining(ROLE.RULE));
                assertFalse(role.isContaining(ROLE.HOST));
                assertFalse(role.isContaining(ROLE.LIMIT));
                assertFalse(role.isContaining(ROLE.SYSTEM));
                assertFalse(role.isContaining(ROLE.LOGCONTROL));
                assertFalse(role.isContaining(ROLE.UNUSED));
                assertFalse(role.isContaining(ROLE.PARTNER));
                assertFalse(role.isContaining(ROLE.CONFIGADMIN));
                assertFalse(role.isContaining(ROLE.FULLADMIN));
                assertFalse(role.hasNoAccess());
                assertFalse(role.isContaining(ROLE.NOACCESS));
                break;
            case RULE:
                assertFalse(role.hasHost());
                assertFalse(role.hasLimit());
                assertFalse(role.hasLogControl());
                assertFalse(role.hasReadOnly());
                assertTrue(role.hasRule());
                assertFalse(role.hasSystem());
                assertFalse(role.hasTransfer());
                assertFalse(role.hasUnused());
                assertFalse(role.isContaining(ROLE.READONLY));
                assertFalse(role.isContaining(ROLE.TRANSFER));
                assertTrue(role.isContaining(ROLE.RULE));
                assertFalse(role.isContaining(ROLE.HOST));
                assertFalse(role.isContaining(ROLE.LIMIT));
                assertFalse(role.isContaining(ROLE.SYSTEM));
                assertFalse(role.isContaining(ROLE.LOGCONTROL));
                assertFalse(role.isContaining(ROLE.UNUSED));
                assertFalse(role.isContaining(ROLE.PARTNER));
                assertFalse(role.isContaining(ROLE.CONFIGADMIN));
                assertFalse(role.isContaining(ROLE.FULLADMIN));
                assertFalse(role.hasNoAccess());
                assertFalse(role.isContaining(ROLE.NOACCESS));
                break;
            case SYSTEM:
                assertFalse(role.hasHost());
                assertFalse(role.hasLimit());
                assertFalse(role.hasLogControl());
                assertFalse(role.hasReadOnly());
                assertFalse(role.hasRule());
                assertTrue(role.hasSystem());
                assertFalse(role.hasTransfer());
                assertFalse(role.hasUnused());
                assertFalse(role.isContaining(ROLE.READONLY));
                assertFalse(role.isContaining(ROLE.TRANSFER));
                assertFalse(role.isContaining(ROLE.RULE));
                assertFalse(role.isContaining(ROLE.HOST));
                assertFalse(role.isContaining(ROLE.LIMIT));
                assertTrue(role.isContaining(ROLE.SYSTEM));
                assertFalse(role.isContaining(ROLE.LOGCONTROL));
                assertFalse(role.isContaining(ROLE.UNUSED));
                assertFalse(role.isContaining(ROLE.PARTNER));
                assertFalse(role.isContaining(ROLE.CONFIGADMIN));
                assertFalse(role.isContaining(ROLE.FULLADMIN));
                assertFalse(role.hasNoAccess());
                assertFalse(role.isContaining(ROLE.NOACCESS));
                break;
            case TRANSFER:
                assertFalse(role.hasHost());
                assertFalse(role.hasLimit());
                assertFalse(role.hasLogControl());
                assertFalse(role.hasReadOnly());
                assertFalse(role.hasRule());
                assertFalse(role.hasSystem());
                assertTrue(role.hasTransfer());
                assertFalse(role.hasUnused());
                assertFalse(role.isContaining(ROLE.READONLY));
                assertTrue(role.isContaining(ROLE.TRANSFER));
                assertFalse(role.isContaining(ROLE.RULE));
                assertFalse(role.isContaining(ROLE.HOST));
                assertFalse(role.isContaining(ROLE.LIMIT));
                assertFalse(role.isContaining(ROLE.SYSTEM));
                assertFalse(role.isContaining(ROLE.LOGCONTROL));
                assertFalse(role.isContaining(ROLE.UNUSED));
                assertFalse(role.isContaining(ROLE.PARTNER));
                assertFalse(role.isContaining(ROLE.CONFIGADMIN));
                assertFalse(role.isContaining(ROLE.FULLADMIN));
                assertFalse(role.hasNoAccess());
                assertFalse(role.isContaining(ROLE.NOACCESS));
                break;
            case UNUSED:
                assertFalse(role.hasHost());
                assertFalse(role.hasLimit());
                assertFalse(role.hasLogControl());
                assertFalse(role.hasReadOnly());
                assertFalse(role.hasRule());
                assertFalse(role.hasSystem());
                assertFalse(role.hasTransfer());
                assertTrue(role.hasUnused());
                assertFalse(role.isContaining(ROLE.READONLY));
                assertFalse(role.isContaining(ROLE.TRANSFER));
                assertFalse(role.isContaining(ROLE.RULE));
                assertFalse(role.isContaining(ROLE.HOST));
                assertFalse(role.isContaining(ROLE.LIMIT));
                assertFalse(role.isContaining(ROLE.SYSTEM));
                assertFalse(role.isContaining(ROLE.LOGCONTROL));
                assertTrue(role.isContaining(ROLE.UNUSED));
                assertFalse(role.isContaining(ROLE.PARTNER));
                assertFalse(role.isContaining(ROLE.CONFIGADMIN));
                assertFalse(role.isContaining(ROLE.FULLADMIN));
                assertFalse(role.hasNoAccess());
                assertFalse(role.isContaining(ROLE.NOACCESS));
                break;
            case PARTNER:
                assertFalse(role.hasHost());
                assertFalse(role.hasLimit());
                assertFalse(role.hasLogControl());
                assertTrue(role.hasReadOnly());
                assertFalse(role.hasRule());
                assertFalse(role.hasSystem());
                assertTrue(role.hasTransfer());
                assertFalse(role.hasUnused());
                assertTrue(role.isContaining(ROLE.READONLY));
                assertTrue(role.isContaining(ROLE.TRANSFER));
                assertFalse(role.isContaining(ROLE.RULE));
                assertFalse(role.isContaining(ROLE.HOST));
                assertFalse(role.isContaining(ROLE.LIMIT));
                assertFalse(role.isContaining(ROLE.SYSTEM));
                assertFalse(role.isContaining(ROLE.LOGCONTROL));
                assertFalse(role.isContaining(ROLE.UNUSED));
                assertTrue(role.isContaining(ROLE.PARTNER));
                assertFalse(role.isContaining(ROLE.CONFIGADMIN));
                assertFalse(role.isContaining(ROLE.FULLADMIN));
                assertFalse(role.hasNoAccess());
                assertFalse(role.isContaining(ROLE.NOACCESS));
                break;
            case CONFIGADMIN:
                assertTrue(role.hasHost());
                assertFalse(role.hasLimit());
                assertFalse(role.hasLogControl());
                assertTrue(role.hasReadOnly());
                assertTrue(role.hasRule());
                assertFalse(role.hasSystem());
                assertTrue(role.hasTransfer());
                assertFalse(role.hasUnused());
                assertTrue(role.isContaining(ROLE.READONLY));
                assertTrue(role.isContaining(ROLE.TRANSFER));
                assertTrue(role.isContaining(ROLE.RULE));
                assertTrue(role.isContaining(ROLE.HOST));
                assertFalse(role.isContaining(ROLE.LIMIT));
                assertFalse(role.isContaining(ROLE.SYSTEM));
                assertFalse(role.isContaining(ROLE.LOGCONTROL));
                assertFalse(role.isContaining(ROLE.UNUSED));
                assertTrue(role.isContaining(ROLE.PARTNER));
                assertTrue(role.isContaining(ROLE.CONFIGADMIN));
                assertFalse(role.isContaining(ROLE.FULLADMIN));
                assertFalse(role.hasNoAccess());
                assertFalse(role.isContaining(ROLE.NOACCESS));
                break;
            case FULLADMIN:
                assertTrue(role.hasHost());
                assertTrue(role.hasLimit());
                assertTrue(role.hasLogControl());
                assertTrue(role.hasReadOnly());
                assertTrue(role.hasRule());
                assertTrue(role.hasSystem());
                assertTrue(role.hasTransfer());
                assertFalse(role.hasUnused());
                assertTrue(role.isContaining(ROLE.READONLY));
                assertTrue(role.isContaining(ROLE.TRANSFER));
                assertTrue(role.isContaining(ROLE.RULE));
                assertTrue(role.isContaining(ROLE.HOST));
                assertTrue(role.isContaining(ROLE.LIMIT));
                assertTrue(role.isContaining(ROLE.SYSTEM));
                assertTrue(role.isContaining(ROLE.LOGCONTROL));
                assertFalse(role.isContaining(ROLE.UNUSED));
                assertTrue(role.isContaining(ROLE.PARTNER));
                assertTrue(role.isContaining(ROLE.CONFIGADMIN));
                assertTrue(role.isContaining(ROLE.FULLADMIN));
                assertFalse(role.hasNoAccess());
                assertFalse(role.isContaining(ROLE.NOACCESS));
                break;
            default:
                break;
        }
    }

    @Test
    public void testRoleDefault() {
        RoleDefault role = new RoleDefault();
        checkRole(role, ROLE.NOACCESS);
    }

    @Test
    public void testRoleDefaultROLE() {
        for (ROLE roletoset : ROLE.values()) {
            RoleDefault role = new RoleDefault(roletoset);
            checkRole(role, roletoset);
            role.clear();
            checkRole(role, ROLE.NOACCESS);
        }
    }

    @Test
    public void testAddRole() {
        for (ROLE roletoset : ROLE.values()) {
            RoleDefault role = new RoleDefault();
            checkRole(role, ROLE.NOACCESS);
            role.addRole(roletoset);
            checkRole(role, roletoset);
            role.clear();
            checkRole(role, ROLE.NOACCESS);
        }
    }

    @Test
    public void testSetRole() {
        for (ROLE roletoset : ROLE.values()) {
            RoleDefault role = new RoleDefault();
            checkRole(role, ROLE.NOACCESS);
            role.addRole(ROLE.FULLADMIN);
            checkRole(role, ROLE.FULLADMIN);
            role.setRole(roletoset);
            checkRole(role, roletoset);
        }
    }

    @Test
    public void testSetRoleDefault() {
        for (ROLE roletoset : ROLE.values()) {
            RoleDefault role = new RoleDefault();
            checkRole(role, ROLE.NOACCESS);
            role.addRole(roletoset);
            checkRole(role, roletoset);
            RoleDefault role2 = new RoleDefault();
            checkRole(role2, ROLE.NOACCESS);
            role2.setRoleDefault(role);
            checkRole(role2, roletoset);
            role.clear();
            checkRole(role, ROLE.NOACCESS);
            checkRole(role2, roletoset);
        }
    }

}
