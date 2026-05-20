package dev.pollito.stonks_java.config.log;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class AdapterOutLogAspect {

  @Pointcut(
      "execution(public * dev.pollito.stonks_java..adapter.out..*.*(..))"
          + " && !execution(* dev.pollito.stonks_java..adapter.out..*Mapper.*(..))"
          + " && !execution(* dev.pollito.stonks_java..adapter.out..*Repository.*(..))"
          + " && !execution(* dev.pollito.stonks_java.cobol.adapter.out.CobolProgramExecutor.*(..))")
  public void adapterOutPublicMethodsPointcut() {}

  @Before("adapterOutPublicMethodsPointcut()")
  public void logBefore(@NonNull JoinPoint joinPoint) {
    log.info(
        "[{}] Args: {}",
        joinPoint.getSignature().toShortString(),
        Arrays.toString(joinPoint.getArgs()));
  }

  @AfterReturning(pointcut = "adapterOutPublicMethodsPointcut()", returning = "result")
  public void logAfterReturning(@NonNull JoinPoint joinPoint, Object result) {
    log.info("[{}] Response: {}", joinPoint.getSignature().toShortString(), result);
  }

  @AfterThrowing(pointcut = "adapterOutPublicMethodsPointcut()", throwing = "exception")
  public void logAfterThrowing(@NonNull JoinPoint joinPoint, @NonNull Throwable exception) {
    log.info(
        "[{}] Args: {} | Exception: {}",
        joinPoint.getSignature().toShortString(),
        Arrays.toString(joinPoint.getArgs()),
        exception.toString());
  }
}
