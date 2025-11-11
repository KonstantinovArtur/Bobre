package ru.forge.blacksmith_shop.common.audit;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;              // + NEW
import org.springframework.web.context.request.ServletRequestAttributes;    // + NEW
import ru.forge.blacksmith_shop.users.repo.UserRepository;

@Aspect
@Component
public class SetAppUserAspect {

    private final UserRepository users;

    @PersistenceContext
    private EntityManager em;

    public SetAppUserAspect(UserRepository users) {
        this.users = users;
    }

    // Существенно сузили скоуп: только Controller/RestController, +
    // исключили сам аудит И springdoc (org.springdoc..).
    @Around("(" +
            "within(@org.springframework.stereotype.Controller *) || " +
            "within(@org.springframework.web.bind.annotation.RestController *)" +
            ") && " +
            "!within(ru.forge.blacksmith_shop.common.audit..*) && " +
            "!within(org.springdoc..*)"
    )
    public Object setUserIdAround(ProceedingJoinPoint pjp) throws Throwable {

        // --- ГАРД ПО URI: не трогаем swagger/api-docs/actuator ---
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String uri = (attrs != null && attrs.getRequest() != null) ? attrs.getRequest().getRequestURI() : "";
        if (uri.startsWith("/v3/api-docs") ||
                uri.startsWith("/swagger-ui") ||
                uri.startsWith("/swagger-resources") ||
                uri.startsWith("/actuator")) {
            return pjp.proceed();
        }
        // ----------------------------------------------------------

        Integer uid = resolveCurrentUserId();
        try {
            if (uid != null) {
                em.createNativeQuery("select set_config('app.user_id', cast(:uid as text), true)")
                        .setParameter("uid", uid)
                        .getSingleResult();
            } else {
                em.createNativeQuery("select set_config('app.user_id', null, true)")
                        .getSingleResult();
            }
        } catch (Exception ignored) {}

        try {
            return pjp.proceed();
        } finally {
            try {
                em.createNativeQuery("select set_config('app.user_id', null, true)").getSingleResult();
            } catch (Exception ignored) {}
        }
    }

    private Integer resolveCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        String login = auth.getName(); // username = login
        return users.findByLogin(login)
                .map(u -> u.getUserId()) // если поле иначе — поправь
                .orElse(null);
    }
}
