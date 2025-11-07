// src/main/java/ru/forge/blacksmith_shop/audit/web/AdminLogsController.java
package ru.forge.blacksmith_shop.audit.web;

import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.forge.blacksmith_shop.audit.repo.LogRow;
import ru.forge.blacksmith_shop.audit.repo.LogsReadRepository;

import java.time.*;

@Controller
@RequestMapping("/admin/logs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminLogsController {

    private final LogsReadRepository repo;

    public AdminLogsController(LogsReadRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "userId", required = false) Integer userId,
                       @RequestParam(value = "action", required = false) String action,
                       @RequestParam(value = "from", required = false) LocalDate from,
                       @RequestParam(value = "to", required = false) LocalDate to,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "20") int size,
                       Model model) {

        // UTC-границы: [from; to)
        Instant fromTs = (from == null) ? null
                : from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toTs = (to == null) ? null
                : to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt", "id"));

        Page<LogRow> p = repo.findRows(q, userId, action, fromTs, toTs, pageable);

        model.addAttribute("page", p);
        model.addAttribute("q", q);
        model.addAttribute("userId", userId);
        model.addAttribute("actionVal", action);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "admin/logs/list";
    }
}
