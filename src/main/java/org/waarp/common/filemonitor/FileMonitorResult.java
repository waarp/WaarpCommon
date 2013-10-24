package org.waarp.common.filemonitor;

import org.waarp.common.filemonitor.FileMonitor.FileItem;

public class FileMonitorResult {
	public boolean status = false;
	public FileItem fileItem;
	/**
	 * @param fileItem
	 */
	public FileMonitorResult(FileItem fileItem) {
		this.fileItem = fileItem;
	}
	
}