package de.jbee.inject.util;

import static de.jbee.inject.Dependency.dependency;
import static de.jbee.inject.Type.parameterTypes;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import de.jbee.inject.Dependency;
import de.jbee.inject.Injector;
import de.jbee.inject.Instance;
import de.jbee.inject.Parameter;
import de.jbee.inject.Supplier;
import de.jbee.inject.Type;

public final class SuppliedBy {

	private static final Object[] NO_ARGS = new Object[0];

	public static final Supplier<Provider<?>> PROVIDER_BRIDGE = new ProviderSupplier();
	public static final Supplier<List<?>> LIST_BRIDGE = new ArrayToListBridgeSupplier();
	public static final Supplier<Set<?>> SET_BRIDGE = new ArrayToSetBridgeSupplier();
	public static final Factory<Logger> LOGGER = new LoggerFactory();

	public static <T> Supplier<T> provider( Provider<T> provider ) {
		return new ProviderAsSupplier<T>( provider );
	}

	public static <T> Supplier<Provider<T>> constant( Provider<T> provider ) {
		return new ConstantSupplier<Provider<T>>( provider );
	}

	public static <T> Supplier<T> constant( T constant ) {
		return new ConstantSupplier<T>( constant );
	}

	public static <T> Supplier<T> reference( Class<? extends Supplier<? extends T>> type ) {
		return new ReferenceSupplier<T>( type );
	}

	public static <T> Supplier<T> instance( Instance<T> instance ) {
		return new InstanceSupplier<T>( instance );
	}

	public static <T> Supplier<T> parametrizedInstance( Instance<T> instance ) {
		return new ParametrizedInstanceSupplier<T>( instance );
	}

	public static <E> Supplier<E[]> elements( Class<E[]> arrayType, Supplier<? extends E>[] elements ) {
		return new ElementsSupplier<E>( arrayType, elements );
	}

	public static <T> Supplier<T> method( Type<T> returnType, Method factory,
			Parameter<?>... parameters ) {
		if ( !Type.returnType( factory ).isAssignableTo( returnType ) ) {
			throw new IllegalArgumentException( "The factory methods methods return type `"
					+ Type.returnType( factory ) + "` is not assignable to: " + returnType );
		}
		Argument<?>[] arguments = Argument.arguments( Type.parameterTypes( factory ), parameters );
		return new FactoryMethodSupplier<T>( returnType, factory, arguments );
	}

	public static <T> Supplier<T> costructor( Constructor<T> constructor ) {
		final Class<?>[] params = constructor.getParameterTypes();
		if ( params.length == 0 ) {
			return new StaticConstructorSupplier<T>( constructor, NO_ARGS );
		}
		return costructor( constructor, new Parameter[0] );
	}

	public static <T> Supplier<T> costructor( Constructor<T> constructor,
			Parameter<?>... parameters ) {
		Argument<?>[] arguments = Argument.arguments( parameterTypes( constructor ), parameters );
		return Argument.allConstants( arguments )
			? new StaticConstructorSupplier<T>( constructor, Argument.constantsFrom( arguments ) )
			: new ConstructorSupplier<T>( constructor, arguments );
	}

	public static <T> Supplier<T> factory( Factory<T> factory ) {
		return new FactorySupplier<T>( factory );
	}

	private SuppliedBy() {
		throw new UnsupportedOperationException( "util" );
	}

	private static abstract class ArrayBridgeSupplier<T>
			implements Supplier<T> {

		ArrayBridgeSupplier() {
			// make visible
		}

		@Override
		public final T supply( Dependency<? super T> dependency, Injector context ) {
			Type<?> elementType = !dependency.getType().isParameterized()
				? Type.WILDCARD
				: dependency.getType().getParameters()[0];
			return bridge( supplyArray( dependency.anyTyped( elementType.getArrayType() ), context ) );
		}

		private <E> E[] supplyArray( Dependency<E[]> elementType, Injector resolver ) {
			return resolver.resolve( elementType );
		}

		abstract <E> T bridge( E[] elements );
	}

