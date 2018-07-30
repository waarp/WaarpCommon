
package org.waarp.common.file;

import org.junit.Test;
import static org.junit.Assert.*;

public class AbstractDirTest {
    
    @Test
    public void testNormalizePath() {
        assertEquals("single relative filename", "test.dat", AbstractDir.normalizePath("test.dat"));
        assertEquals("relative path (unix)", "foo/test.dat", AbstractDir.normalizePath("foo/test.dat"));
        assertEquals("absolute path (unix)", "/foo/test.dat", AbstractDir.normalizePath("/foo/test.dat"));
        assertEquals("absolute uri (unix)", "file:///foo/test.dat", AbstractDir.normalizePath("file:///foo/test.dat"));
        assertEquals("relative uri (unix)", "file://foo/test.dat", AbstractDir.normalizePath("file://foo/test.dat"));

        assertEquals("relative path (win)", "foo/test.dat", AbstractDir.normalizePath("foo\\test.dat"));
        assertEquals("absolute path (win)", "c:/foo/test.dat", AbstractDir.normalizePath("c:\\foo\\test.dat"));
        assertEquals("absolute uri (win)", "file:///c:/foo/test.dat", AbstractDir.normalizePath("file:///c:/foo/test.dat"));
        assertEquals("relative uri (win)", "file://foo/test.dat", AbstractDir.normalizePath("file://foo/test.dat"));
        assertEquals("UNC path (win)", "//server/share/test.dat", AbstractDir.normalizePath("\\\\server\\share\\test.dat"));
        assertEquals("UNC uri (win)", "file:////server/share/test.dat", AbstractDir.normalizePath("file:////server/share/test.dat"));
    }

    @Test
    public void testPathFromUri() {
        assertEquals("single relative filename", "test.dat", AbstractDir.pathFromURI("test.dat"));
        assertEquals("relative path (unix)", "foo/test.dat", AbstractDir.pathFromURI("foo/test.dat"));
        assertEquals("absolute path (unix)", "/foo/test.dat", AbstractDir.pathFromURI("/foo/test.dat"));
        assertEquals("absolute uri (unix)", "/foo/test.dat", AbstractDir.pathFromURI("file:///foo/test.dat"));
        assertEquals("relative uri (unix)", "foo/test.dat", AbstractDir.pathFromURI("file://foo/test.dat"));

        assertEquals("relative path (win)", "foo/test.dat", AbstractDir.pathFromURI("foo/test.dat"));
        assertEquals("absolute path (win)", "c:/foo/test.dat", AbstractDir.pathFromURI("c:/foo/test.dat"));
        assertEquals("absolute uri (win)", "c:/foo/test.dat", AbstractDir.pathFromURI("file:///c:/foo/test.dat"));
        assertEquals("relative uri (win)", "foo/test.dat", AbstractDir.pathFromURI("file://foo/test.dat"));
        assertEquals("UNC path (win)", "//server/share/test.dat", AbstractDir.pathFromURI("//server/share/test.dat"));
        assertEquals("UNC uri (win)", "//server/share/test.dat", AbstractDir.pathFromURI("file:////server/share/test.dat"));

        assertEquals("url to decode", "//server/share/béah @;tré", AbstractDir.pathFromURI("file:////server/share/b%C3%A9ah%20%40%3Btr%C3%A9"));
        assertEquals("filename with percent sign", "/tmp/100% accuracy.txt", AbstractDir.pathFromURI("file:///tmp/100% accuracy.txt"));
    }
}
