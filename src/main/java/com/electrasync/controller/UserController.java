package com.electrasync.controller;

import com.electrasync.model.User;
import com.electrasync.service.EmployeeService;
import com.electrasync.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/owner/users")
public class UserController {

    private final UserService userService;
    private final EmployeeService employeeService;

    public UserController(UserService userService, EmployeeService employeeService) {
        this.userService = userService;
        this.employeeService = employeeService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "owner/users-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", User.Role.values());
        return "owner/user-form";
    }

    // Called from the Employees page when the owner clicks "Create Login" for a specific employee.
    // Pre-fills the name so the owner doesn't have to retype it.
    @GetMapping("/new-for-employee/{employeeId}")
    public String newFormForEmployee(@PathVariable Long employeeId, Model model, RedirectAttributes redirectAttributes) {
        var employee = employeeService.getById(employeeId).orElse(null);
        if (employee == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Employee not found.");
            return "redirect:/owner/employees";
        }

        User user = new User();
        user.setFullName(employee.getFullName());

        model.addAttribute("user", user);
        model.addAttribute("roles", User.Role.values());
        model.addAttribute("employeeId", employeeId);
        return "owner/user-form";
    }

    // Loads an existing login into the edit form
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        User user = userService.getById(id).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
            return "redirect:/owner/users";
        }
        model.addAttribute("user", user);
        model.addAttribute("roles", User.Role.values());
        return "owner/user-edit-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute User user,
                       @RequestParam String rawPassword,
                       @RequestParam(required = false) Long employeeId,
                       RedirectAttributes redirectAttributes) {

        if (rawPassword == null || rawPassword.trim().length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password must be at least 6 characters.");
            return "redirect:/owner/users/new";
        }

        if (userService.isUsernameTaken(user.getUsername())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Username '" + user.getUsername() + "' is already taken.");
            return "redirect:/owner/users/new";
        }

        if (userService.isEmailTaken(user.getEmail())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email '" + user.getEmail() + "' is already registered.");
            return "redirect:/owner/users/new";
        }

        User createdUser = userService.createUser(user, rawPassword);

        // If this login was created from the Employees page, link the two records together
        if (employeeId != null) {
            employeeService.linkUserAccount(employeeId, createdUser);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Login created for " + user.getFullName() + ".");
        return "redirect:/owner/users";
    }

    // Updates an existing login's name, email and role.
    // Password is only changed if the owner types a new one - leaving it blank keeps the current password.
    // Username is not editable since it's used to identify the account throughout the system.
    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam String fullName,
                         @RequestParam String email,
                         @RequestParam User.Role role,
                         @RequestParam(required = false) String newPassword,
                         RedirectAttributes redirectAttributes) {

        if (userService.isEmailTakenByAnotherUser(email, id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email '" + email + "' is already used by another account.");
            return "redirect:/owner/users/edit/" + id;
        }

        if (newPassword != null && !newPassword.trim().isEmpty() && newPassword.trim().length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "New password must be at least 6 characters.");
            return "redirect:/owner/users/edit/" + id;
        }

        try {
            userService.updateUser(id, fullName, email, role, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Login updated successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/owner/users";
    }

    @PostMapping("/disable/{id}")
    public String disable(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.disableUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "User account disabled.");
        return "redirect:/owner/users";
    }

    @PostMapping("/enable/{id}")
    public String enable(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.enableUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "User account enabled.");
        return "redirect:/owner/users";
    }
}
