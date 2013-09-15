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
package org.waarp.common.filemonitor;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.waarp.common.digest.FilesystemBasedDigest;
import org.waarp.common.digest.FilesystemBasedDigest.DigestAlgo;
import org.waarp.common.future.WaarpFuture;
import org.waarp.common.json.AdaptativeJsonHandler;
import org.waarp.common.json.AdaptativeJsonHandler.JsonCodec;
import org.waarp.common.utility.WaarpThreadFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * This package would like to propose a JSE 6 compatible way to scan a directory
 * for new, deleted and changed files, in order to allow some functions like 
 * pooling a directory before actions.
 * 
 * @author "Frederic Bregier"
 *
 */
public class FileMonitor {
	protected static final DigestAlgo defaultDigestAlgo = DigestAlgo.MD5;
	protected static final long minimalDelay = 100;
	protected static final long defaultDelay = 1000;
	
	protected WaarpFuture future = null;
	protected WaarpFuture internalfuture = null;
	protected final File statusFile;
	protected final File stopFile;
	protected final File directory;
	protected final DigestAlgo digest;
	protected long elapseTime = defaultDelay; // default to 1s
	protected Timer timer = null;
	
	protected final HashMap<String, FileItem> fileItems = 
			new HashMap<String, FileMonitor.FileItem>();
	
	protected FileFilter filter = 
			new FileFilter() {
		public boolean accept(File pathname) {
			return pathname.isFile();
		}
	};
	protected FileMonitorCommand commandValidFile = null;
	protected FileMonitorCommand commandRemovedFile = null;
	
	protected ConcurrentLinkedQueue<FileItem> toUse = 
			new ConcurrentLinkedQueue<FileMonitor.FileItem>();
	protected final AdaptativeJsonHandler handler = new AdaptativeJsonHandler(JsonCodec.JSON);
	
	/**
	 * @param statusFile the file where the current status is saved (current files)
	 * @param stopFile the file when created (.exists()) will stop the daemon
	 * @param directory the directory where files will be monitored
	 * @param digest the digest to use (default if null is MD5)
	 * @param elapseTime the time to wait in ms for between 2 checks (default is 1000ms, minimum is 100ms)
	 * @param filter the filter to be applied on selected files (default is isFile())
	 * @param commandValidFile the commandValidFile to run (may be null, which means poll() commandValidFile has to be used)
	 * @param commandRemovedFile the commandRemovedFile to run (may be null)
	 */
	public FileMonitor(File statusFile, File stopFile,
			File directory, DigestAlgo digest, long elapseTime, 
			FileFilter filter,
			FileMonitorCommand commandValidFile, 
			FileMonitorCommand commandRemovedFile) {
		this.statusFile = statusFile;
		this.stopFile = stopFile;
		this.directory = directory;
		if (digest == null) {
			this.digest = defaultDigestAlgo;
		} else {
			this.digest = digest;
		}
		if (elapseTime >= minimalDelay) {
			this.elapseTime = (elapseTime/10)*10;
		}
		if (filter != null) {
			this.filter = filter;
		}
		this.commandValidFile = commandValidFile;
		this.commandRemovedFile = commandRemovedFile;
		this.reloadStatus();
	}

	protected void reloadStatus() {
		if (statusFile == null) return;
		if (! statusFile.exists()) return;
		try {
			HashMap<String, FileItem> newHashMap = 
					handler.mapper.readValue(statusFile, 
							new TypeReference<HashMap<String, FileItem>>() {});
			fileItems.putAll(newHashMap);
		} catch (JsonParseException e) {
		} catch (JsonMappingException e) {
		} catch (IOException e) {
		}
	}
	protected void saveStatus() {
		if (statusFile == null) return;
		try {
			handler.mapper.writeValue(statusFile, fileItems);
		} catch (JsonGenerationException e) {
		} catch (JsonMappingException e) {
		} catch (IOException e) {
		}
	}
	/**
	 * @return the elapseTime
	 */
	public long getElapseTime() {
		return elapseTime;
	}
	/**
	 * @param elapseTime the elapseTime to set
	 */
	public void setElapseTime(long elapseTime) {
		this.elapseTime = elapseTime;
	}
	/**
	 * @param filter the filter to set
	 */
	public void setFilter(FileFilter filter) {
		this.filter = filter;
	}
	
	public void start() {
		if (timer == null) {
			timer = new HashedWheelTimer(
						new WaarpThreadFactory("TimerFileMonitor"),
						100, TimeUnit.MILLISECONDS, 8);
			timer.newTimeout(new FileMonitorTimerTask(this), elapseTime, TimeUnit.MILLISECONDS);
			future = new WaarpFuture(true);
			internalfuture = new WaarpFuture(true);
		}// else already started
	}
	
