package com.example.todoapp;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    private final TodoMapper todoMapper;

    public HomeController(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "ToDo");
        return "index";
    }

    @GetMapping("/todos")
    public String todos(Model model) {
        model.addAttribute("todos", todoMapper.findAll());
        return "todos";
    }

    @GetMapping("/todos/new")
    public String createForm(Model model) {
        model.addAttribute("todo", new Todo());
        return "create";
    }

    @PostMapping("/todos/confirm")
    public String createConfirm(@ModelAttribute Todo todo, Model model) {
        model.addAttribute("todo", todo);
        return "create-confirm";
    }

    @PostMapping("/todos/new")
    public String createRewrite(@ModelAttribute Todo todo, Model model) {
        model.addAttribute("todo", todo);
        return "create";
    }

    @PostMapping("/todos")
    public String create(@ModelAttribute Todo todo, RedirectAttributes redirectAttributes) {
        todoMapper.insert(todo);
        redirectAttributes.addFlashAttribute("message", "登録しました");
        return "redirect:/todos";
    }

    @GetMapping("/todos/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Todo todo = todoMapper.findById(id);
        if (todo == null) {
            redirectAttributes.addFlashAttribute("message", "見つかりませんでした");
            return "redirect:/todos";
        }
        model.addAttribute("todo", todo);
        return "edit";
    }

    @PostMapping("/todos/{id}/confirm")
    public String editConfirm(@PathVariable("id") Long id, @ModelAttribute Todo todo, Model model) {
        todo.setId(id);
        model.addAttribute("todo", todo);
        return "edit-confirm";
    }

    @PostMapping("/todos/{id}/edit")
    public String editRewrite(@PathVariable("id") Long id, @ModelAttribute Todo todo, Model model) {
        todo.setId(id);
        model.addAttribute("todo", todo);
        return "edit";
    }

    @PostMapping("/todos/{id}")
    public String edit(@PathVariable("id") Long id, @ModelAttribute Todo todo,
            RedirectAttributes redirectAttributes) {
        todo.setId(id);
        todoMapper.update(todo);
        redirectAttributes.addFlashAttribute("message", "保存しました");
        return "redirect:/todos";
    }

    @GetMapping("/todos/{id}/delete")
    public String deleteConfirm(@PathVariable("id") Long id, Model model,
            RedirectAttributes redirectAttributes) {
        Todo todo = todoMapper.findById(id);
        if (todo == null) {
            redirectAttributes.addFlashAttribute("message", "見つかりませんでした");
            return "redirect:/todos";
        }
        model.addAttribute("todo", todo);
        return "delete";
    }

    @PostMapping("/todos/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        todoMapper.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "削除しました");
        return "redirect:/todos";
    }
}
