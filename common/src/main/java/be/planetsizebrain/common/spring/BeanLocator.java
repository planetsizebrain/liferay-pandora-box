package be.planetsizebrain.common.spring;

import javax.inject.Named;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.GenericTypeResolver;

/**
 * This static class can be used to retrieve Spring managed services inside Liferay managed classes such as: portlet
 * classes, hook classes (model listeners, startup actions, service overrides), scheduler jobs, ...
 */
@Named
public class BeanLocator implements ApplicationContextAware {

	private static ApplicationContext ctx;

	/**
	 * Returns the bean of the specified type.
	 */
	public static <T> T getBean(Class<T> serviceClass) {
		return ctx.getBean(serviceClass);
	}

	/**
	 * Returns the bean of the specified full qualified class name.
	 */
	public static <T> T getBean(String className) {
		try {
			return (T) getBean(Class.forName(className));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Bean not found", e);
		}
	}

	/**
	 * Returns the bean of the generic type T defined in genericInterfaceClass and implemented in targetClass. Use this
	 * method if you want to dynamically get a bean instance based on the generic parameter in the
	 * genericInterfaceClass. Usage: BeanLocator.getGenericBean(MyAbstractClass.class, getClass())
	 */
	public static <T> T getGenericBean(Class<?> genericInterfaceClass, Class<?> targetClass) {
		return (T) getBean(GenericTypeResolver.resolveTypeArgument(targetClass, genericInterfaceClass));
	}

	public static boolean isInitialized() {
		return ctx != null;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		ctx = applicationContext;
	}
}