package com.fren_gor.ultimateAdvancementAPI.tests;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tests annotated with this annotation are executed without an initialized {@link AdvancementMain} and database.
 * <p>Applying this annotation to a class is the same as annotating each test in that class.
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface NoAdvancementMain {
}