	public void stop() {
		if (timer != null) {
			timer.stop();
		}
		if (internalfuture != null) {
			internalfuture.awaitUninterruptibly();
			internalfuture = null;
		}
		timer = null;
		if (future != null) {
			future.setSuccess();
		}
	}
	/**
	 * 
	 * @return the head of the File queue but does not remove it
	 */
	public File peek() {
		FileItem item = toUse.peek();
		if (item == null)
			return null;
		return item.file;
	}
	/**
	 * 
	 * @return the head of the File queue and removes it
	 */
	public File poll() {
		FileItem item = toUse.poll();
		if (item == null)
			return null;
		return item.file;
	}
	/**
	 * Wait until the Stop file is created
	 */
	public void waitForStopFile() {
		internalfuture.awaitUninterruptibly();
		stop();
	}
	/**
	 * Check Files
	 * @return False to stop
	 */
	protected boolean checkFiles() {
		boolean fileItemsChanged = false;
		if (stopFile.exists()) {
			return false;
		}
		File [] files = directory.listFiles(filter);
		for (File file : files) {
			String name = file.getName();
			FileItem fileItem = fileItems.get(name);
			if (fileItem == null) {
				// never seen until now
				fileItems.put(name, new FileItem(file));
				fileItemsChanged = true;
				continue;
			}
			if (fileItem.used) {
				// already used so ignore
				continue;
			}
			long lastTimeModified = fileItem.file.lastModified();
			if (lastTimeModified != fileItem.lastTime) {
				// changed or second time check
				fileItem.lastTime = lastTimeModified;
				fileItemsChanged = true;
				continue;
			}
			// now check Hash or third time
			try {
				byte [] hash = FilesystemBasedDigest.getHash(fileItem.file, true, digest);
				if (! Arrays.equals(hash, fileItem.hash)) {
					fileItem.hash = hash;
					fileItemsChanged = true;
					continue;
				}
				// now time and hash are the same so act on it
				fileItem.used = true;
				fileItem.timeUsed = System.currentTimeMillis();
				fileItem.hash = null;
				if (commandValidFile != null) {
					commandValidFile.run(fileItem.file);
				} else {
					toUse.add(fileItem);
				}
				fileItemsChanged = true;
			} catch (IOException e) {
				continue;
			}
		}
		// now check that all existing items are still valid
		List<FileItem> todel = new LinkedList<FileItem>();
		for (FileItem item : fileItems.values()) {
			if (item.file.isFile()) {
				continue;
			}
			todel.add(item);
		}
		// remove invalid files
		for (FileItem fileItem : todel) {
			fileItems.remove(fileItem.file.getName());
			toUse.remove(fileItem);
			if (commandRemovedFile != null) {
				commandRemovedFile.run(fileItem.file);
			}
			fileItem.file = null;
			fileItem.hash = null;
			fileItem = null;
			fileItemsChanged = true;
		}
		if (fileItemsChanged) {
			this.saveStatus();
		}
		return true;
	}
	
	/**
	 * Timer task 
	 * @author "Frederic Bregier"
	 *
	 */
	protected static class FileMonitorTimerTask implements TimerTask {
		protected final FileMonitor fileMonitor;
		/**
		 * @param fileMonitor
		 */
		protected FileMonitorTimerTask(FileMonitor fileMonitor) {
			this.fileMonitor = fileMonitor;
		}

		public void run(Timeout timeout) throws Exception {
			if (fileMonitor.checkFiles()) {
				fileMonitor.timer.newTimeout(this, fileMonitor.elapseTime, TimeUnit.MILLISECONDS);
			} else {
				fileMonitor.internalfuture.setSuccess();
			}
		}
		
	}
	
	/**
	 * One element in the directory
	 * @author "Frederic Bregier"
	 *
	 */
	protected static class FileItem {
		public File file;
		public byte[] hash = null;
		public long lastTime = Long.MIN_VALUE;
		public long timeUsed = Long.MIN_VALUE;
		public boolean used = false;
		@SuppressWarnings("unused")
		private FileItem() {
			// empty constructor for JSON
		}
		/**
		 * @param file
		 */
		protected FileItem(File file) {
			this.file = file;
		}

		@Override
		public boolean equals(Object obj) {
			return (file.equals(((FileItem) obj).file));
		}
	}

	
	public static void main(String[] args) {
    	if (args.length < 3) {
    		System.err.println("Need a statusfile, a stopfile and a directory to test");
    		return;
    	}
    	File file = new File(args[0]);
    	if (file.exists() && ! file.isFile()) {
    		System.err.println("Not a correct status file");
    		return;
    	}
    	File stopfile = new File(args[1]);
    	if (file.exists() && ! file.isFile()) {
    		System.err.println("Not a correct stop file");
    		return;
    	}
    	File dir = new File(args[2]);
    	if (! dir.isDirectory()) {
    		System.err.println("Not a directory");
    		return;
    	}
    	FileMonitor monitor = new FileMonitor(file, stopfile, dir, null, 0, 
    			new RegexFileFilter(RegexFileFilter.REGEX_XML_EXTENSION), 
    			new FileMonitorCommand() {
			public void run(File file) {
				System.out.println("File: "+file.getAbsolutePath());
			}
		}, new FileMonitorCommand() {
			public void run(File file) {
				System.err.println("File: "+file.getAbsolutePath());
			}
		});
    	monitor.start();
    	monitor.waitForStopFile();
	}
}
