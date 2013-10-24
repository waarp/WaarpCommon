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

import org.waarp.common.filemonitor.FileMonitor.FileItem;

/**
 * Command run when a new file item is validated
 * @author "Frederic Bregier"
 *
 */
public abstract class FileMonitorCommandRunnableFuture implements Runnable {
	public FileItem fileItem;
	private Thread currentThread;
	
	/**
	 * @param specialId
	 * @param file
	 * @param fileItem
	 */
	public FileMonitorCommandRunnableFuture(FileItem fileItem) {
		this.fileItem = fileItem;
	}
	
	/**
	 * This must be overridden and calling super.run() in the very beginning
	 */
	@Override
	public void run() {
		currentThread = Thread.currentThread();
	}

	protected void finalize(boolean status) {
		if (status) {
			fileItem.used = true;
			fileItem.hash = null;
		} else {
			// execution in error, will retry later on
			fileItem.used = false;
			fileItem.hash = null;
		}
	}
	
	public void cancel() {
		if (currentThread != null) {
			currentThread.interrupt();
		}
	}
}
