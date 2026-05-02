package dev.pollito.stonks_java.config.log;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LogAspect {
  @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
  public void controllerPublicMethodsPointcut() {}

  @Before("controllerPublicMethodsPointcut()")
  public void logBefore(@NonNull JoinPoint joinPoint) {
    log.info(
        "[{}] Args: {}",
        joinPoint.getSignature().toShortString(),
        Arrays.toString(joinPoint.getArgs()));
  }

  @AfterReturning(pointcut = "controllerPublicMethodsPointcut()", returning = "result")
  public void logAfterReturning(@NonNull JoinPoint joinPoint, Object result) {
    log.info("[{}] Response: {}", joinPoint.getSignature().toShortString(), result);
  }
}
