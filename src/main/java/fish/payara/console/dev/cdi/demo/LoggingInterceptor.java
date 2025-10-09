package fish.payara.console.dev.cdi.demo;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.util.logging.Logger;

@Interceptor
@Logging
@Priority(Interceptor.Priority.APPLICATION)
public class LoggingInterceptor {

    private static final Logger logger = Logger.getLogger(LoggingInterceptor.class.getName());

    @AroundInvoke
    public Object logMethodEntry(InvocationContext context) throws Exception {
        logger.info(() -> "Entering method: " + context.getMethod().getName());
        try {
            return context.proceed();
        } finally {
            logger.info(() -> "Exiting method: " + context.getMethod().getName());
        }
    }
}
