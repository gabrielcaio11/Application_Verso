package br.com.gabrielcaio.verso.infrastructure.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class RepositoryLoggingAspect
{

    /**
     * Intercepta todos os métodos públicos de todos os repositórios do pacote
     * br.com.gabrielcaio.verso.repositories Inclui: save, findById, findAll, delete, query
     * methods,
     *
     * @Query, etc.
     */
    @Before("within(br.com.gabrielcaio.verso.repositories..*) && execution(public * *(..))")
    public void logBefore(JoinPoint joinPoint)
    {
        String repository = joinPoint.getTarget()
                .getClass()
                .getSimpleName();
        String method = joinPoint.getSignature()
                .getName();

        Object[] args = joinPoint.getArgs();

        log.info(
                "[REPOSITORY CALL] {}.{} | args={}",
                repository,
                method,
                formatArguments(args)
        );
    }

    @AfterReturning(
            pointcut = "within(br.com.gabrielcaio.verso.repositories..*) && execution(public * *(..))",
            returning = "result"
    )
    public void logAfterReturning(JoinPoint joinPoint, Object result)
    {
        String repository = joinPoint.getTarget()
                .getClass()
                .getSimpleName();
        String method = joinPoint.getSignature()
                .getName();

        log.info(
                "[REPOSITORY RETURN] {}.{} | result={}",
                repository,
                method,
                formatReturn(result)
        );
    }

    /**
     * Evita logs gigantes — especialmente entidades com: relações ManyToOne / OneToMany
     */
    private Object formatArguments(Object[] args)
    {
        if(args == null || args.length == 0)
        {
            return "[]";
        }

        Object first = args[0];

        // Se for entidade, loga só o ID se existir
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
