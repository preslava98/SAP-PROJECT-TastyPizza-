package server;


import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import client.Client;

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
			log4j.error("Error opening a new socket or connecting!");
		}
	}
	
	private static final Logger log4j = LogManager.getLogger(Server.class.getName());
}
