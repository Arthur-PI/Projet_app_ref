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

	@Override
	public void run() {
		try {
			this.sin = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
			this.sout = new PrintWriter(this.client.getOutputStream(), true);

			boolean continu = false;
			do {
				switch (menuArrive()) {
				case "1":
					continu = connexion();
					break;
				case "2":
					continu = inscription();
					break;
				default: // == exit
					sout.println("finService");
					return;
				}
			} while (!continu);

			// ici l'utilisateur est connecté

			// TODO Une fois connecter: Menu pour ajouter une service ou pour changer le
			// serveur FTP
			while (true) {
				switch (menuService()) {
				case "charger":
					chargerService();
					break;
				case "ftp":
					modifFtpServeur();
					break;
				default: // == exit
					sout.println("finService");
					return;
				}
			}
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
		String message = "Bienvenue sur le service de chargement de service." + "##Connexion(1) ou Inscription(2):";

		try {
			do {
				sout.println(message);
				line = sin.readLine().trim();
				message = "Veuillez choisir 1 ou 2 :";
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
			message = "Pour vous inscrire##Entrez votre identifiant : ";
			do {
				sout.println(message);
				login = sin.readLine();
				message = "Ce login est deja prit.##Merci d'en essayer un autre : ";
				// TODO gérer la concurrence sur programmeurs
			} while (!(login.equals("exit") || !programmeurs.containsKey(login)));

			synchronized (programmeurs) {
				programmeurs.put(login, null);
			}

			if (login.equals("exit"))
				return false;

			sout.println("Entrez votre mot de passe : ");
			password = sin.readLine();
			if (password.equals("exit") || password.trim().isEmpty())
				return false;

			message = "Entrez l'URL de votre serveur FTP : ";
			do {
				sout.println(message);
				ftp = sin.readLine();
				message = "Merci de rentrer une URL valide";
			} while (!(ftp.startsWith("ftp://") || ftp.equals("exit")));

			this.programmeur = new Programmeur(login, password, ftp);
			synchronized (programmeurs) {
				programmeurs.put(login, this.programmeur);
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
			message = "Pour vous connecter, merci d'entrer votre identifiant : ";
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
				message = "Identifiant et/ou mot de passe incorrect(s)##Merci de réessayer ; entrez votre identifiant : ";
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return false;
		}
	}

	public String menuService() {
		String line = "";
		String message = "Connecté en tant que " + this.programmeur
				+ "##Entrez [Charger](ou mettre à jour) un service ou [ftp] pour changer l'addresse de votre serveur ftp";

		try {
			do {
				sout.println(message);
				line = sin.readLine().trim().toLowerCase();
				message = "Veuillez choisir [charger] ou [ftp] :";
			} while (!(line.equals("charger") || line.equals("ftp") || line.equals("exit")));

			return line;

		} catch (IOException e) {
			System.err.println(e.getMessage());
			return "";
		}
	}

	public void modifFtpServeur() {
		// TODO modif le serveur ftp d'un programmeur
	}

	public void chargerService() {
		// TODO charge un service
	}
}
