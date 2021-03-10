package serveur;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.*;

import service.IService;

public class ServiceAma implements Runnable {

	private Socket client;

	public ServiceAma(Socket socket) {
		client = socket;
	}

	public void run() {
		try {
			BufferedReader sin = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter sout = new PrintWriter(client.getOutputStream(), true);

			String message = ServiceRegistry.toStringue() + "Tapez le numero de service desire :";
			Class<? extends IService> classe = null;
			String line = "";
			do {
				sout.println(message);
				line = sin.readLine();
				if (line.equals("exit")) {
					sout.println("finService");
					return;
				}
				int choix = Integer.parseInt(line);
				classe = ServiceRegistry.getServiceClass(choix);
				message = "Choisissez un service valide";
			} while (classe == null);

			try {
				Constructor<? extends IService> constructor = classe.getConstructor(java.net.Socket.class);
				IService service = constructor.newInstance(this.client);
				service.run();
				sout.println("finService");

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
