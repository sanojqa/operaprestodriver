/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */
package com.opera.core.systems.runner.launcher;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import org.openqa.selenium.ProcessUtils;
import org.openqa.selenium.WebDriverException;

public class OperaLauncherBinary extends Thread {

    private Process process;
    private OutputWatcher watcher;
    private Thread outputWatcherThread;
    private List<String> commands = new ArrayList<String>();
    private static Logger logger = Logger.getLogger(OperaLauncherBinary.class.getName());
    private AtomicBoolean running = new AtomicBoolean(false);

    public OperaLauncherBinary(String location, String... args) {
        super(new ThreadGroup("run-process"), "launcher");
        commands.add(location);

        if (args != null && args.length > 0) {
            commands.addAll(Arrays.asList(args));
        }
        init();
    }
    
    public void kill() {
        watcher.kill();
    }

    public void shutdown()
    {
        if (!running.get())
            return;

        try {
            running.set(false);
            interrupt();
            join(1000);
        } catch (InterruptedException ex) {
            logger.severe("Unable to shutdown Opera binary in a timely fashion.");
            kill();
        }
    }

    public void init() {
        ProcessBuilder builder = new ProcessBuilder(commands);
        
        try {

            process = builder.start();
            builder.redirectErrorStream(true);
            
            watcher = new OutputWatcher(process);

            outputWatcherThread = new Thread(getThreadGroup(), watcher , "output-watcher");
            outputWatcherThread.start();

            running.set(true);
        } catch (IOException e) {
            throw new WebDriverException("Could not start the process : " + e.getMessage());
        }
    }


    @Override
    public void run()
    {
        logger.fine("Waiting for Launcher binary to exit.");
        int exit = 0;
        while (running.get())
        {
            try
            {
                exit = process.waitFor();
                logger.info("Launcher exited with return value " + exit);
                running.set(false);
            } catch (InterruptedException e) {
                logger.fine("Got interrupted. Will terminate Launcher.");
                process.destroy();
            }
        }
    }


    private class OutputWatcher implements Runnable {
        private Process process;

        public OutputWatcher(Process process) {
            this.process = process;
        }

        public void run() {
            InputStream stream = process.getInputStream();
            while (running.get()) {
                try {
                    if(stream.read() == -1) {
                    	return;
                    }
                } catch (IOException e) {
                    /* ignored */
                }
            }
        }
        
		public void kill() {
			running.set(false);
			try {
				ProcessUtils.killProcess(process);
			} catch (Exception e) { //ProcessStillAliveException, RuntimeException
				//we cant do much here
				logger.warning("Could not kill the process : " + e.getMessage());
			}
		}
    }
}