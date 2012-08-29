package de.jbee.inject.bind;

import static de.jbee.inject.Dependency.dependency;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.Test;

import de.jbee.inject.Injector;
import de.jbee.inject.bind.Bootstrapper.ModularBootstrapper;

/**
 * A test that demonstrates how to use {@link Feature}s and {@link Edition}s to allow composition of
 * different setups without introducing a single if-statement in the binding code.
 * 
 * The example also shows how {@link Annotation}s like {@link Featured} can be used to mark
 * {@link Bundle}s or {@link Module}s as a specific {@link Feature}.
 * 
 * @author Jan Bernitt (jan.bernitt@gmx.de)
 */
public class TestEditionFeatureBinds {

	public static enum AnnotatedFeature
			implements Feature<AnnotatedFeature> {
		FOO,
		BAR,
		BAZ;

		@Override
		public AnnotatedFeature featureOf( Class<?> bundleOrModule ) {
			Featured featured = bundleOrModule.getAnnotation( Featured.class );
			return featured == null
				? null
				: featured.value();
		}
	}

	@Target ( ElementType.TYPE )
	@Retention ( RetentionPolicy.RUNTIME )
	public static @interface Featured {

		AnnotatedFeature value();
	}

	private static class RootBundle
			extends BootstrapperBundle {

		@Override
		protected void bootstrap() {
			install( FeaturedBundle.class );
			install( FeaturedModule.class );
			install( FeaturedModularBundle.QUX );
		}

	}

	@Featured ( AnnotatedFeature.FOO )
	private static class FeaturedBundle
			extends BootstrapperBundle {

		@Override
		protected void bootstrap() {
			install( SomeModule.class );
		}
	}

	private static class SomeModule
			extends BinderModule {

		@Override
		protected void declare() {
			multibind( Integer.class ).to( 42 );
		}

	}

	@Featured ( AnnotatedFeature.BAR )
	private static class FeaturedModule
			extends BinderModule {

		@Override
		protected void declare() {
			multibind( Integer.class ).to( 8 );
		}

	}

	@Featured ( AnnotatedFeature.BAZ )
	private static enum FeaturedModularBundle
			implements ModularBundle<FeaturedModularBundle> {
		QUX;

		@Override
		public void bootstrap( ModularBootstrapper<FeaturedModularBundle> bootstrap ) {
			bootstrap.install( AnotherModule.class, QUX );
		}

	}

	private static class AnotherModule
			extends BinderModule {

		@Override
		protected void declare() {
			multibind( Integer.class ).to( 128 );
		}

	}

	@Test
	public void thatJustTheFeaturedBundleIsInstalled() {
		assertEditionInstalls( Bootstrap.edition( AnnotatedFeature.FOO ), 42 );
	}

	@Test
	public void thatJustTheFeaturedModuleIsInstalled() {
		assertEditionInstalls( Bootstrap.edition( AnnotatedFeature.BAR ), 8 );
	}

	@Test
	public void thatJustTheFeaturedModularBundleIsInstalled() {
		assertEditionInstalls( Bootstrap.edition( AnnotatedFeature.BAZ ), 128 );
	}

	@Test
	public void thatTheFeaturedBundlesAndModulesAreInstalled() {
		assertEditionInstalls( Bootstrap.edition( AnnotatedFeature.BAR, AnnotatedFeature.BAZ ), 8,
				128 );
	}

	private void assertEditionInstalls( Edition edition, Integer... values ) {
		Injector injector = Bootstrap.injector( RootBundle.class, edition );
		assertThat( injector.resolve( dependency( Integer[].class ) ), is( values ) );
	}

}