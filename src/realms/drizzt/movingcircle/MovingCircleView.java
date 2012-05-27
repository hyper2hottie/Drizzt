package realms.drizzt.movingcircle;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
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
		
		@Override
		public void run()
		{
			while(state == STATE_RUNNING)
			{
				Canvas c = null;
				try
				{
					c = surfaceHolder.lockCanvas(null);
					synchronized (surfaceHolder) {
						//updateCircle();
						updateCircleLocation();
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
		 * Set the current state of animation
		 */
		private void setState(int state)
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
		 * Update circle location.
		 */
		private void updateCircleLocation()
		{
			//Set location of circle to be middle
			int left = X - CIRCLE_WIDTH/2;
			int top = Y - CIRCLE_WIDTH/2;
			circle.setBounds(left, top, left + CIRCLE_WIDTH, top + CIRCLE_WIDTH);
		}
		
		/**
		 * Change the current location of the circle.
		 */
		public void setCircleLocation(int x, int y)
		{
			synchronized (surfaceHolder) {
				X = x;
				Y = y;
			}
		}
	}
	
	/** The thread that animates the circle */
	private MovingCircleThread thread;	
	
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
		
		//register that we want to head changes to our surface
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		
		//create thread, it is started in surfaceCreated()
		thread = new MovingCircleThread(holder);
		
		setFocusable(true);
	}
	
	/**
     * Standard window-focus override. Notice focus lost so we can pause on
     * focus lost. e.g. user switches to take a call.
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!hasWindowFocus) thread.pause();
    }

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height) {
		thread.setSurfaceSize(width, height);
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
        thread.setState(thread.STATE_RUNNING);
        thread.start();
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        thread.setState(thread.STATE_PAUSED);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }		
	}	
	
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		int x = (int) event.getX();
		int y = (int) event.getY();
		thread.setCircleLocation(x, y);
		
		return super.onTouchEvent(event);
	}
}
