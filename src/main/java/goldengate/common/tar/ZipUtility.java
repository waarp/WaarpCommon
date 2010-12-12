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
package goldengate.common.tar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

/**
 * @author Frederic Bregier
 *
 */
public class ZipUtility {
    /**
     * Create a new Zip from a root directory
     * @param directory the base directory
     * @param filename the output filename
     * @param absolute store absolute filepath (from directory) or only filename
     * @return True if OK
     */
    public static boolean createZipFromDirectory(String directory, String filename, boolean absolute) {
        File rootDir = new File(directory);
        File saveFile = new File(filename);
        // recursive call
        ZipArchiveOutputStream zaos;
        try {
            zaos = new ZipArchiveOutputStream(new FileOutputStream(saveFile));
        } catch (FileNotFoundException e) {
            return false;
        }
        try {
            recurseFiles(rootDir, rootDir, zaos, absolute);
        } catch (IOException e2) {
            try {
                zaos.close();
            } catch (IOException e) {
                // ignore
            }
            return false;
        }
        try {
            zaos.finish();
        } catch (IOException e1) {
            // ignore
        }
        try {
            zaos.flush();
        } catch (IOException e) {
            // ignore
        }
        try {
            zaos.close();
        } catch (IOException e) {
            // ignore
        }
        return true;
    }
    /**
     * Recursive traversal to add files
     * @param root
     * @param file
     * @param zaos
     * @param absolute
     * @throws IOException 
     */
    private static void recurseFiles(File root, File file, ZipArchiveOutputStream zaos, boolean absolute) throws IOException {
        if (file.isDirectory()) {
            // recursive call
            File [] files = file.listFiles();
            for (File file2: files) {
                recurseFiles(root, file2, zaos, absolute);
            }
        } else if ((! file.getName().endsWith(".zip")) && (! file.getName().endsWith(".ZIP"))) {
            String filename = null;
            if (absolute) {
                filename = file.getAbsolutePath().substring(root.getAbsolutePath().length());
            } else {
                filename = file.getName();
            }
            ZipArchiveEntry zae = new ZipArchiveEntry(filename);
            zae.setSize(file.length());
            zaos.putArchiveEntry(zae);
            FileInputStream fis = new FileInputStream(file);
            IOUtils.copy(fis, zaos);
            zaos.closeArchiveEntry();
        }
    }
    /**
     * Create a new Zip from a list of Files (only name of files will be used)
     * @param files list of files to add
     * @param filename the output filename
     * @return True if OK
     */
    public static boolean createZipFromFiles(List<File> files, String filename) {
        File saveFile = new File(filename);
        ZipArchiveOutputStream zaos;
        try {
            zaos = new ZipArchiveOutputStream(new FileOutputStream(saveFile));
        } catch (FileNotFoundException e) {
            return false;
        }
        for (File file: files) {
            try {
                addFile(file, zaos);
            } catch (IOException e) {
                try {
                    zaos.close();
                } catch (IOException e1) {
                    // ignore
                }
                return false;
            }
        }
        try {
            zaos.finish();
        } catch (IOException e1) {
            // ignore
        }
        try {
            zaos.flush();
        } catch (IOException e) {
            // ignore
        }
        try {
            zaos.close();
        } catch (IOException e) {
            // ignore
        }
        return true;
    }
    /**
     * Create a new Zip from an array of Files (only name of files will be used)
     * @param files array of files to add
     * @param filename the output filename
     * @return True if OK
     */
    public static boolean createZipFromFiles(File[] files, String filename) {
        File saveFile = new File(filename);
        ZipArchiveOutputStream zaos;
        try {
            zaos = new ZipArchiveOutputStream(new FileOutputStream(saveFile));
        } catch (FileNotFoundException e) {
            return false;
        }
        for (File file: files) {
            try {
                addFile(file, zaos);
            } catch (IOException e) {
                try {
                    zaos.close();
                } catch (IOException e1) {
                    // ignore
                }
                return false;
            }
        }
        try {
            zaos.finish();
        } catch (IOException e1) {
            // ignore
        }
        try {
            zaos.flush();
        } catch (IOException e) {
            // ignore
        }
        try {
            zaos.close();
        } catch (IOException e) {
            // ignore
        }
        return true;
    }
    /**
     * Recursive traversal to add files
     * @param file
     * @param zaos
     * @throws IOException 
     */
    private static void addFile(File file, ZipArchiveOutputStream zaos) throws IOException {
        String filename = null;
        filename = file.getName();
        ZipArchiveEntry zae = new ZipArchiveEntry(filename);
        zae.setSize(file.length());
        zaos.putArchiveEntry(zae);
        FileInputStream fis = new FileInputStream(file);
        IOUtils.copy(fis, zaos);
        zaos.closeArchiveEntry();
    }
}
