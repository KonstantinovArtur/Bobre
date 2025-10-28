package ru.forge.blacksmith_shop.users.web;

import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.forge.blacksmith_shop.users.domain.User;
import ru.forge.blacksmith_shop.users.dto.PasswordChangeForm;
import ru.forge.blacksmith_shop.users.dto.ProfileForm;
import ru.forge.blacksmith_shop.users.repo.UserRepository;
import ru.forge.blacksmith_shop.users.service.ProfileService;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final UserRepository users;
    private final UserDetailsService userDetailsService;

    public ProfileController(ProfileService profileService,
                             UserRepository users,
                             UserDetailsService userDetailsService) {
        this.profileService = profileService;
        this.users = users;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping
    public String page(Model model, Authentication auth) {
        User u = users.findByLogin(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));
        if (!model.containsAttribute("profileForm")) {
            model.addAttribute("profileForm", new ProfileForm(u.getLogin(), u.getName()));
        }
        if (!model.containsAttribute("pwdForm")) {
            model.addAttribute("pwdForm", new PasswordChangeForm());
        }
        model.addAttribute("email", u.getEmail());
        return "users/profile";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute("profileForm") @Valid ProfileForm form,
                                BindingResult br,
                                Authentication auth,
                                RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.profileForm", br);
            ra.addFlashAttribute("profileForm", form);
            return "redirect:/profile";
        }

        User u = users.findByLogin(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));

        try {
            // Обновляем данные пользователя
            profileService.updateProfile(u.getUserId(), form);

            // 🔄 После смены логина обновляем SecurityContext,
            // чтобы текущая сессия использовала новый username
            UserDetails fresh = userDetailsService.loadUserByUsername(form.getLogin());
            UsernamePasswordAuthenticationToken newAuth =
                    new UsernamePasswordAuthenticationToken(
                            fresh, auth.getCredentials(), fresh.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            ra.addFlashAttribute("ok", "Профиль обновлён");
        } catch (IllegalArgumentException ex) {
            br.rejectValue("login", "dup", ex.getMessage());
            ra.addFlashAttribute("org.springframework.validation.BindingResult.profileForm", br);
            ra.addFlashAttribute("profileForm", form);
        }

        return "redirect:/profile";
    }

    @PostMapping("/password")
    public String changePassword(@ModelAttribute("pwdForm") @Valid PasswordChangeForm form,
                                 BindingResult br,
                                 Authentication auth,
                                 RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.pwdForm", br);
            ra.addFlashAttribute("pwdForm", form);
            return "redirect:/profile";
        }

        User u = users.findByLogin(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));

        try {
            profileService.changePassword(u.getUserId(), form);
            ra.addFlashAttribute("okPwd", "Пароль успешно изменён");
        } catch (IllegalArgumentException ex) {
            br.reject("pwdError", ex.getMessage());
            ra.addFlashAttribute("org.springframework.validation.BindingResult.pwdForm", br);
            ra.addFlashAttribute("pwdForm", form);
        }

        return "redirect:/profile";
    }
}
