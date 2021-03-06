package application;

import java.io.IOException;

import serveur.Serveur;
import serveur.ServiceProg;
import serveur.ServiceAma;

public class BRiLaunch {
	private static final int PORT_AMA = 3000;
	private static final int PORT_PROG = 4000;

	public static void main(String[] args) throws IOException {
		System.out.println("Lancement de l'application de serveur.");
		new Thread(new Serveur(PORT_AMA, ServiceAma.class)).start();
		new Thread(new Serveur(PORT_PROG, ServiceProg.class)).start();
	}
}
