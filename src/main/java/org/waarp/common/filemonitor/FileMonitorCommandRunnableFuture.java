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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.waarp.common.filemonitor.FileMonitor.FileItem;
import org.waarp.common.future.WaarpFuture;

/**
 * Command run when a new file item is validated
 * @author "Frederic Bregier"
 *
 */
public abstract class FileMonitorCommandRunnableFuture implements Callable<FileMonitorResult> {
	public FileMonitorResult result;
	private WaarpFuture monitorFuture = new WaarpFuture(true);
	private Thread currentThread;
	
	/**
	 * @param specialId
	 * @param file
	 * @param fileItem
	 */
	public FileMonitorCommandRunnableFuture(FileItem fileItem) {
		result = new FileMonitorResult(fileItem);
	}
	
	/**
	 * This must be overridden and calling super.run() in the very beginning
	 * @see java.util.concurrent.RunnableFuture#run()
	 */
	@Override
	public FileMonitorResult call() {
		currentThread = Thread.currentThread();
		return result;
	}

	public void invalidate(Exception cause) {
		monitorFuture.setFailure(cause);
	}
	
	public void validate() {
		monitorFuture.setSuccess();
	}
	
	public boolean cancel(boolean interrupt) {
		if (currentThread != null && interrupt) {
			currentThread.interrupt();
		}
		return monitorFuture.cancel();
	}

	public FileMonitorResult get() throws InterruptedException, ExecutionException {
		monitorFuture.await();
		return result;
	}

	public FileMonitorResult get(long arg0, TimeUnit arg1) throws InterruptedException,
			ExecutionException, TimeoutException {
		monitorFuture.await(arg0, arg1);
		if (monitorFuture.isDone()) {
			return result;
		}
		return null;
	}

	public boolean isCancelled() {
		return monitorFuture.isCancelled();
	}

	public boolean isDone() {
		return monitorFuture.isDone();
	}
}
