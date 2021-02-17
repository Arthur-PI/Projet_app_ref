package serveur;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Vector;

public class ServiceRegistry {
	
	static {
		servicesClasses = new Vector<Class<? extends Service>>();
	}
	
	private static List<Class<? extends Service>> servicesClasses;

	public static void addService(Class<? extends Service> runnableClass) throws ValidationException {
		validation(runnableClass);
		// TODO gerer les pb de concurrence
		servicesClasses.add(runnableClass);
	}

	private static void validation(Class<? extends Service> classe) throws ValidationException {
		
		Constructor<? extends Service> c = null;
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
		
		// TODO finir les verifs
	
	}

	public static Class<? extends Service> getServiceClass(int numService) {
		// TODO gerer les pb de concurrence
		return servicesClasses.get(numService-1);
	}
	
	public static String toStringue() {
		// TODO gerer les pb de concurrence
		String result = "Activités présentes :##";
		int i = 1;
		// foreach n'est qu'un raccourci d'écriture 
		// donc il faut prendre le verrou explicitement sur la collection
		synchronized (servicesClasses) { 
			for (Class<? extends Service> s : servicesClasses) {
				try {
					Method toStringue = s.getMethod("toStringue");
					String string = (String) toStringue.invoke(s);
					result = result + i + " " + string+"##";
					i++;
				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					e.printStackTrace(); // ??? - normalement déjà testé par validation()
				}
			}
		}
		return result;
	}

}
