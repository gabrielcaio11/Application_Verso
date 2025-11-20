package br.com.gabrielcaio.verso.infrastructure.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ServiceLoggingAspect
{

    /**
     * Intercepta todos os métodos públicos de todos os services. Exemplo: ArticleService,
     * UserService, CategoryService, etc.
     */
    @Around("within(br.com.gabrielcaio.verso.services..*) && execution(public * *(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable
    {

        String serviceName = joinPoint.getTarget()
                .getClass()
                .getSimpleName();
        String methodName = joinPoint.getSignature()
                .getName();

        Object[] args = joinPoint.getArgs();
        long start = System.currentTimeMillis();

        log.info(
                "[SERVICE CALL] {}.{} | args={}",
                serviceName,
                methodName,
                formatArguments(args)
        );

        try
        {
            Object result = joinPoint.proceed(); // executa o método real
            long executionTime = System.currentTimeMillis() - start;

            log.info(
                    "[SERVICE RETURN] {}.{} | result={} | time={}ms",
                    serviceName,
                    methodName,
                    formatReturn(result),
                    executionTime
            );

            return result;
        } catch(Exception ex)
        {
            long executionTime = System.currentTimeMillis() - start;

            log.error(
                    "[SERVICE ERROR] {}.{} | message={} | time={}ms",
                    serviceName,
                    methodName,
                    ex.getMessage(),
                    executionTime
            );

            throw ex;
        }
    }

    // --- Mesmos utilitários inteligentes de sanitização usados no RepositoryLoggingAspect ---

    private Object formatArguments(Object[] args)
    {
        if(args == null || args.length == 0)
        {
            return "[]";
        }

        Object first = args[0];

        if(hasId(first))
        {
            return "entity-id=" + extractId(first);
        }

        return args;
    }

    private Object formatReturn(Object result)
    {
        if(result == null)
        {
            return "null";
        }
        if(hasId(result))
        {
            return "entity-id=" + extractId(result);
        }
        return result;
    }

    private boolean hasId(Object obj)
    {
        if(obj == null)
        {
            return false;
        }
        try
        {
            obj.getClass()
                    .getMethod("getId");
            return true;
        } catch(Exception ignored)
        {
            return false;
        }
    }

    private Object extractId(Object obj)
    {
        try
        {
            return obj.getClass()
                    .getMethod("getId")
                    .invoke(obj);
        } catch(Exception e)
        {
            return "?";
        }
    }
}
