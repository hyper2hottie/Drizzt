package realms.drizzt.drawcircle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CircleView extends SurfaceView implements SurfaceHolder.Callback 
{
	class CircleThread extends Thread
	{
		/*
		 * Constants
		 */
		/** Pixel width of circle */
		private static final int circleWidth = 50;
		
		/** Animation states */
		private static final int MODE_RUNNING = 0;
		private static final int MODE_PAUSED = 1;
				
		/* 
		 * Member (state) fields
		 */
				
		/** Current height of the canvas. */
		private int canvasHeight = 1;
		
		/** Current width of the canvas. */
		private int canvasWidth= 1;
		
		/** What to draw. */
		private ShapeDrawable circle;
		
		/** Current animation state */
		private int mode;
		
		/** Handle to the surface manager object to interact with */
		private SurfaceHolder surfaceHolder;
		
		public CircleThread(SurfaceHolder surfaceHolder)
		{
			//important handles
			this.surfaceHolder = surfaceHolder;
			
			//Get drawables
			circle = new ShapeDrawable(new OvalShape());
			circle.getPaint().setColor(0xffff0000);
			
		}
		
		/**
		 * Starts the animations
		 */
		public void doStart()
		{
			setState(MODE_RUNNING);
		}
		
		/**
		 * Pause animating.
		 */
		public void pause()
		{
			synchronized (surfaceHolder) {
				if(mode == MODE_RUNNING) setState(MODE_PAUSED);
			}
		}
		
		@Override
		public void run()
		{
			while(mode == MODE_RUNNING)
			{
				Canvas c = null;
				try
				{
					c = surfaceHolder.lockCanvas(null);
					synchronized (surfaceHolder) {
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
				
				//Set location of circle to be middle
				int left = canvasWidth/2 - circleWidth/2;
				int top = canvasHeight/2 + circleWidth/2;
				circle.setBounds(left, top, left + circleWidth, top + circleWidth);
				//circle.setBounds(400, 200, 450, 250);
				circle.setIntrinsicHeight(circleWidth);
				circle.setIntrinsicWidth(circleWidth);
			}
		}
		
		/**
		 * Resume animating.
		 */
		public void unpause()
		{
			setState(MODE_RUNNING);
		}
		
		/**
		 * Sets the state of animations.
		 */
		private void setState(int state)
		{
			mode = state;
		}
		
		/**
		 * Draws the background and circle.
		 */
		private void doDraw(Canvas canvas)
		{
			//Black out background
			canvas.drawColor(0xff000000);
			
			//circle.setBounds(200, 200, 250, 250);
			//Draw the circle
			circle.draw(canvas);
			
		}
	}
	
	/** The thread that animates the circle */
	private CircleThread thread;	
	
	/** 
	 * Return the circle thread 
	 * 
	 * @return the animation thread
	 */
	public CircleThread getThread()
	{
		return thread;
	}	
	
	/**
	 * Public constructor
	 */
	public CircleView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		//register that we want to head changes to our surface
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		
		//create thread, it is started in surfaceCreated()
		thread = new CircleThread(holder);
		
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
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) 
	{
		thread.setSurfaceSize(width, height);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) 
	{
		// start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
        thread.setState(thread.MODE_RUNNING);
        thread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) 
	{
		// we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        thread.setState(thread.MODE_PAUSED);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }

	}

}
