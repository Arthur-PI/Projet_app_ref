package clientama;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientAma {
	private final static int PORT_SERVICE = 3000;
	private final static String HOST = "localhost"; 
	
	public static void main(String[] args) {
		Socket s = null;		
		try {
			s = new Socket(HOST, PORT_SERVICE);

			BufferedReader sin = new BufferedReader (new InputStreamReader(s.getInputStream ( )));
			PrintWriter sout = new PrintWriter (s.getOutputStream ( ), true);
			Scanner clavier = new Scanner(System.in);			
		
			System.out.println("Connecté au serveur " + s.getInetAddress() + ":" + s.getPort());
			
			String line;
			while (true) {
				line = sin.readLine().replaceAll("##", "\n");
				if (line.equals("finService"))
					break;

				System.out.println(line);
				System.out.print("> ");
				String reponse = clavier.nextLine();
				if (reponse == null) reponse = "";

				sout.println(reponse);
			}
			System.out.println("Au revoir et merci");
				
			
		}
		catch (IOException e) { System.err.println("Fin de la connexion"); }
		try { if (s != null) s.close(); } 
		catch (IOException e2) { ; }		
	}
}
