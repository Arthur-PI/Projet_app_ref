package serveur;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServiceProg implements Runnable{
	
	private Socket client;
	
	public ServiceProg(Socket s) {
		this.client = s;
	}

	@SuppressWarnings("unused")
	@Override
	public void run() {
		try {
			BufferedReader in = new BufferedReader (new InputStreamReader(this.client.getInputStream ( )));
			PrintWriter out = new PrintWriter (this.client.getOutputStream ( ), true);
			// TODO
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {this.client.close();} catch (IOException e2) {}
	}
	protected void finalize() throws Throwable {
		 client.close(); 
	}
}
