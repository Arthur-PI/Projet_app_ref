package serveur;


import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.*;


public class Serveur implements Runnable {
	
	private ServerSocket listen_socket;
	private Class<? extends Runnable> service;
	
	public Serveur(int port, Class<? extends Runnable> serviceClass) throws IOException {
		listen_socket = new ServerSocket(port);
		this.service = serviceClass;
	}

	public void run() {
		try {
			Constructor<? extends Runnable> c = this.service.getConstructor(Socket.class);
			while(true)
				new Thread((Runnable) c.newInstance(this.listen_socket.accept())).start();
				
		}
		catch (IOException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			try {this.listen_socket.close();} catch (IOException e1) {}
			System.err.println("Pb sur le port d'écoute :" + e);
		}
	}

	protected void finalize() throws Throwable {
		try {this.listen_socket.close();} catch (IOException e1) {}
	}
}
