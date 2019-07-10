package server;

public class Accounts {
	
	private String username;
	private String password;
	private String typeOfAcc;
	private static final String  specialPass = "iamadmin";
	
	public Accounts(String username, String password, String typeOfAcc) {
		this.username = username;
		this.password = password;
		this.typeOfAcc = typeOfAcc;
	}
	
	public static String getSpecialPass()
	{
		return specialPass;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getTypeOfAcc() {
		return typeOfAcc;
	}
	public void setTypeOfAcc(String typeOfAcc) {
		this.typeOfAcc = typeOfAcc;
	}
	
	public String toString()
	{
		return "\n{\"Username\"" + ":" + "\"" + username + "\"," + "\n" +
				"\"Password\"" + ":" + "\"" + password + "\"," + "\n" +
				"\"Type of Account\"" + ":" + "\"" + typeOfAcc + "\"}\n";
	}
}
