package serveur;

import java.lang.reflect.*;
import java.net.Socket;
import java.util.*;

import service.IService;
import service.UnService;

// TODO test concurrences mais Vector == threadsafe
@SuppressWarnings("unchecked")
public class ServiceRegistry {
	private static List<UnService> servicesClasses;

	static {
		servicesClasses = new Vector<>();
	}

	
	public static void addService(Class<?> runnableClass, String userLogin) throws ValidationException {
		validation(runnableClass);
		Class<? extends IService> tmpService = (Class<? extends IService>) runnableClass;
		int index = containService(tmpService);
		
		if (index != -1) {
			servicesClasses.get(index).setService(tmpService);
			return;
		}
		servicesClasses.add(new UnService(tmpService, userLogin));
	}
	
	public static boolean deleteService(String index, String user) {
		int i;
		try {
			i = Integer.parseInt(index) - 1;
		} catch (NumberFormatException e) {
			return false;
		}
		if (i < servicesClasses.size() && i >= 0 && servicesClasses.get(i).getUser().equals(user)) {
			servicesClasses.remove(i);
			return true;
		}
		return false;
	}
	
	public static boolean toggleService(String index, String user) {
		int i;
		try {
			i = Integer.parseInt(index) - 1;
		} catch (NumberFormatException e) {
			return false;
		}
		if (i < servicesClasses.size() && i >= 0 && servicesClasses.get(i).getUser().equals(user)) {
			servicesClasses.get(i).toogleEnable();
			return true;
		}
		return false;
	}
	
	// Cherche si le classe de service existe deja
	// Renvoie l'index du service si trouve sinon -1
	private static int containService(Class<? extends IService> service) {
		for (int i=0; i<servicesClasses.size(); i++) {
			if (servicesClasses.get(i).getService().getName().equals(service.getName())) return i;
		}
		return -1;
	}
	

	private static void validation(Class<?> classe) throws ValidationException {

		// Verif implemente l'interface Service
		boolean found = false;
		for (Class<?> i : classe.getInterfaces()) {
			if (i.getSimpleName().equals(IService.class.getSimpleName())) {
				found = true;
				break;
			}
		}
		if (!found)
			throw new ValidationException("N'implemente pas l'interface Service");

		// Verif du constructeur
		Constructor<?> c = null;
		try {
			c = classe.getConstructor(java.net.Socket.class);
		} catch (NoSuchMethodException e) {
			throw new ValidationException("Il faut un constructeur avec Socket");
		}
		int modifiers = c.getModifiers();
		if (!Modifier.isPublic(modifiers))
			throw new ValidationException("Le constructeur (Socket) doit être public");
		if (c.getExceptionTypes().length != 0)
			throw new ValidationException("Le constructeur (Socket) ne doit pas lever d'exception");

		// Verif n'est pas abstract
		if (Modifier.isAbstract(c.getModifiers()))
			throw new ValidationException("Est abstract");

		// Verif a un attribut de type Socket private final
		found = false;
		Field[] fields = classe.getDeclaredFields();
		for (Field f : fields) {
			int m = f.getModifiers();
			if (Modifier.isFinal(m) && Modifier.isPrivate(m) && f.getType() == Socket.class)
				found = true;
		}
		if (!found)
			throw new RuntimeException("Pas de Socket private final");

		// Verif a une methode toStringue public static
		found = false;
		Method[] methods = classe.getDeclaredMethods();
		for (Method m : methods) {
			int mo = m.getModifiers();
			if (Modifier.isStatic(mo) && Modifier.isPublic(mo) && m.getReturnType() == String.class
					&& m.getName().equals("toStringue"))
				found = true;
		}
		if (!found)
			throw new ValidationException("Pas de methode String toStringue public static");
	}

	public static Class<? extends IService> getServiceClass(int numService) {
		if (numService < 1 || numService > servicesClasses.size()) return null;
		UnService tmpService = servicesClasses.get(numService - 1);
		return tmpService.isEnable() ? tmpService.getService() : null;
	}

	public static String toStringue() {
		String result = "Activites presentes :##";
		int i = 1;
		// foreach n'est qu'un raccourci d'ecriture
		// donc il faut prendre le verrou explicitement sur la collection
		for (UnService s : servicesClasses) {
			if (!s.isEnable()) {
				i++;
				continue;
			}
			try {
				Method toStringue = s.getService().getMethod("toStringue");
				String string = (String) toStringue.invoke(s);
				result = result + i + " " + string + "##";
				i++;
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				e.printStackTrace(); // ??? - normalement deja teste par validation()
			}
		}
		return result;
	}
	
	public static String toStringue(String user) {
		String result = "Activites presentes :##";
		int i = 1;
		// foreach n'est qu'un raccourci d'ecriture
		// donc il faut prendre le verrou explicitement sur la collection
		for (UnService s : servicesClasses) {
			if (s.getUser().equals(user)) {
				result += i + "- " + s + "##";
				i++;
			}
		}
		return result;
	}

}
