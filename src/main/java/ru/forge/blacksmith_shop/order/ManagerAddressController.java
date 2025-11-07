// src/main/java/ru/forge/blacksmith_shop/order/web/ManagerAddressController.java
package ru.forge.blacksmith_shop.order; // <- поправил пакет на .../web/ для единообразия

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.forge.blacksmith_shop.order.domain.OrderAddress;
import ru.forge.blacksmith_shop.order.repo.OrderAddressRepository;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
@RequestMapping("/manager/addresses")
public class ManagerAddressController {

    private final OrderAddressRepository orderAddressRepository;

    public ManagerAddressController(OrderAddressRepository orderAddressRepository) {
        this.orderAddressRepository = orderAddressRepository;
    }

    // LIST
    @GetMapping
    public String list(Model model, @RequestParam(value = "ok", required = false) String ok) {
        model.addAttribute("items", orderAddressRepository.findAll());
        model.addAttribute("ok", ok);
        return "manager/addresses/list";
    }

    // CREATE FORM
    @GetMapping("/new")
    public String createForm(Model model) {
        OrderAddress a = new OrderAddress();
        a.setShippingCost(BigDecimal.ZERO);
        model.addAttribute("form", a);
        model.addAttribute("isEdit", false);
        return "manager/addresses/form";
    }

    // CREATE
    @PostMapping("/create")
    @Transactional
    public String create(@Valid @ModelAttribute("form") OrderAddress form,
                         BindingResult br,
                         Model model,
                         RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "manager/addresses/form";
        }
        normalize(form);
        orderAddressRepository.save(form);
        ra.addAttribute("ok", "Адрес добавлен");
        return "redirect:/manager/addresses";
    }

    // EDIT FORM
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        Optional<OrderAddress> opt = orderAddressRepository.findById(id);
        if (opt.isEmpty()) {
            ra.addAttribute("ok", "Адрес не найден");
            return "redirect:/manager/addresses";
        }
        model.addAttribute("form", opt.get());
        model.addAttribute("isEdit", true);
        return "manager/addresses/form";
    }

    // UPDATE
    @PostMapping("/{id}/update")
    @Transactional
    public String update(@PathVariable Integer id,
                         @Valid @ModelAttribute("form") OrderAddress form,
                         BindingResult br,
                         Model model,
                         RedirectAttributes ra) {
        // Сохраняем id в форме, чтобы th:action и заголовок работали при ошибках
        form.setId(id);

        if (br.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "manager/addresses/form";
        }

        OrderAddress existing = orderAddressRepository.findById(id).orElse(null);
        if (existing == null) {
            ra.addAttribute("ok", "Адрес не найден");
            return "redirect:/manager/addresses";
        }

        // Только после успешной валидации переносим данные
        existing.setAddressLine(nz(form.getAddressLine()));
        existing.setShippingCost(form.getShippingCost() == null ? BigDecimal.ZERO : form.getShippingCost());
        // orderId менеджерскому справочнику не нужен — не трогаем
        orderAddressRepository.save(existing);

        ra.addAttribute("ok", "Изменения сохранены");
        return "redirect:/manager/addresses";
    }

    // DELETE
    @PostMapping("/{id}/delete")
    @Transactional
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        if (orderAddressRepository.existsById(id)) {
            orderAddressRepository.deleteById(id);
            ra.addAttribute("ok", "Адрес удалён");
        } else {
            ra.addAttribute("ok", "Адрес не найден");
        }
        return "redirect:/manager/addresses";
    }

    // helpers
    private static String nz(String s) { return (s == null) ? "" : s.trim(); }

    private static void normalize(OrderAddress a) {
        a.setAddressLine(nz(a.getAddressLine()));
        if (a.getShippingCost() == null) a.setShippingCost(BigDecimal.ZERO);
        // orderId оставляем null
    }
}
