package de.jbee.inject;

public class Availability
		implements Preciser<Availability> {

	public static final Availability EVERYWHERE = availability( Instance.ANY );

	public static Availability availability( Instance<?> target ) {
		return new Availability( target, "", -1 );
	}

	private final Instance<?> target;
	private final String path;
	private final int depth;

	private Availability( Instance<?> target, String path, int depth ) {
		super();
		this.target = target;
		this.path = path;
		this.depth = depth;
	}

	public Availability injectingInto( Instance<?> target ) {
		return new Availability( target, path, depth );
	}

	public boolean isApplicableFor( Dependency<?> dependency ) {

		return true;
	}

	@Override
	public String toString() {
		if ( target.isAny() && path.isEmpty() && depth < 0 ) {
			return "everywhere";
		}
		return "[" + path + "-" + depth + "-" + target + "]";
	}

	public Availability within( String path ) {
		return new Availability( target, path, depth );
	}

	@Override
	public boolean morePreciseThan( Availability other ) {
		// TODO Auto-generated method stub
		return true;
	}
}
