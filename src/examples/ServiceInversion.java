package raphael.service;

import java.io.*;
import java.net.*;

import service.IService;

public class ServiceInversion implements IService {
	
	private final Socket client;
	
	public ServiceInversion(Socket socket) {
		client = socket;
	}

	@Override
	public void run() {
		try {BufferedReader in = new BufferedReader (new InputStreamReader(client.getInputStream ( )));
			PrintWriter out = new PrintWriter (client.getOutputStream ( ), true);

			out.println("Tapez un texte à inverser pas modif");
		   
			String line = in.readLine();		
	
			String invLine = new String (new StringBuffer(line).reverse());
			
			out.println(invLine);
			
			client.close();
		}
		catch (IOException e) {
			//Fin du service d'inversion
		}
	}
	
	protected void finalize() throws Throwable {
		 client.close(); 
	}
	
	public String toString() {
		return "Inversion de texte";
	}

	public static String toStringue() {
		return "Inversion de texte";
	}
}
