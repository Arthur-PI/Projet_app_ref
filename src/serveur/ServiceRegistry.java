package serveur;

import java.lang.reflect.*;
import java.net.Socket;
import java.util.*;

import service.IService;

// TODO test concurrences mais Vector == threadsafe
public class ServiceRegistry {
	private static List<Class<? extends IService>> servicesClasses;

	static {
		servicesClasses = new Vector<Class<? extends IService>>();
	}

	@SuppressWarnings("unchecked")
	public static void addService(Class<?> runnableClass) throws ValidationException {
		validation(runnableClass);
		Class<? extends IService> tmpService = (Class<? extends IService>) runnableClass;
		int index = containService(tmpService);
		if (index != -1) {
			servicesClasses.set(index, tmpService);
			return;
		}
		servicesClasses.add(tmpService);
	}
	
	// Cherche si le classe de service existe deja
	// Renvoie l'index du service si trouve sinon -1
	private static int containService(Class<? extends IService> service) {
		for (int i=0; i<servicesClasses.size(); i++) {
			if (servicesClasses.get(i).getName().equals(service.getName())) return i;
		}
		return -1;
	}
	

	private static void validation(Class<?> classe) throws ValidationException {

		// Verif implemente l'interface Service
		System.out.println("caca et " + classe.getName());
		boolean found = false;
		for (Class<?> i : classe.getInterfaces()) {
			System.out.println(i.getName() + " et " + IService.class.getName());
			if (i.getName().equals(IService.class.getName())) {
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
		return servicesClasses.get(numService - 1);
	}

	public static String toStringue() {
		String result = "Activites presentes :##";
		int i = 1;
		// foreach n'est qu'un raccourci d'ecriture
		// donc il faut prendre le verrou explicitement sur la collection
		for (Class<? extends IService> s : servicesClasses) {
			try {
				Method toStringue = s.getMethod("toStringue");
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

}
