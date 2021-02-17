package serveur;

import java.io.*;
import java.net.Socket;
import java.util.*;

import clientprog.Programmeur;

public class ServiceProg implements Runnable {

	private Socket client;
	private Programmeur programmeur;
	private BufferedReader sin;
	PrintWriter sout;

	private static Map<String, Programmeur> programmeurs;

	static {
		programmeurs = new HashMap<>();
	}

	public ServiceProg(Socket s) {
		this.client = s;
	}

	@SuppressWarnings("unused")
	@Override
	public void run() {
		try {
			this.sin = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
			this.sout = new PrintWriter(this.client.getOutputStream(), true);

			switch (menuArrive()) {
			case "1":
				connexion();
				break;
			case "2":
				inscription();
				break;
			default:
				return;
			}
			// TODO Inscription: demander le serveur FTP, login, password, verfier si le
			// login existe deja
			// TODO Connexion: login, password
			// TODO Une fois connecter: Menu pour ajouter une service ou pour changer le
			// serveur FTP

		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			this.client.close();
		} catch (IOException e2) {
		}
	}

	protected void finalize() throws Throwable {
		client.close();
	}

	public String menuArrive() {
		String line = "";
		String message = "Bienvenue sur le service de chargement de service."
				+ "##Vous pouvez à tout moment entrer [exit] pour quitter le service"
				+ "##Connexion(1) ou Inscription(2):";
		
		try {
			do {
				sout.print(message);
				line = sin.readLine();
				message = "Veuillez choisir 1 ou 2:";
			} while (!(line.equals("1") || line.equals("2") || line.equals("0")));

			return line;
			
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return "";
		}
	}

	public boolean inscription() {
		String login = "";
		String password = "";
		String ftp = "";
		String message = "";

		try {
			message = "Pour vous inscrire##Votre login:";
			do {
				sout.println(message);
				login = sin.readLine();
				message = "Ce login est deja prit";
				// TODO gérer la concurrence sur programmeurs
			} while (!(login.equals("exit") || !programmeurs.containsKey(login)));

			synchronized (programmeurs) {
				programmeurs.put(login, null);
			}

			if (login.equals("exit"))
				return false;

			sout.println("Entrez votre mot de passe:");
			password = sin.readLine();
			if (password.equals("exit") || password.trim().isEmpty())
				return false;

			message = "Entrez l'URL de votre serveur FTP:";
			do {
				sout.println(message);
				ftp = sin.readLine();
				message = "Merci de rentrer une URL valide";
			} while (!(ftp.startsWith("ftp://") || ftp.equals("exit")));

			synchronized (programmeurs) {
				programmeurs.put(login, new Programmeur(login, password, ftp));
			}
			return true;

		} catch (IOException e) {
			System.err.println(e.getMessage());
			return false;
		}
	}

	public boolean connexion() {
		String login = "";
		String password = "";
		String message = "";

		try {
			message = "Pour vous connecter, merci d'entrer votre identifiant :";
			while (true) {
				sout.println(message);
				login = sin.readLine();

				if (login.equals("exit"))
					return false;

				sout.println("Entrer votre mot de passe");
				password = sin.readLine();
				Programmeur p;
				synchronized (programmeurs) {
					p = programmeurs.get(login);
				}
				if (p != null && p.verifCredentials(password)) {
					this.programmeur = p;
					return true;
				}
				message = "identifiant et/ou mot de passe incorrect(s)";
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return false;
		}
	}

	public void menuService() {
		// TODO
	}

	public void modifFtpServeur() {
		// TODO
	}

	public void ajoutService() {
		// TODO
	}
}
