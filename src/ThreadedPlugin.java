/**
 * ThreadedPlugin.java - Threaded Plug-in template for hey0's minecraft mod.
 * @author Shaun (sturmeh)
 */
public abstract class ThreadedPlugin extends SuperPlugin implements Runnable {
	private Thread clock;
	private long interval = 0;

    @Deprecated
	public ThreadedPlugin(String name) {
		super(name);
	}

	public ThreadedPlugin(String name, float version) {
		super(name, version);
	}
	
	public ThreadedPlugin(String name, float version, String prop) {
        super(name, version, prop);
    }

	public synchronized void doWork() {}
	
	@Override
	public void enable() {
		if (clock == null)
			clock = new Thread(this);
		reloadConfig();
		clock.start();
		super.enable();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void disable() {
		clock.stop();
		clock = null;
		super.disable();
	}
	
	public void setInterval(long interval) {
		this.interval = interval;
	}
	
	public void run() {
		while (isEnabled()) {
			try {
				while (interval <= 0)
					Thread.sleep(50);	// thread is dormant.
				for (long i=0; i < interval && isEnabled(); i++)
					Thread.sleep(50); // sleep for an in-game second.
				if (isEnabled()) doWork();
			} catch (InterruptedException e) { }
		}
	}
}
