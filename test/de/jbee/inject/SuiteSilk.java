package de.jbee.inject;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.jbee.inject.bind.SuiteBind;

@RunWith ( Suite.class )
@SuiteClasses ( { TestName.class, TestType.class, SuiteBind.class } )
public class SuiteSilk {
	// all project tests
}