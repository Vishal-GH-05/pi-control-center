package com.vishal.picontrolcentre.aop;

import com.vishal.picontrolcentre.exception.RedisUnavailableException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RedisMetricsStoreAspect {

    @Around(
            "execution(* com.vishal.picontrolcentre.store.RedisMetricsStore.*(..))"
    )
    public Object handleRedisFailures(
            ProceedingJoinPoint joinPoint
    ) throws Throwable {

        try {
            return joinPoint.proceed();

        } catch (DataAccessException ex) {

            throw new RedisUnavailableException(
                    "Redis operation failed.",
                    ex
            );
        }
    }
}
