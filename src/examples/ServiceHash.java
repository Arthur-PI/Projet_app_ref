package raphael.service;

import java.io.*;
import java.net.*;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import service.IService;

public class ServiceHash implements IService {
	
	private final Socket client;
	
	public ServiceHash(Socket socket) {
		client = socket;
	}

	@Override
	public void run() {
		try {
			BufferedReader in = new BufferedReader (new InputStreamReader(client.getInputStream ( )));
			PrintWriter out = new PrintWriter (client.getOutputStream ( ), true);

			out.println("Tapez un texte à Hasher en SHA-256");
		   
			String line = in.readLine();
			
			out.println(getCryptoHash(line, "SHA-256"));
			
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
		return "Hash de texte";
	}

	public static String toStringue() {
		return "Hash de texte";
	}
	
	private String getCryptoHash(String input, String algorithm) {
        try {
            MessageDigest msgDigest = MessageDigest.getInstance(algorithm);

            byte[] inputDigest = msgDigest.digest(input.getBytes());

            BigInteger inputDigestBigInt = new BigInteger(1, inputDigest);

            String hashtext = inputDigestBigInt.toString(16);

            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
