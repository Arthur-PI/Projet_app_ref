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
		servicesClasses = new Vector<Class<? extends IService>>();
	}

	private static List<Class<? extends IService>> servicesClasses;

	@SuppressWarnings("unchecked")
	public static void addService(Class<?> runnableClass) throws ValidationException {
		validation(runnableClass);
		// TODO gerer les pb de concurrence
		servicesClasses.add((Class<? extends IService>) runnableClass);
	}

	private static void validation(Class<?> classe) throws ValidationException {
		
		// Verif implemente l'interface Service
		boolean found = false;
		for (Class<?> i : classe.getInterfaces()) {
			if (i == IService.class) {
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
		// TODO gerer les pb de concurrence
		return servicesClasses.get(numService - 1);
	}

	public static String toStringue() {
		// TODO gerer les pb de concurrence
		String result = "Activites presentes :##";
		int i = 1;
		// foreach n'est qu'un raccourci d'ecriture
		// donc il faut prendre le verrou explicitement sur la collection
		synchronized (servicesClasses) {
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
		}
		return result;
	}

}
