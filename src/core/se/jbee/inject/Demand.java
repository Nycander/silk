/*
 *  Copyright (c) 2012-2013, Jan Bernitt 
 *			
 *  Licensed under the Apache License, Version 2.0, http://www.apache.org/licenses/LICENSE-2.0
 */
package se.jbee.inject;

/**
 * A {@link Demand} is a {@link Dependency} resolved to a specific {@link Resource} in the context
 * of a specific {@link Injector} (the {@link Repository} within it).
 * 
 * @author Jan Bernitt (jan@jbee.se)
 * 
 * @param <T>
 *            Type of the value demanded
 */
public final class Demand<T>
		implements Resourced<T> {

	public static <T> Demand<T> demand( Resource<T> resource, Dependency<? super T> dependency,
			int serialNumber, int cardinality ) {
		return new Demand<T>( resource, dependency, serialNumber, cardinality );
	}

	private final Resource<T> resource;
	private final Dependency<? super T> dependency;
	private final int serialNumber;
	private final int cardinality;

	private Demand( Resource<T> resource, Dependency<? super T> dependency, int serialNumber,
			int cardinality ) {
		super();
		this.resource = resource;
		this.dependency = dependency;
		this.serialNumber = serialNumber;
		this.cardinality = cardinality;
	}

	public Dependency<? super T> getDependency() {
		return dependency;
	}

	@Override
	public Resource<T> getResource() {
		return resource;
	}

	/**
	 * @return the number of the {@link Injectron} being injected.
	 */
	public final int envSerialNumber() {
		return serialNumber;
	}

	/**
	 * @return the total amount of {@link Injectron}s in the same environment ({@link Injector}).
	 */
	public final int envCardinality() {
		return cardinality;
	}

	public Demand<T> from( Dependency<? super T> dependency ) {
		return new Demand<T>( resource, dependency, serialNumber, cardinality );
	}

	@Override
	public String toString() {
		return serialNumber + " " + dependency;
	}
}
