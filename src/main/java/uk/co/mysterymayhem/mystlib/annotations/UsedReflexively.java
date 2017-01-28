package uk.co.mysterymayhem.mystlib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to denote that a method/field/etc. is used with reflection, so don't delete it and be careful with its access
 * modifiers!
 * Created by Mysteryem on 2017-01-27.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface UsedReflexively {
}
