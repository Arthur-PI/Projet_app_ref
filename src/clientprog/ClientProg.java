package clientprog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientProg {
	private static int PORT_SERVICE = 4000;
	private static String HOST = "localhost";

	public static void main(String[] args) {
		Socket s = null;
		Scanner sc = null;

		try {
			s = new Socket(HOST, PORT_SERVICE);
			BufferedReader sin = new BufferedReader(new InputStreamReader(s.getInputStream()));
			PrintWriter sout = new PrintWriter(s.getOutputStream(), true);
			sc = new Scanner(System.in);
			String line = "";

			while (true) {
				line = sin.readLine().replaceAll("##", "\n");
				if (line.equals("finService"))
					break;

				System.out.println(line);
				System.out.print("> ");

				sout.println(sc.nextLine());
			}
			System.out.println("Au revoir et merci");

		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			s.close();
			sc.close();
		} catch (IOException | NullPointerException e) {
			System.err.println(e.getMessage());
		}
	}

}
