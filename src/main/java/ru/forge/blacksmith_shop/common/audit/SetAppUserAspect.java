package ru.forge.blacksmith_shop.common.audit;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.forge.blacksmith_shop.users.repo.UserRepository;

@Aspect
@Component
public class SetAppUserAspect {

    private final UserRepository users;

    @PersistenceContext
    private EntityManager em;

    // Явный конструктор (вместо Lombok)
    public SetAppUserAspect(UserRepository users) {
        this.users = users;
    }

  @Around("(" +
          "within(@org.springframework.stereotype.Controller *) || " +
          "within(@org.springframework.web.bind.annotation.RestController *) || " +
          "within(@org.springframework.stereotype.Service *)" +
        ") && " +
        "!within(ru.forge.blacksmith_shop.common.audit..*) && " +   // исключаем сам аудит
        "!within(ru.forge.blacksmith_shop.users.repo..*)")          // и репозитории пользователей/ролей
  public Object setUserIdAround(ProceedingJoinPoint pjp) throws Throwable {
      Integer uid = resolveCurrentUserId();
      try {
          if (uid != null) {
              em.createNativeQuery("select set_config('app.user_id', cast(:uid as text), true)")
                      .setParameter("uid", uid)
                      .getSingleResult();
          } else {
              // БЫЛО: '0'  → из-за этого ловили FK-ошибку и она маскировалась как “дубликат”
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
                .map(u -> u.getUserId()) // если у тебя поле называется иначе — поправь
                .orElse(null);
    }
}

