package ru.forge.blacksmith_shop.order.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.forge.blacksmith_shop.order.repo.OrderAdminRepository;

@Controller
@RequestMapping("/manager/orders")
public class ManagerOrdersController {

    private final OrderAdminRepository orderAdminRepository;

    @Autowired
    public ManagerOrdersController(OrderAdminRepository orderAdminRepository) {
        this.orderAdminRepository = orderAdminRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("orders", orderAdminRepository.findAll());
        return "manager/orders/list";
    }

    /** Меняет статус с 'Оформлен' → 'В пункте выдачи' */
    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable int id, RedirectAttributes ra) {
        int updated = orderAdminRepository.markInTransitIfCreated(id);
        if (updated > 0) {
            ra.addFlashAttribute("ok", "Статус заказа #" + id + " изменён: «В пункте выдачи».");
        } else {
            ra.addFlashAttribute("ok", "Статус не изменён (возможно, заказ уже не «Оформлен»).");
        }
        return "redirect:/manager/orders";
    }

    /** Дополнительно: смена 'В пункте выдачи' → 'Получен' */
    @PostMapping("/{id}/receive")
    public String receive(@PathVariable int id, RedirectAttributes ra) {
        int updated = orderAdminRepository.markReceivedIfInTransit(id);
        if (updated > 0) {
            ra.addFlashAttribute("ok", "Заказ #" + id + " отмечен как «Получен».");
        } else {
            ra.addFlashAttribute("ok", "Не удалось отметить заказ как «Получен» (не тот статус).");
        }
        return "redirect:/manager/orders";
    }
}
