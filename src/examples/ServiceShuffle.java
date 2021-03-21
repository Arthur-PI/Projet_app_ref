package raphael.service;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

import service.IService;

public class ServiceShuffle implements IService {
	
	private final Socket client;
	
	public ServiceShuffle(Socket socket) {
		client = socket;
	}

	@Override
	public void run() {
		try {
			BufferedReader in = new BufferedReader (new InputStreamReader(client.getInputStream ( )));
			PrintWriter out = new PrintWriter (client.getOutputStream ( ), true);

			out.println("Tapez un texte à shuffle");
		   
			String line = in.readLine();
			
			List<Character> characters = new ArrayList<Character>();
	        for(char c : line.toCharArray()){
	            characters.add(c);
	        }
	        StringBuilder output = new StringBuilder(line.length());
	        while(characters.size()!=0){
	            int randPicker = (int)(Math.random()*characters.size());
	            output.append(characters.remove(randPicker));
	        }
			
			out.println(output.toString());
			
			client.close();
		}
		catch (IOException e) {
			//Fin du service
		}
	}
	
	protected void finalize() throws Throwable {
		 client.close(); 
	}
	
	public String toString() {
		return "Shuffle de texte";
	}

	public static String toStringue() {
		return "Shuffle de texte";
	}
}
