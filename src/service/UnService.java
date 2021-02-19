package service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class UnService {
	private Class<? extends IService> service;
	private boolean enable;
	private String user;
	
	public boolean isEnable() {
		return enable;
	}

	public Class<? extends IService> getService() {
		return service;
	}
	
	public void setService(Class<? extends IService> newService) {
		this.service = newService;
	}

	public String getUser() {
		return user;
	}
	
	public void toogleEnable() {
		this.enable = !this.enable;
	}

	
	public UnService(Class<? extends IService> service, String user) {
		this.service = service;
		this.user = user;
		this.enable = true;
	}
	
	public String toString() {
		try {
			Method toStringue = service.getMethod("toStringue");
			String string = (String) toStringue.invoke(service);
			return string + ": " + ( enable ? "active" : "desactive");
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			return "Error affichage"; // ??? - normalement deja teste par validation()
		}
	}
	
}
