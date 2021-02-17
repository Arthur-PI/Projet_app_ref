package serveur;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

public class ServiceRegistry {

	static {
		servicesClasses = new Vector<Class<? extends Service>>();
	}

	private static List<Class<? extends Service>> servicesClasses;

	public static void addService(Class<? extends Service> runnableClass) throws ValidationException {
		validation(runnableClass);
		// TODO gérer les pb de concurrence
		servicesClasses.add(runnableClass);
	}

	private static void validation(Class<? extends Service> classe) throws ValidationException {

		// Vérif du constructeur
		Constructor<? extends Service> c = null;
		try {
			c = classe.getConstructor(java.net.Socket.class);
		} catch (NoSuchMethodException e) {
			throw new ValidationException("Il faut un constructeur avec Socket");
		}
		int modifiers = c.getModifiers();
		if (!Modifier.isPublic(modifiers))
			throw new ValidationException("Le constructeur (Socket) doit Ãªtre public");
		if (c.getExceptionTypes().length != 0)
			throw new ValidationException("Le constructeur (Socket) ne doit pas lever d'exception");

		// Vérif implémente l'interface Service
		boolean found = false;
		for (Class<?> i : classe.getInterfaces())
			if (i == Service.class)
				found = true;
		if (!found)
			throw new ValidationException("N'implémente pas l'interface Service");

		// Vérif n'est pas abstract
		if (Modifier.isAbstract(c.getModifiers()))
			throw new ValidationException("Est abstract");

		// Vérif a un attribut de type Socket private final
		found = false;
		Field[] fields = classe.getDeclaredFields();
		for (Field f : fields) {
			int m = f.getModifiers();
			if (Modifier.isFinal(m) && Modifier.isPrivate(m) && f.getType() == Socket.class)
				found = true;
		}
		if (!found)
			throw new RuntimeException("Pas de Socket private final");

		// Vérif a une méthode toStringue public static
		found = false;
		Method[] methods = classe.getDeclaredMethods();
		for (Method m : methods) {
			int mo = m.getModifiers();
			if (Modifier.isStatic(mo) && Modifier.isPublic(mo) && m.getReturnType() == String.class
					&& m.getName().equals("toStringue"))
				found = true;
		}
		if (!found)
			throw new ValidationException("Pas de méthode String toStringue public static");
	}

	public static Class<? extends Service> getServiceClass(int numService) {
		// TODO gérer les pb de concurrence
		return servicesClasses.get(numService - 1);
	}

	public static String toStringue() {
		// TODO gérer les pb de concurrence
		String result = "Activités présentes :##";
		int i = 1;
		// foreach n'est qu'un raccourci d'écriture
		// donc il faut prendre le verrou explicitement sur la collection
		synchronized (servicesClasses) {
			for (Class<? extends Service> s : servicesClasses) {
				try {
					Method toStringue = s.getMethod("toStringue");
					String string = (String) toStringue.invoke(s);
					result = result + i + " " + string + "##";
					i++;
				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					e.printStackTrace(); // ??? - normalement déjà  testé par validation()
				}
			}
		}
		return result;
	}

}
