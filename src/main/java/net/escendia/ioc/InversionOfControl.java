package net.escendia.ioc;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

@Singleton
public class InversionOfControl {

	private Map<Class<?>, Object> referencedObjectMap = new HashMap<>();
	private Map<Object, Set<Method>> postProcessMap = new HashMap<>();
	private static InversionOfControl inversionOfControl;

	/**
	 * Protected default Constructor
	 */
	protected InversionOfControl() {
		referencedObjectMap = new HashMap<>();
		postProcessMap = new HashMap<>();
	}

	/**
	 * Create a {@link InversionOfControl} Object if not exists and return it.
	 * 
	 * @return {@link InversionOfControl}
	 */
	public static InversionOfControl get() {
		if (inversionOfControl == null)
			inversionOfControl = new InversionOfControl();
		return inversionOfControl;
	}

	/**
	 * Register a dependency {@link Object} with {@link Class} in a {@link HashMap}.
	 * 
	 * @param object
	 */
	public void registerDependency(Object object) {
		referencedObjectMap.put(object.getClass(), object);
	}

	/**
	 * Builds a {@link Object} or returns it form the Referenced Object Map.
	 * 
	 * @param classToBuild
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T build(Class<? extends T> classToBuild) {
		T t = null;

		if (referencedObjectMap.containsKey(classToBuild)) {
			return (T) referencedObjectMap.get(classToBuild);
		}

		try {
			t = classToBuild.newInstance();
		} catch (InstantiationException | IllegalAccessException error) {
			error.printStackTrace();
		}

		return get(classToBuild, t);
	}

	/**
	 * Register {@link Object}, inject Fields and find Annotated Methods
	 * 
	 * @param classToBuild
	 * @param t
	 * @return T
	 */
	@SuppressWarnings("unchecked")
	private <T> T get(Class<? extends T> classToBuild, T t) {
		if (referencedObjectMap.containsKey(classToBuild))
			return (T) referencedObjectMap.get(classToBuild);
		if (classToBuild.isAnnotationPresent(Singleton.class)) {
			register(t);
		}
		injectFields(t, classToBuild);
		findAnnotatedMethods(classToBuild, t);
		return t;
	}

	/**
	 * Register the {@link Object} to the Map.
	 * 
	 * @param object
	 */
	public void register(Object object) {
		if (!referencedObjectMap.containsKey(object.getClass()))
			referencedObjectMap.put(object.getClass(), object);
	}

	/**
	 * Inject the fields of the {@link Object} of the class to inject.
	 * 
	 * @param object
	 * @param classToInject
	 */
	private void injectFields(Object object, Class classToInject) {
		for (Field field : classToInject.getDeclaredFields()) {
			/**
			 * Has Annotation of the Inject.class
			 */
			if (field.isAnnotationPresent(Inject.class)) {
				field.setAccessible(true);
			}
			Class fieldType = field.getType();
			Object instanceOfFieldType = build(fieldType);
			try {
				field.set(object, instanceOfFieldType);
			} catch (IllegalAccessException error) {
				error.printStackTrace();
			}
			Class superClass = classToInject.getSuperclass();
			/**
			 * Inject the Fields of upper classes
			 */
			if (superClass != Object.class) {
				injectFields(object, superClass);
			}
		}
	}

	/**
	 * Find Annotated Methods in a {@link Class} of an {@link Object}
	 * 
	 * @param classToFindMethods
	 * @param objectToFindMethods
	 */
	private void findAnnotatedMethods(Class classToFindMethods, Object objectToFindMethods) {
		if (postProcessMap.containsKey(classToFindMethods))
			return;
		Set<Method> set = new HashSet<>();
		Class superClass = classToFindMethods.getSuperclass();
		findAnnotatedMethods(classToFindMethods, objectToFindMethods, set);
		postProcessMap.put(objectToFindMethods, set);
		if (superClass != Object.class) {
			findAnnotatedMethods(classToFindMethods, objectToFindMethods);
		}
	}

	/**
	 * Add the {@link Method}'s of a {@link Object} of a {@link Class} into a
	 * {@link Set} of {@link Method}'s
	 * 
	 * @param classToFindMethods
	 * @param objectToFindMethods
	 * @param set
	 */
	private void findAnnotatedMethods(Class classToFindMethods, Object objectToFindMethods, Set<Method> set) {
		for (Method method : classToFindMethods.getMethods()) {
			if (method.isAnnotationPresent(PostProcess.class)) {
				set.add(method);
			}
		}
	}

	/**
	 * Register Method to register {@link Object}'s of {@link Class}'es
	 * @param classToAdd
	 * @param objectToAdd
	 */
	public void registerInterfaceImplementation(Class classToAdd, Object objectToAdd) {
		referencedObjectMap.put(classToAdd, objectToAdd);
	}
	
	/**
	 * Run the post Process calculation and invoke them.
	 */
    public void postProcess() {
        for (Map.Entry<Object, Set<Method>> entry : entriesSortedByValues(postProcessMap)) {
            Set<Method> set = entry.getValue();
            for (Method m : set) {
                m.setAccessible(true);
                try {
                    m.invoke(entry.getKey());

                } catch (IllegalAccessException | InvocationTargetException error) {
                   error.printStackTrace();
                }
            }
        }
        postProcessMap.clear();
    }

    /**
     * Sorts the postProcess Methods by pritory.
     * @param map
     * @return
     */
    static SortedSet<Map.Entry<Object, Set<Method>>> entriesSortedByValues(Map<Object, Set<Method>> map) {
        SortedSet<Map.Entry<Object, Set<Method>>> sortedEntries = new TreeSet<Map.Entry<Object, Set<Method>>>(
                (o1, o2) -> {
                    int res = 0;
                    int first = 0;
                    int second = 0;
                    for (Method method : o1.getValue()) {
                        first = method.getAnnotation(PostProcess.class).priority();
                        break;
                    }
                    for (Method method : o2.getValue()) {
                        second = method.getAnnotation(PostProcess.class).priority();
                        break;
                    }
                    res = first - second;
                    return res != 0 ? res : 1;
                });
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
}

}
