package de.jbee.inject;

/**
 * A kind of singleton for a {@link Resource} inside a {@link Injector}.
 * 
 * @author Jan Bernitt (jan.bernitt@gmx.de)
 */
public interface Injectron<T> {

	Resource<T> getResource();

	Source getSource();

	T provide( Dependency<T> dependency, DependencyContext context );

	//void inject( Dependency<T> dependency, InjectionPoint<T> point );
}