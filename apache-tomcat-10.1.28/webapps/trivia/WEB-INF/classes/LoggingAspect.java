import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.ProceedingJoinPoint;
import java.util.logging.Logger;

@Aspect
public class LoggingAspect {

    private static Logger logger = Logger.getLogger(LoggingAspect.class.getName());

    // Pointcut to match all servlets in your package
    @Around("execution(* *..*Servlet.do*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        logger.info("Starting execution for: " + joinPoint.getSignature());
        
        // Proceed with the original method
        Object result = joinPoint.proceed();
        
        long timeTaken = System.currentTimeMillis() - startTime;
        logger.info("Completed execution for: " + joinPoint.getSignature() + " in " + timeTaken + "ms");
        
        return result;
    }
}
