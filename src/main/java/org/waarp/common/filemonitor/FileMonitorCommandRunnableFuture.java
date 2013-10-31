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
	 */
	public FileMonitorCommandRunnableFuture() {
	}

	/**
	 * @param fileItem
	 */
	public FileMonitorCommandRunnableFuture(FileItem fileItem) {
		this.fileItem = fileItem;
	}

	public void setFileItem(FileItem fileItem) {
		this.fileItem = fileItem;
	}

	@Override
	public void run() {
		currentThread = Thread.currentThread();
		if (fileItem != null) {
			run(fileItem);
		}
	}
	/**
	 * 
	 * @param fileItem fileItem on which the command will be executed.
	 * @return True if the execution is successful
	 */
	public abstract void run(FileItem fileItem);
	/**
	 * To be called at the end of the primary action (only for commandValidFile).
	 * @param status
	 * @param specialId the specialId associated with the task
	 */
	protected void finalize(boolean status, long specialId) {
		if (status) {
			fileItem.used = true;
			fileItem.hash = null;
			fileItem.specialId = specialId;
		} else {
			// execution in error, will retry later on
			fileItem.used = false;
			fileItem.hash = null;
			fileItem.specialId = specialId;
		}
	}
	
	public void cancel() {
		if (currentThread != null) {
			currentThread.interrupt();
		}
	}
}
