package ru.forge.blacksmith_shop.users.web;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.forge.blacksmith_shop.users.domain.Role;
import ru.forge.blacksmith_shop.users.repo.RoleRepository;
import ru.forge.blacksmith_shop.users.service.RoleInUseException;
import ru.forge.blacksmith_shop.users.service.RoleService;

@Controller
@RequestMapping("/admin/roles")
public class AdminRoleController {

    private final RoleRepository roles;
    private final RoleService roleService;

    public AdminRoleController(RoleRepository roles, RoleService roleService) {
        this.roles = roles;
        this.roleService = roleService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("roles", roles.findAll());
        return "admin/roles";
    }

    @PostMapping("/create")
    @Transactional
    public String create(@RequestParam("name") String name, RedirectAttributes ra) {
        String n = normalize(name);
        if (n.isEmpty()) { ra.addFlashAttribute("err", "Название роли не может быть пустым"); return "redirect:/admin/roles"; }
        if (n.length() < 3 || n.length() > 20) { ra.addFlashAttribute("err", "Название роли должно быть от 3 до 20 символов"); return "redirect:/admin/roles"; }
        if (roles.existsByRoleNameIgnoreCase(n)) { ra.addFlashAttribute("err", "Такая роль уже существует: " + n); return "redirect:/admin/roles"; }

        try {
            Role r = new Role();
            r.setRoleName(n);
            roles.save(r); // flush не обязателен
            ra.addFlashAttribute("ok", "Роль создана");
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("err", "Такая роль уже существует: " + n);
        }
        return "redirect:/admin/roles";
    }

    @PostMapping("/{roleId}/rename")
    @Transactional
    public String rename(@PathVariable Integer roleId,
                         @RequestParam("name") String name,
                         RedirectAttributes ra) {

        String newName = normalize(name);
        if (newName.isEmpty()) { ra.addFlashAttribute("err", "Название роли не может быть пустым"); return "redirect:/admin/roles"; }
        if (newName.length() < 3 || newName.length() > 20) { ra.addFlashAttribute("err", "Название роли должно быть от 3 до 20 символов"); return "redirect:/admin/roles"; }

        Role role = roles.findById(roleId).orElse(null);
        if (role == null) { ra.addFlashAttribute("err", "Роль не найдена"); return "redirect:/admin/roles"; }

        if (role.getRoleName().equalsIgnoreCase(newName)) {
            ra.addFlashAttribute("ok", "Имя роли не изменилось");
            return "redirect:/admin/roles";
        }

        if (roles.existsByRoleNameIgnoreCaseAndRoleIdNot(newName, roleId)) {
            ra.addFlashAttribute("err", "Нельзя переименовать: роль с таким именем уже существует");
            return "redirect:/admin/roles";
        }

        try {
            role.setRoleName(newName);
            roles.save(role);
            ra.addFlashAttribute("ok", "Роль переименована");
        } catch (DataIntegrityViolationException ex) {
            ra.addFlashAttribute("err", "Ошибка: роль с таким именем уже существует");
        }

        return "redirect:/admin/roles";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            roleService.deleteById(id);
            ra.addFlashAttribute("ok", "Роль удалена.");
        } catch (RoleInUseException e) {
            ra.addFlashAttribute("error",
                    "Нельзя удалить роль: к ней привязано " + e.getCount()
                            + " пользовател(ей). Сначала переназначьте роли этим пользователям.");
        } catch (EntityNotFoundException e) {
            ra.addFlashAttribute("error", "Роль не найдена.");
        }
        return "redirect:/admin/roles";
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s+", " ");
    }
}
