package serveur;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.*;

public class ServiceAma implements Runnable {

	private Socket client;

	public ServiceAma(Socket socket) {
		client = socket;
	}

	public void run() {
		try {
			BufferedReader sin = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter sout = new PrintWriter(client.getOutputStream(), true);

			sout.println(ServiceRegistry.toStringue() + "Tapez le numero de service desire :");
			int choix = Integer.parseInt(sin.readLine());
			Class<? extends IService> classe = ServiceRegistry.getServiceClass(choix);

			try {
				Constructor<? extends IService> constructor = classe.getConstructor(java.net.Socket.class);
				IService service = constructor.newInstance(this.client);
				service.run();

			} catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException e) {
				System.out.println(e);
			}
		} catch (IOException e) {
			// Fin du service
		}

		try {
			client.close();
		} catch (IOException e2) {
		}
	}

	protected void finalize() throws Throwable {
		client.close();
	}
}
