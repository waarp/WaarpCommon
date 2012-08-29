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
package org.waarp.common.service;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.waarp.common.logging.WaarpInternalLogger;
import org.waarp.common.logging.WaarpInternalLoggerFactory;
import org.waarp.common.logging.WaarpSlf4JLoggerFactory;

/**
 * Launch the Engine from a variety of sources, either through a main() or invoked through
 * Apache Daemon.
 *
 * @author Frederic Bregier
 * Inspired from Apache Daemon Wiki
 *
 */
public abstract class ServiceLauncher implements Daemon {
	/**
     * Internal Logger
     */
    protected static WaarpInternalLogger logger;

    protected static EngineAbstract engine = null;

    protected static ServiceLauncher engineLauncherInstance = null;

    protected ExecutorService executor = null;
    
	//{ engineLauncherInstance = new XXXServiceLauncher(); }

    /**
     * 
     * @return a new EngineAbstract
     */
    protected abstract EngineAbstract getNewEngineAbstract();
    
    public ServiceLauncher() {
    	if (logger == null) {
    		logger = WaarpInternalLoggerFactory.getLogger(ServiceLauncher.class);
    	}
    	if (executor == null) {
    		executor = Executors.newSingleThreadExecutor();
    	}
    	engineLauncherInstance = this;
    	if (engine == null) {
    		engine = getNewEngineAbstract();
    	}
    }
    /**
     * The Java entry point.
     * @param args Command line arguments, all ignored.
     */
    public static void main(String[] args) {
		InternalLoggerFactory.setDefaultFactory(new WaarpSlf4JLoggerFactory(null));
        if (engineLauncherInstance == null || engine == null) {
        	System.err.println("Engine not correctly initialized");
        	System.exit(1);
        }
        // the main routine is only here so I can also run the app from the command line
        engineLauncherInstance.initialize();

        Scanner sc = new Scanner(System.in);
        // wait until receive stop command from keyboard
        System.out.printf("Enter 'stop' to halt: ");
        while(!sc.nextLine().toLowerCase().equals("stop"));

        if (!engine.isShutdown()) {
            engineLauncherInstance.terminate();
        }
        sc.close();
    }

    /**
     * Windows mode<br>
     * <br>
     * Static methods called by prunsrv to start/stop
     * the Windows service.  Pass the argument "start"
     * to start the service, and pass "stop" to
     * stop the service.
     *
     * <pre> 
     * prunsrv.exe //IS/MyService --Classpath=C:\...\xxx.jar --Description="My Java Service" --Jvm=auto --StartMode=jvm --StartClass=org.waarp.xxx.service.ServiceLauncher --StartMethod=windowsService --StartParams=start --StopMode=jvm --StopClass=org.waarp.xxx.service.ServiceLauncher --StopMethod=windowsService --StopParams=stop 
     * </pre> 
     * @param args Arguments from prunsrv command line
     **/
    public static void windowsService(String args[]) {
		InternalLoggerFactory.setDefaultFactory(new WaarpSlf4JLoggerFactory(null));
        String cmd = "start";
        if (args.length > 0) {
            cmd = args[0];
        }
        if (engineLauncherInstance == null || engine == null) {
        	System.err.println("Engine not correctly initialized");
        	System.exit(1);
        }
        if ("start".equals(cmd)) {
            engineLauncherInstance.windowsStart();
        } else {
            engineLauncherInstance.windowsStop();
        }
    }

    /**
     * Windows mode<br>
     * <br>
     * <pre>
     * prunsrv.exe //IS/MyService --Classpath=C:\...\xxx.jar --Description="My Java Service" --Jvm=auto --StartMode=jvm --SartClass=org.waarp.xxx.service.ServiceLauncher --StartMethod=start --StopMode=jvm --StopClass=org.waarp.xxx.service.ServiceLauncher --StopMethod=stop
     * </pre>
     */
    public void windowsStart() {
        logger.debug("windowsStart called");
        initialize();
        // Should we wait ?
        try {
			engine.getShutdownFuture().await();
		} catch (InterruptedException e) {
		}
    }

    /**
     * Windows mode<br>
     * <br>
     * <pre>
     * prunsrv.exe //IS/MyService --Classpath=C:\...\xxx.jar --Description="My Java Service" --Jvm=auto --SartClass=org.waarp.xxx.service.EngineLauncher --StartMethod=start --StartMode=jvm --StopClass=org.waarp.xxx.service.EngineLauncher --StopMethod=stop --StopMode=jvm
     * </pre>
     */
    public void windowsStop() {
        logger.debug("windowsStop called");
        terminate();
        // should we force Future to be cancelled there?
    }

    // Implementing the Daemon interface is not required for Windows but is for Linux
    @Override
    public void init(DaemonContext arg0) throws Exception {
    	logger.debug("Daemon init");
    }

    @Override
    public void start() {
    	logger.debug("Daemon start");
        initialize();
    }

    @Override
    public void stop() {
    	logger.debug("Daemon stop");
        terminate();
    }

    @Override
    public void destroy() {
    	logger.debug("Daemon destroy");
    	if (engine != null && !engine.isShutdown()) {
    		terminate();
    	}
    	if (executor != null) {
    		executor.shutdown();
    		executor = null;
    	}
    }

    /**
     * Do the work of starting the engine
     */
    private void initialize() {
        if (engine != null) {
        	logger.info("Starting the Engine");
        	engine.setDaemon(true);
        	executor.execute(engine);
        } else {
        	logger.error("Engine cannot be started since it is not initialized");
        }
    }

    /**
     * Cleanly stop the engine.
     */
    private void terminate() {
        if (engine != null) {
        	logger.info("Stopping the Engine");
            engine.shutdown();
            engine = null;
        }
    	if (executor != null) {
    		executor.shutdown();
    		executor = null;
    	}
        logger.info("Engine stopped");
    }
}