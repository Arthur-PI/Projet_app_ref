package clientprog;

public class Programmeur {
	private String login;
	private String motDePasse;
	private String ftpUrl;

	public Programmeur(String log, String mdp, String ftpU) {
		this.login = log;
		this.motDePasse = mdp;
		this.ftpUrl = ftpU;
	}

	public String getFtpUrl() {
		return ftpUrl;
	}

	public void setFtpUrl(String newftpUrl) {
		this.ftpUrl = newftpUrl;
	}

	public String getLogin() {
		return this.login;
	}

	public boolean verifCredentials(String password) {
		return this.motDePasse.equals(password);
	}

	@Override
	public String toString() {
		return login;
	}

}