	/**
	 * Shows how support for {@link List}s and such works.
	 * 
	 * Basically we just resolve the array of the element type (generic of the list). Arrays itself
	 * have build in support that will (if not redefined by a more precise binding) return all known
	 * 
	 * @author Jan Bernitt (jan.bernitt@gmx.de)
	 * 
	 */
	private static final class ArrayToListBridgeSupplier
			extends ArrayBridgeSupplier<List<?>> {

		ArrayToListBridgeSupplier() {
			//make visible
		}

		@Override
		<E> List<E> bridge( E[] elements ) {
			return new ArrayList<E>( Arrays.asList( elements ) );
		}

	}

	private static final class ArrayToSetBridgeSupplier
			extends ArrayBridgeSupplier<Set<?>> {

		ArrayToSetBridgeSupplier() {
			// make visible
		}

		@Override
		<E> Set<E> bridge( E[] elements ) {
			return new HashSet<E>( Arrays.asList( elements ) );
		}

	}

	private static final class ConstantSupplier<T>
			implements Supplier<T> {

		private final T instance;

		ConstantSupplier( T instance ) {
			super();
			this.instance = instance;
		}

		@Override
		public T supply( Dependency<? super T> dependency, Injector context ) {
			return instance;
		}

		@Override
		public String toString() {
			return instance.toString();
		}

	}

	/**
	 * A {@link Supplier} uses multiple different separate suppliers to provide the elements of a
	 * array of the supplied type.
	 * 
	 * @author Jan Bernitt (jan.bernitt@gmx.de)
	 */
	private static final class ElementsSupplier<E>
			implements Supplier<E[]> {

		private final Class<E[]> arrayType;
		private final Supplier<? extends E>[] elements;

		ElementsSupplier( Class<E[]> arrayType, Supplier<? extends E>[] elements ) {
			super();
			this.arrayType = arrayType;
			this.elements = elements;
		}

		@SuppressWarnings ( "unchecked" )
		@Override
		public E[] supply( Dependency<? super E[]> dependency, Injector context ) {
			E[] res = (E[]) Array.newInstance( arrayType.getComponentType(), elements.length );
			int i = 0;
			final Dependency<E> elementDependency = (Dependency<E>) dependency.typed( Type.raw(
					arrayType ).elementType() );
			for ( Supplier<? extends E> e : elements ) {
				res[i++] = e.supply( elementDependency, context );
			}
			return res;
		}

	}

	private static final class ReferenceSupplier<T>
			implements Supplier<T> {

		private final Class<? extends Supplier<? extends T>> type;

		ReferenceSupplier( Class<? extends Supplier<? extends T>> type ) {
			super();
			this.type = type;
		}

		@Override
		public T supply( Dependency<? super T> dependency, Injector context ) {
			final Supplier<? extends T> supplier = context.resolve( dependency.anyTyped( type ) );
			return supplier.supply( dependency, context );
		}
	}

	private static final class ParametrizedInstanceSupplier<T>
			implements Supplier<T> {

		private final Instance<? extends T> instance;

		ParametrizedInstanceSupplier( Instance<? extends T> instance ) {
			super();
			this.instance = instance;
		}

		@Override
		public T supply( Dependency<? super T> dependency, Injector context ) {
			Type<? super T> type = dependency.getType();
			Instance<? extends T> parametrized = instance.typed( instance.getType().parametized(
					type.getParameters() ).lowerBound( dependency.getType().isLowerBound() ) );
			return context.resolve( dependency.instanced( parametrized ) );
		}

		@Override
		public String toString() {
			return instance.toString();
		}

	}

	private static final class InstanceSupplier<T>
			implements Supplier<T> {

		private final Instance<? extends T> instance;

		InstanceSupplier( Instance<? extends T> instance ) {
			super();
			this.instance = instance;
		}

		@Override
		public T supply( Dependency<? super T> dependency, Injector context ) {
			return context.resolve( dependency.instanced( instance ) );
		}

		@Override
		public String toString() {
			return instance.toString();
		}
	}

	private static final class ProviderAsSupplier<T>
			implements Supplier<T> {

		private final Provider<T> provider;

		ProviderAsSupplier( Provider<T> provider ) {
			super();
			this.provider = provider;
		}

		@Override
		public T supply( Dependency<? super T> dependency, Injector context ) {
			return provider.provide();
		}

	}

