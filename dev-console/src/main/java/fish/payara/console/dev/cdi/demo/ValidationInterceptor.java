package fish.payara.console.dev.cdi.demo;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.util.logging.Logger;

@Interceptor
@Validate
@Priority(Interceptor.Priority.APPLICATION)
public class ValidationInterceptor {

    private static final Logger logger = Logger.getLogger(ValidationInterceptor.class.getName());

    @AroundInvoke
    public Object logMethodEntry(InvocationContext context) throws Exception {
        logger.info(() -> "Validate entry method: " + context.getMethod().getName());
        try {
            return context.proceed();
        } finally {
            logger.info(() -> "Validate exit method: " + context.getMethod().getName());
        }
    }
}
