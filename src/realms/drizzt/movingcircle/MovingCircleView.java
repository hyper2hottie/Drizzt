package realms.drizzt.movingcircle;


import java.util.LinkedList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MovingCircleView extends SurfaceView implements SurfaceHolder.Callback {
	public class MovingCircleThread extends Thread
	{
		/*
		 * CONSTANTS
		 */
		/** Width of the circle in pixels */
		public static final int CIRCLE_WIDTH = 50;
		
		/** Color of the circle */
		public static final int CIRCLE_COLOR = 0xffff0000;
		
		/** Animation states */
		public static final int STATE_RUNNING = 1;
		public static final int STATE_PAUSED = 2;
		public static final int STATE_WAITING = 3;
		public static final int STATE_STOPPING = 4;
		
		/*
		 * State fields
		 */
		/** Height of the canvas */
		private int canvasHeight = 1;
		
		/** Width of the canvas */
		private int canvasWidth = 1;
		
		/** Center of canvas */
		private int cX;
		private int cY;
		
		/** The circle to draw */
		private ShapeDrawable circle;
		
		/** Location of circle */
		private int X;
		private int Y;
		
		/** Circle direction and velocity */
		private int vX;
		private int vY;
				
		/** Current animation state */
		private int state;
		
		/** Is thread runing */
		private boolean isRunning = false;
		
		/** Hanlder to the surface holder */
		private SurfaceHolder surfaceHolder;
				
		/** 
		 * Constructs the thread
		 * 
		 * @param surfaceHolder - The surface holder we interact with for drawing on.
		 */
		public MovingCircleThread(SurfaceHolder surfaceHolder)
		{
			//Set the handle to the surface holder
			this.surfaceHolder = surfaceHolder;
			
			//Create the circle
			circle = new ShapeDrawable(new OvalShape());			
			circle.getPaint().setColor(CIRCLE_COLOR);
			
			vX = 1;
			vY = 1;
		}
		
		/**
		 * Starts the animations
		 */
		public void doStart()
		{
			setState(STATE_RUNNING);
		}
		
		/**
		 * Pause animating
		 */
		public void pause()
		{
			synchronized (surfaceHolder) {
				if(state == STATE_RUNNING) setState(STATE_PAUSED);
			}			
		}
		
		/**
		 * Get the thread to wait.
		 * @throws InterruptedException 
		 */
		public void waitThread()
		{
			synchronized (this) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					
				}
			}
		}
		
		@Override
		public void run()
		{
			while(isRunning)
			{		
				//For stopping the thread during a pause
				if(state == STATE_WAITING)
					waitThread();
				//This is used for destroying the thread
				if(state == STATE_STOPPING)
					return;
				
				//Update and draw the circle
				Canvas c = null;
				try
				{
					c = surfaceHolder.lockCanvas(null);
					synchronized (surfaceHolder) {
						//updateCircle();
						if (state == STATE_RUNNING) updateCircleLocation();
						doDraw(c);
					}
				}
				finally
				{
					if(c != null)
						surfaceHolder.unlockCanvasAndPost(c);
				}
			}
		}
		
		/** Used to change surface dimensions. */
		public void setSurfaceSize(int width, int height)
		{
			//synched to make sure changes atomically
			synchronized (surfaceHolder) {
				canvasHeight = height;
				canvasWidth = width;
				
				cX = X = canvasWidth/2;
				cY = Y = canvasHeight/2;
				
				//Set location of circle to be middle
				int left = canvasWidth/2 - CIRCLE_WIDTH/2;
				int top = canvasHeight/2 + CIRCLE_WIDTH/2;
				circle.setBounds(left, top, left + CIRCLE_WIDTH, top + CIRCLE_WIDTH);

			}
		}
		
		/**
		 * Resume animating.
		 */
		public void unpause()
		{
			setState(STATE_RUNNING);
		}
		
		/**
		 * Used to signal the thread to run or not.
		 * True lets it run, false will shut down the thread.
		 * 
		 * @param run - true to run, false to shut down
		 */
		public void setRunning(boolean run)
		{
			isRunning = run;
		}
		
		/**
		 * Set the current state of animation
		 */
		public void setState(int state)
		{
			this.state = state;
		}
		
		/**
		 * Draws the background and circle.
		 */
		private void doDraw(Canvas canvas)
		{
			//Black out background
			canvas.drawColor(0xff000000);
			
			//Draw the circle
			circle.draw(canvas);
		}
		
		/**
		 * Updates the location of the circle.
		 */
		private void updateCircle()
		{
			circle.getPaint().setColor(CIRCLE_COLOR);
			
			//Move the circle
			if(X > (cX + 100))
				vX = -1;
			else if(X < (cX - 100))
				vX = 1;
			
			if(Y > (cY + 100))
				vY = -1;
			else if(Y < (cY - 100))
				vY = 1;
			
			X += vX*2;
			Y += vY*2;
			//Set location of circle to be middle
			int left = X - CIRCLE_WIDTH/2;
			int top = Y - CIRCLE_WIDTH/2;
			circle.setBounds(left, top, left + CIRCLE_WIDTH, top + CIRCLE_WIDTH);
		}
		
		/**
		 * Update circle location.  Sets the circle objects location based
		 * on the location stored in the thread.
		 */
		private void updateCircleLocation()
		{
			//Set location of circle to be middle
			int left = X - CIRCLE_WIDTH/2;
			int top = Y - CIRCLE_WIDTH/2;
			if(circle.getBounds().left != left || circle.getBounds().top != top)
			{
				Pair<Integer, Integer> offset =  getCircleOffset();
				callListeners(offset.first, offset.second);
			}
			circle.setBounds(left, top, left + CIRCLE_WIDTH, top + CIRCLE_WIDTH);
		}
		
		/**
		 * Change the current location of the circle.  These changes only
		 * affect the draw location if the thread is running and updateCircleLocation()
		 * is being called.
		 */
		public void setCircleLocation(int x, int y)
		{
			synchronized (surfaceHolder) {
				X = x;
				Y = y;
			}
		}
		
		/**
		 * Get circle offset.  Used to get the offset from center of the screen
		 * Note: Towards the right of the screen is a positive x offset and towards 
		 * the top is a positive y offset.
		 * @return offset of drawn circle in format [x,y] from center of screen
		 */
		public Pair<Integer, Integer> getCircleOffset()
		{
			int tempX = X;
			int tempY = Y;
			int tempcX = cX;
			int tempcY = cY;
			
			return new Pair<Integer, Integer>(tempX-tempcX, tempcY - tempY);
		}
	}
	
	/** The thread that animates the circle */
	private MovingCircleThread thread;	
	
	Context context;
	

	/** Listeners */
	private LinkedList<MovingCircleListener> listeners;
	
	/** 
	 * Return the circle thread 
	 * 
	 * @return the animation thread
	 */
	public MovingCircleThread getThread()
	{
		return thread;
	}
	
	public MovingCircleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		//register that we want to head changes to our surface
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		
		//create thread, it is started in surfaceCreated()
		thread = new MovingCircleThread(holder);
		
		//create the listeners list
		listeners = new LinkedList<MovingCircleListener>();
		
		setFocusable(true);
	}
	
	/**
     * Standard window-focus override. Notice focus lost so we can pause on
     * focus lost. e.g. user switches to take a call.
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!hasWindowFocus) thread.pause();
        else thread.unpause();
    }

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height) {
		thread.setSurfaceSize(width, height);
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
        thread.setRunning(true);
        if(!thread.isAlive())
        	thread.start();
        else
        {
        	synchronized (thread) {
        		thread.notifyAll();
			}
        }
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// we have to tell thread to wait until it is resumed
        thread.setState(thread.STATE_WAITING);
		
	}	
	
	/**
	 * This function is used for adding a listener.  All listeners will
	 * be called whenever the circles location changes
	 * @param listener - the object that wants calls from this circle
	 */
	public void registerListener(MovingCircleListener listener)
	{
		if(!listeners.contains(listener))
			listeners.add(listener);
	}
	
	/**
	 * This function removes a listener.
	 * @param listener - the object that no longer wants to recieve updates
	 */
	public void unregisterListener(MovingCircleListener listener)
	{
		if(listeners.contains(listener))
			listeners.remove(listener);
	}
	
	/**
	 * Call all of the listeners with the new circle location.
	 */
	private void callListeners(int X, int Y)
	{
		for(MovingCircleListener listener : listeners)
		{
			listener.onCircleMoved(X, Y);
		}
	}
	
	/**
	 * Whenever someone makes a touch to this view the onTouchEvent
	 * is called.  It updates the location of the circle.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		int x = (int) event.getX();
		int y = (int) event.getY();
		thread.setCircleLocation(x, y);
		return true;
	}
}
