package serveur;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class ServiceProg implements Runnable {

	private Socket client;
	private Programmeur programmeur;
	private BufferedReader sin;
	private PrintWriter sout;

	private static Map<String, Programmeur> programmeurs;

	static {
		programmeurs = Collections.synchronizedMap(new HashMap<>());
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
				default: // == "exit"
					sout.println("finService");
					return;
				}
			} while (!continu);

			// ici l'utilisateur est connecte

			String message = "Connecte en tant que " + this.programmeur + "##";
			String action = "";
			while (true) {
				action = menuService(message);
				message = "";
				switch (action) {
				case "1":
					message = chargerService();
					break;
				case "2":
					message = modifFtpServeur();
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
		String message = "Bienvenue sur le service de chargement de service. Vous pourrez taper [exit] a tout moment pout quitter";
		message += "####Quel methode d'authentification:";
		message += "##1 == Connexion ==";
		message += "##2 == Inscription ==";

		try {
			do {
				sout.println(message);
				line = sin.readLine().trim();
				message = "Veuillez choisir [1] ou [2] :";
			} while (!(line.equals("1") || line.equals("2") || line.equals("exit")));

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
			message = "Entrez votre identifiant : ";
			do {
				sout.println(message);
				login = sin.readLine();
				message = "Ce login est deja pris, merci d'en essayer un autre : ";
			} while (!(login.equals("exit") || !programmeurs.containsKey(login)));

			programmeurs.put(login, null);

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
			} while (!(ftp.startsWith("ftp://") || ftp.startsWith("ftps://") || ftp.equals("exit")));

			this.programmeur = new Programmeur(login, password, ftp);
			programmeurs.put(login, this.programmeur);
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
				message = "Identifiant et/ou mot de passe incorrect(s)##Merci de reessayer, entrez votre identifiant : ";
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return false;
		}
	}

	public String menuService(String m) {
		String line = "";
		// TODO afficher les services ajouter par le Client
		String message = m + "Que voulez-vous faire : ";
		message += "##1 == Ajouter/Recharger un service de votre serveur FTP ==";
		message += "##2 == Modifier l'URL de votre serveur FTP ==";

		try {
			do {
				sout.println(message);
				line = sin.readLine().trim().toLowerCase();
				message = "Veuillez choisir [1] ou [2] :";
			} while (!(line.equals("1") || line.equals("2") || line.equals("exit")));

			return line;

		} catch (IOException e) {
			System.err.println(e.getMessage());
			return "";
		}
	}

	public String modifFtpServeur() {
		String line = "";
		String message = "Quel est l'url du nouveau serveur FTP (actuellement " + programmeur.getFtpUrl() + " ) :";
		try {
			do {
				sout.println(message);
				line = sin.readLine();
				message = "Rentrez une URL ftp valide :";
			} while (!(line.startsWith("ftp://") || line.equals("exit")));
		} catch (IOException e) {
			return e.toString() + "##";
		}
		if (line.equals("exit"))
			return "";

		programmeur.setFtpUrl(line);
		return "Serveur ftp modifier##";
	}

	public String chargerService() {
		String line = "";
		String message = "Donnez le nom de la Classe du service a charger :";
		URLClassLoader urlcl = null;

		try {
			sout.println(message);
			line = sin.readLine();
			message = "Donnez une Classe existante";
			if (line.equals("exit"))
				return "";
			// caca

			urlcl = URLClassLoader.newInstance(new URL[] { new URL(programmeur.getFtpUrl()) });
			try {
				System.out.println(urlcl.getURLs()[0].toString() + programmeur.getLogin() + "." + line);
				ServiceRegistry.addService(urlcl.loadClass(programmeur.getLogin() + "." + line));
				return "Service ajoute avec succes####";
			} catch (ClassNotFoundException e) {
				return "La classe est introuvable####Vérifiez qu'elle se trouve dans un package à votre nom";
			} catch (ValidationException e) {
				return e.getMessage() + "####";
			}

		} catch (IOException e) {
			System.err.println(e.getMessage());
			return "";
		}
	}
}
