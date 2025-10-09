package fish.payara.console.dev.core;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.TYPE})
public @interface DevConsoleQualifier {

    public static final class Literal extends AnnotationLiteral<DevConsoleQualifier> implements DevConsoleQualifier {
        public static final Literal INSTANCE = new Literal();
        private Literal() {}
    }
}
