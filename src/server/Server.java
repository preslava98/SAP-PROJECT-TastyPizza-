package server;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	
	public static void main(String[] args)
	{		
		try
		{
			ServerSocket ss = new ServerSocket(6969);	
			while(true)
			{
				Socket socket = ss.accept();
				ServerThread th = new ServerThread(socket);
				th.start();
			}

		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
