package com.vishal.picontrolcentre.aop;

import com.vishal.picontrolcentre.exception.RedisUnavailableException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RedisMetricsStoreAspect {

    private static final Logger logger =
            LoggerFactory.getLogger(RedisMetricsStoreAspect.class);

    @Around(
            "execution(* com.vishal.picontrolcentre.store.RedisMetricsStore.*(..))"
    )
    public Object handleRedisFailures(
            ProceedingJoinPoint joinPoint
    ) throws Throwable {

        try {
            return joinPoint.proceed();

        } catch (DataAccessException ex) {

            String operation = joinPoint.getSignature().toShortString();

            logger.error(
                    "Redis operation failed: {}",
                    operation,
                    ex
            );

            throw new RedisUnavailableException(
                    "Redis operation failed: " + operation,
                    ex
            );
        }
    }
}
