package serveur;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ServiceProg implements Runnable {

	private Socket client;
	private Programmeur currentProgrammeur;
	private BufferedReader sin;
	private PrintWriter sout;
	private static String EXIT = "exit";
	private static String CHOICES = "12345";

	private static Map<String, Programmeur> programmeurs;

	static {
		programmeurs = Collections.synchronizedMap(new HashMap<>());
		programmeurs.put("arthur", new Programmeur("arthur", "a", "ftp://localhost:21/"));
		programmeurs.put("raphael", new Programmeur("raphael", "r", "ftp://localhost:2121/"));
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
				default:
					sout.println("finService");
					return;
				}
			} while (!continu);

			String message = "Connecte en tant que " + this.currentProgrammeur + "##";
			String action = "";
			while (true) {
				action = menuService(message);
				message = "";
				switch (action) {
				case "1":
					try {
						message = chargerService(false);
					} catch (ChargerServiceException e) {
						message = e.getMessage();
					}
					break;
				case "2":
					message = "Service Indisponible##";
					// try {
					// message = chargerServiceJar();
					// } catch (ChargerServiceException e) { message = e.getMessage(); }
					break;
				case "3":
					message = deleteService();
					break;
				case "4":
					message = toggleService();
					break;
				case "5":
					message = modifFtpServeur();
					break;
				default:
					sout.println("finService");
					return;
				}
				System.out.println(ServiceRegistry.toStringue());
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
		String message = "Bienvenue sur le service de chargement de service. Vous pourrez taper [" + EXIT
				+ "] a tout moment pout quitter";
		message += "####Quel methode d'authentification:";
		message += "##1 == Connexion ==";
		message += "##2 == Inscription ==";

		try {
			do {
				sout.println(message);
				line = sin.readLine().trim();
				message = "Veuillez choisir [1] ou [2] :";
			} while (!(line.equals("1") || line.equals("2") || line.equals(EXIT)));

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

				if (login.equals(EXIT))
					throw new IOException(EXIT);
			} while (programmeurs.containsKey(login));

			programmeurs.put(login, null);

			sout.println("Entrez votre mot de passe : ");
			password = sin.readLine();

			if (password.equals(EXIT) || password.trim().isEmpty())
				throw new IOException(EXIT);

			message = "Entrez l'URL de votre serveur FTP : ";
			do {
				sout.println(message);
				ftp = sin.readLine();
				message = "Merci de rentrer une URL valide";
				if (login.equals(EXIT))
					throw new IOException(EXIT);
			} while (!(ftp.startsWith("ftp://") || ftp.startsWith("ftps://")));

			this.currentProgrammeur = new Programmeur(login, password, ftp);
			programmeurs.remove(login);
			return true;

		} catch (IOException e) {
			System.err.println(e.getMessage());
			programmeurs.put(login, null);
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

				if (login.equals(EXIT))
					return false;

				sout.println("Entrer votre mot de passe");
				password = sin.readLine();
				Programmeur p;
				synchronized (programmeurs) {
					p = programmeurs.get(login);
				}
				if (p != null && p.verifCredentials(password)) {
					this.currentProgrammeur = p;
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
		String message = m + "Que voulez-vous faire : ";
		message += "##1 == Ajouter/Recharger un service de votre serveur FTP ==";
		message += "##2 == Ajouter/Recharger un JAR de votre serveur FTP ==";
		message += "##3 == Supprimer un service deja charge ==";
		message += "##4 == Activer/Desactiver un service deja charge ==";
		message += "##5 == Modifier l'URL de votre serveur FTP ==";

		try {
			do {
				sout.println(message);
				line = sin.readLine().trim().toLowerCase();
				message = "Veuillez choisir [1], [2], [3], [4] ou [5] :";
			} while (!(CHOICES.contains(line) || line.equals(EXIT)));

			return line;

		} catch (IOException e) {
			System.err.println(e.getMessage());
			return "";
		}
	}

	public String modifFtpServeur() {
		String line = "";
		String message = "Quel est l'url du nouveau serveur FTP (actuellement " + currentProgrammeur.getFtpUrl()
				+ " ) :";
		try {
			do {
				sout.println(message);
				line = sin.readLine();
				message = "Rentrez une URL ftp valide :";
			} while (!(line.startsWith("ftp://") || line.equals(EXIT)));
		} catch (IOException e) {
			return e.toString() + "##";
		}
		if (line.equals(EXIT))
			return "";

		currentProgrammeur.setFtpUrl(line);
		return "Serveur ftp modifie##";
	}

	public String chargerService(Boolean jar) throws ChargerServiceException {
		String line = "";
		String message = "Donnez le nom de la Classe du service a charger :";
		URLClassLoader urlcl = null;

		try {
			sout.println(message);
			line = sin.readLine();
			if (line.equals(EXIT))
				throw new ChargerServiceException("");

			String jarFile = jar ? currentProgrammeur.getLogin() + ".jar" : "";
			urlcl = new URLClassLoader(new URL[] { new URL(currentProgrammeur.getFtpUrl() + jarFile) });
			try {
				ServiceRegistry.addService(urlcl.loadClass(currentProgrammeur.getLogin() + "." + line),
						currentProgrammeur.getLogin());
				urlcl.close();
				return "Service ajoute avec succes####";
			} catch (NoClassDefFoundError e) {
				urlcl.close();
				throw new ChargerServiceException(e.getMessage() + "####");
			} catch (ClassNotFoundException e) {
				urlcl.close();
				throw new ChargerServiceException(
						"La classe est introuvable##Verifiez qu'elle se trouve dans un package a votre nom####");
			} catch (ValidationException e) {
				urlcl.close();
				throw new ChargerServiceException(e.getMessage() + "####");
			}

		} catch (IOException e) {
			System.err.println(e.getMessage());
			throw new ChargerServiceException("");
		}
	}

	// Ne fonctionne pas
	public String chargerServiceJar() throws ChargerServiceException {
		this.chargerService(true);

		try {
			String path = this.currentProgrammeur.getFtpUrl() + this.currentProgrammeur.getLogin() + ".jar";
			JarFile jarFile = new JarFile(new File(new URI(path)));
			Enumeration<JarEntry> entries = jarFile.entries();

			URL[] urls = { new URL("jar:" + path + "!/") };
			URLClassLoader cl = new URLClassLoader(urls);

			while (entries.hasMoreElements()) {
				JarEntry je = entries.nextElement();
				System.out.println(je.getName());
				if (je.isDirectory() || !je.getName().endsWith(".class")) {
					continue;
				}

				String className = je.getName().substring(0, je.getName().length() - 6);
				className = className.replace('/', '.');
				cl.loadClass(className);

			}
			cl.close();
			return "Success";
		} catch (IOException | ClassNotFoundException | URISyntaxException e1) {
			e1.printStackTrace();
			throw new ChargerServiceException("");
		}
	}

	public String deleteService() {
		String line = "";
		String message = "Quel Classe de voulez vous supprimer :";
		message += ServiceRegistry.toStringue(currentProgrammeur.getLogin());
		try {
			do {
				sout.println(message);
				line = sin.readLine();
				message = "Choisissez un numero valide !";
			} while (!(line.equals(EXIT) || ServiceRegistry.deleteService(line, currentProgrammeur.getLogin())));
		} catch (IOException e) {
			return e.toString() + "##";
		}
		if (line.equals(EXIT))
			return "";

		return "Service supprime !##";

	}

	public String toggleService() {
		String line = "";
		String message = "Quel Classe de service voulez vous activer/desactiver :";
		message += ServiceRegistry.toStringue(currentProgrammeur.getLogin());
		try {
			do {
				sout.println(message);
				line = sin.readLine();
				message = "Choisissez un numero valide !";
			} while (!(line.equals(EXIT) || ServiceRegistry.toggleService(line, currentProgrammeur.getLogin())));
		} catch (IOException e) {
			return e.toString() + "##";
		}
		if (line.equals(EXIT))
			return "";

		return "Etat du service modifie##";

	}
}
