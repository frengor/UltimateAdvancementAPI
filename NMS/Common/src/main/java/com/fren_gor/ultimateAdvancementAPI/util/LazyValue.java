package com.fren_gor.ultimateAdvancementAPI.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A field annotated with {@code LazyValue} should not be assigned until it is used for the first time.
 * Then, its real value should be calculated and assigned back in order to be used henceforth.
 * {@code LazyValue} is useful for fields that are expensive to construct.
 * <p>This annotation only applies to fields.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface LazyValue {
}
