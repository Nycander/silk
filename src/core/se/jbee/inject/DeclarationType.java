/*
 *  Copyright (c) 2012-2013, Jan Bernitt 
 *			
 *  Licensed under the Apache License, Version 2.0, http://www.apache.org/licenses/LICENSE-2.0
 */
package se.jbee.inject;

/**
 * The {@link DeclarationType} is used to keep track of the origin of binding declarations. They
 * describe how or why a binding has been made whereby they get different significance and meaning.
 * 
 * While binds consciously done by API calls through the programmer result in {@link #EXPLICIT} or
 * {@link #MULTI} there are 3 weaker types for those binds that added indirectly. They provide a
 * convenient fall-back behaviour that allows to omit simple self-evident binds but allow to
 * override these fall-backs explicitly.
 * 
 * It is important to distinguish binds in that way since binds always have to be unambiguous. Two
 * equivalent binds would {@link #clashesWith(DeclarationType)} each other.
 * 
 * @author Jan Bernitt (jan@jbee.se)
 */
public enum DeclarationType
		implements PreciserThan<DeclarationType> {

	/**
	 * Has been added by the binder as a fall-back since some bind-calls can have ambiguous
	 * intentions.
	 */
	IMPLICIT,

	/**
	 * Used to provide a default of required parts of a module that can be replaced *once* to
	 * customize behavior.
	 * 
	 * There can be just *one* default for each {@link Resource} and still just one explicit
	 * replacement for it.
	 */
	DEFAULT,

	/**
	 * 
	 */
	PROVIDED,

	/**
	 * A auto-bind has been used. That is binding a class or instance to the exact type as
	 * {@link #EXPLICIT} and to all its super-classes and -interfaces as a {@link #AUTO} bound bind.
	 */
	AUTO,
	/**
	 * A bind that is meant to co-exist with others that might have the same {@link Resource}. Those
	 * have to be defined as {@link #MULTI} as well!
	 */
	MULTI,
	/**
	 * The bind has been made explicitly by a module (should be a unique {@link Resource})
	 */
	EXPLICIT,

	/**
	 * A binding that is just expressing the instance needed but not how to supply it. This allows
	 * to express needs in a module without knowing what will be the implementation and ensure
	 * (during bootstrapping) that there will be a known implementation defined.
	 */
	REQUIRED;

	@Override
	public boolean morePreciseThan( DeclarationType other ) {
		return ordinal() > other.ordinal();
	}

	public boolean clashesWith( DeclarationType other ) {
		return ordinal() + other.ordinal() > MULTI.ordinal() * 2 && this != REQUIRED
				|| ( this == DEFAULT && other == DEFAULT );
	}

	public boolean replacedBy( DeclarationType other ) {
		return other.ordinal() > ordinal() || ( this == IMPLICIT && other == IMPLICIT );
	}

	public boolean nullifiedBy( DeclarationType other ) {
		return this == other && ( this == AUTO || this == PROVIDED );
	}

}
