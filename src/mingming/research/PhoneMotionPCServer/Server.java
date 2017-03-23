package mingming.research.PhoneMotionPCServer;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class Server {

	ServerSocket ssocket = null;
	Socket connectionsocket = null;
	PrintWriter out = null;
	BufferedReader in = null;
	InputStream inputstream;
	int port = 9011;//80
	
	byte[] buff;
	Robot robot;
	public Server()
	{
		buff = new byte[200];
		try {
			robot = new Robot();
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Server(int _port)
	{
		port = _port;
	}
	
	public void socketConnect()
	{
		try{
			System.out.println("before initializing socket....");
			ssocket = new ServerSocket(port);
			System.out.println("after initializing socket....");
			connectionsocket = ssocket.accept();
			System.out.println("client connected...");
			out = new PrintWriter(connectionsocket.getOutputStream());
			inputstream = connectionsocket.getInputStream();
			in = new BufferedReader(new InputStreamReader(inputstream));
		}catch(IOException e){
			e.printStackTrace();
		}
	}
		
	public void socketClose()
	{
		if(out != null)
			out.close();
		
		if(in != null)
			try{
				in.close();
			}catch(IOException e)
			{
				e.printStackTrace();
			}
		
		if(inputstream != null)
			try {
				inputstream.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
		try{
			connectionsocket.close();
			ssocket.close();
		}catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
    public Timer timer;

    public void scheduleTimeTask()
    {
        timer = new Timer();  //At this line a new Thread will be created
        timer.scheduleAtFixedRate(new myTimerTask(), 1000, 30); 
    }

    float deltaX = 0, deltaY  = 0;
    float prev_deltaX = 0, prev_deltaY = 0;
    final float ratio = 8;
    int click = 0;
    class myTimerTask extends TimerTask {

        @Override
        public void run() {
        	//System.out.println("in separate thread...");
          if(in != null)
          {
        	  
        	  try {
	        		 // System.out.println("before reading data from client..."); 
	        		  String line = in.readLine();
	        		  if(line != null)
	        		  {
		        		 // System.out.println("receiving: " + line);
		        		  
			    		  String[] words = line.split(",");
			    		  if(words.length == 3)
			    		  {
							 deltaX = ratio * Float.parseFloat(words[1]);
							 deltaY = (-1) * ratio * Float.parseFloat(words[0]);
							 click = Integer.parseInt(words[2]);
							 System.out.println("click: " + click);
							 Interaction();
							 /*
							 if(deltaX != prev_deltaX || deltaY != prev_deltaY)
							 {
								 Interaction();
								// System.out.println("incoming message: " + deltaX + "," + deltaY);
								 prev_deltaX = deltaX;
								 prev_deltaY = deltaY;
							 }
							 */						 
			    		  }
	        		  }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}      	  
          }
        }
    }
	
    
    
	public void Interaction()
	{
		initLoc.x += deltaX;
		initLoc.y += deltaY;
		if(click == 2)
		{
			robot.mousePress(InputEvent.BUTTON1_MASK);
			robot.mouseRelease(InputEvent.BUTTON1_MASK);
		}
		robot.mouseMove(initLoc.x, initLoc.y);
	}
	
	public Point initLoc;
	
	public static void main(String[] args){
		Server  myserver = new Server();
		
		myserver.socketConnect();
		
		myserver.scheduleTimeTask();
				
		myserver.initLoc = MouseInfo.getPointerInfo().getLocation();
        // Keep this process running until Enter is pressed
        System.out.println("Press Enter Key to quit...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        myserver.timer.cancel();
        myserver.socketClose();
	}
}
