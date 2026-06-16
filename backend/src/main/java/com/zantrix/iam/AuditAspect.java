package com.zantrix.iam;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * Aspect-Oriented Programming (AOP) component for automated audit logging.
 * <p>
 * Intercepts method calls across services, REST controllers, or methods explicitly annotated with
 * {@link AuditLoggable}. It gathers security context (user, IP, roles) and passes the data
 * to the {@link AuditService} to ensure secure, tamper-evident recording of actions as mandated by MDR/NEN7510.
 * </p>
 */
@Aspect
@Component
class AuditAspect {

    private final AuditService auditService;

    /**
     * Constructs a new {@link AuditAspect}.
     *
     * @param auditService The service to handle audit log persistence.
     */
    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * Logs the action after a method successfully returns.
     * <p>
     * Captures the actor's username, action signature, target resource, IP address, and any specific
     * patient context. Also detects if the action was performed under emergency (break-the-glass) access.
     * </p>
     *
     * @param joinPoint The join point providing details about the intercepted method.
     */
    @AfterReturning("(execution(* com.zantrix..*Service.*(..)) || @annotation(com.zantrix.iam.AuditLoggable) || within(@org.springframework.web.bind.annotation.RestController *)) && !target(com.zantrix.iam.AuditService)")
    public void logServiceAction(JoinPoint joinPoint) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null && auth.getName() != null) ? auth.getName() : "system";
        String action = joinPoint.getSignature().toShortString();
        String resource = joinPoint.getTarget().getClass().getSimpleName();

        boolean isBreakTheGlass = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMERGENCY_ACCESS"));

        String ipAddress = "UNKNOWN";
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null && attributes.getRequest() != null) {
            ipAddress = attributes.getRequest().getRemoteAddr();
        }

        String patientId = extractPatientId(joinPoint);

        AuditLog log = new AuditLog(username, action, resource, isBreakTheGlass, ipAddress, patientId);
        auditService.saveSecureLog(log);
    }

    /**
     * Extracts the patient ID from method arguments if specified by the {@link AuditLoggable} annotation.
     *
     * @param joinPoint The join point.
     * @return The patient ID, or "UNKNOWN" if not found.
     */
    private String extractPatientId(JoinPoint joinPoint) {
        if (!(joinPoint.getSignature() instanceof MethodSignature)) {
            return "UNKNOWN";
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AuditLoggable annotation = method.getAnnotation(AuditLoggable.class);
        
        if (annotation != null && !annotation.patientIdParam().isEmpty()) {
            String paramName = annotation.patientIdParam();
            String[] parameterNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            if (parameterNames != null) {
                for (int i = 0; i < parameterNames.length; i++) {
                    if (paramName.equals(parameterNames[i]) && args[i] != null) {
                        return args[i].toString();
                    }
                }
            }
        }
        return "UNKNOWN";
    }
}
