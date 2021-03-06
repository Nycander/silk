package se.jbee.inject.bind;

import static org.junit.Assert.fail;
import static se.jbee.inject.Dependency.dependency;

import org.junit.Test;

import se.jbee.inject.Injector;
import se.jbee.inject.DIRuntimeException.MoreFrequentExpiryException;
import se.jbee.inject.bootstrap.Bootstrap;
import se.jbee.inject.util.Scoped;

public class TestScopedBinds {

	private static class Foo {

		@SuppressWarnings ( "unused" )
		Foo( Bar bar ) {
			// it is just about the instances
		}
	}

	private static class Bar {
		// just to demo
	}

	private static class ScopedBindsModule
			extends BinderModule {

		@Override
		protected void declare() {
			per( Scoped.APPLICATION ).construct( Foo.class );
			per( Scoped.INJECTION ).construct( Bar.class );
		}
	}

	@Test ( expected = MoreFrequentExpiryException.class )
	public void thatInjectingAnInjectionScopedInstanceIntoAppScopedInstanceThrowsAnException() {
		Injector injector = Bootstrap.injector( ScopedBindsModule.class );
		Foo foo = injector.resolve( dependency( Foo.class ) );
		fail( "It should not be possible to create a foo but got one: " + foo );
	}
}
