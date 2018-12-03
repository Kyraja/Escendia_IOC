package net.escendia.ioc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation interface for Injection Fields, Constructors, Type Parameter and Parameter
 * @author FPetersen
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.TYPE_PARAMETER, ElementType.PARAMETER})
public @interface Inject {

	InjectPolicy value() default InjectPolicy.NORMAL;
	
	enum InjectPolicy {
		NORMAL;
	}
}
