package ru.forge.blacksmith_shop.users.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.forge.blacksmith_shop.users.domain.Role;
import ru.forge.blacksmith_shop.users.repo.RoleRepository;

@Controller
@RequestMapping("/admin/roles")
public class AdminRoleController {

    private final RoleRepository roles;

    // ✅ обычный конструктор вместо Lombok
    public AdminRoleController(RoleRepository roles) {
        this.roles = roles;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("roles", roles.findAll());
        return "admin/roles";
    }

    @PostMapping("/create")
    public String create(@RequestParam("name") String name,
                         RedirectAttributes ra) {
        Role r = new Role();
        r.setRoleName(name);
        roles.save(r);
        ra.addFlashAttribute("ok", "Роль создана");
        return "redirect:/admin/roles";
    }

    @PostMapping("/{roleId}/rename")
    public String rename(@PathVariable Integer roleId,
                         @RequestParam("name") String name,
                         RedirectAttributes ra) {
        Role r = roles.findById(roleId).orElseThrow();
        r.setRoleName(name);
        roles.save(r);
        ra.addFlashAttribute("ok", "Роль переименована");
        return "redirect:/admin/roles";
    }

    @PostMapping("/{roleId}/delete")
    public String delete(@PathVariable Integer roleId,
                         RedirectAttributes ra) {
        roles.deleteById(roleId);
        ra.addFlashAttribute("ok", "Роль удалена");
        return "redirect:/admin/roles";
    }
}
