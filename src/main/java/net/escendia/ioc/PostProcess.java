package net.escendia.ioc;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation interface for PostProcess Methods
 * @author FPetersen
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PostProcess {
	/**
	 * Priority of the PostProcess Method
	 */
	int priority() default 0;
}