	private static final class ProviderSupplier
			implements Supplier<Provider<?>> {

		ProviderSupplier() {
			//make visible
		}

		@Override
		public Provider<?> supply( Dependency<? super Provider<?>> dependency, Injector context ) {
			Dependency<?> providedType = dependency.onTypeParameter();
			if ( !dependency.getName().isDefault() ) {
				providedType = providedType.named( dependency.getName() );
			}
			return newProvider( providedType, context );
		}

		private <T> Provider<T> newProvider( Dependency<T> dependency, Injector context ) {
			return new LazyResolvedDependencyProvider<T>( dependency, context );
		}
	}

	private static final class LazyResolvedDependencyProvider<T>
			implements Provider<T> {

		private final Dependency<T> dependency;
		private final Injector resolver;

		LazyResolvedDependencyProvider( Dependency<T> dependency, Injector resolver ) {
			super();
			this.dependency = dependency;
			this.resolver = resolver;
		}

		@Override
		public T provide() {
			return resolver.resolve( dependency );
		}

		@Override
		public String toString() {
			return "provides<" + dependency + ">";
		}
	}

	/**
	 * Adapter to a simpler API that will not need any {@link Injector} to supply it's value in any
	 * case.
	 * 
	 * @author Jan Bernitt (jan.bernitt@gmx.de)
	 * 
	 */
	private static final class FactorySupplier<T>
			implements Supplier<T> {

		private final Factory<T> factory;

		FactorySupplier( Factory<T> factory ) {
			super();
			this.factory = factory;
		}

		@Override
		public T supply( Dependency<? super T> dependency, Injector context ) {
			return factory.produce( dependency.getInstance(), dependency.target( 1 ) );
		}

	}

	/**
	 * A simple version for all the constructors where we know all arguments as constants.
	 */
	private static final class StaticConstructorSupplier<T>
			implements Supplier<T> {

		private final Constructor<T> constructor;
		private final Object[] arguments;

		StaticConstructorSupplier( Constructor<T> constructor, Object[] arguments ) {
			super();
			TypeReflector.makeAccessible( constructor );
			this.constructor = constructor;
			this.arguments = arguments;
		}

		@Override
		public T supply( Dependency<? super T> dependency, Injector context ) {
			return TypeReflector.construct( constructor, arguments );
		}

	}

	private static final class ConstructorSupplier<T>
			implements Supplier<T> {

		private final Constructor<T> constructor;
		private final Argument<?>[] arguments;

		ConstructorSupplier( Constructor<T> constructor, Argument<?>[] arguments ) {
			super();
			TypeReflector.makeAccessible( constructor );
			this.constructor = constructor;
			this.arguments = arguments;
		}

		@Override
		public T supply( Dependency<? super T> dependency, Injector context ) {
			return TypeReflector.construct( constructor, Argument.resolve( dependency, context,
					arguments ) );
		}

	}

	private static final class FactoryMethodSupplier<T>
			implements Supplier<T> {

		private final Type<T> returnType;
		private final Method factory;
		private final Argument<?>[] arguments;
		private final boolean instanceMethod;

		FactoryMethodSupplier( Type<T> returnType, Method factory, Argument<?>[] arguments ) {
			super();
			TypeReflector.makeAccessible( factory );
			this.returnType = returnType;
			this.factory = factory;
			this.arguments = arguments;
			this.instanceMethod = !Modifier.isStatic( factory.getModifiers() );
		}

		@Override
		public T supply( Dependency<? super T> dependency, Injector context ) {
			Object owner = null;
			if ( instanceMethod ) {
				owner = context.resolve( dependency( factory.getDeclaringClass() ) );
			}
			final Object[] args = Argument.resolve( dependency, context, arguments );
			return returnType.getRawType().cast( TypeReflector.invoke( factory, owner, args ) );
		}

	}

	private static class LoggerFactory
			implements Factory<Logger> {

		LoggerFactory() {
			// make visible
		}

		@Override
		public <P> Logger produce( Instance<? super Logger> produced, Instance<P> injected ) {
			return Logger.getLogger( injected.getType().getRawType().getCanonicalName() );
		}

	}
}
