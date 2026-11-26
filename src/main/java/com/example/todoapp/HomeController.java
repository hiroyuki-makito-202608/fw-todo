package com.example.todoapp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

@Controller
public class HomeController {

    private final ConcurrentHashMap<String, FileStorageService.StoredFile> pendingAttachments = new ConcurrentHashMap<>();

    private final TodoService todoService;
    private final TodoAttachmentMapper attachmentMapper;
    private final FileStorageService fileStorageService;

    public HomeController(TodoService todoService, TodoAttachmentMapper attachmentMapper,
            FileStorageService fileStorageService) {
        this.todoService = todoService;
        this.attachmentMapper = attachmentMapper;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "ToDo");
        return "index";
    }

    @GetMapping("/todos")
    public String todos(@RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "asc") String order, Model model) {
        if (!"desc".equals(order)) {
            order = "asc";
        }
        model.addAttribute("todos", todoService.search(keyword, category, order));
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("order", order);
        return "todos";
    }

    @GetMapping("/todos/new")
    public String createForm(Model model) {
        model.addAttribute("todo", new Todo());
        return "create";
    }

    @PostMapping("/todos/confirm")
    public String createConfirm(@Valid @ModelAttribute Todo todo, BindingResult bindingResult,
            @RequestParam(name = "attachment", required = false) MultipartFile attachment, Model model) {
        model.addAttribute("todo", todo);
        if (bindingResult.hasErrors()) {
            return "create";
        }
        if (attachment != null && !attachment.isEmpty()) {
            try {
                var stored = fileStorageService.store(attachment);
                String token = java.util.UUID.randomUUID().toString();
                pendingAttachments.put(token, stored);
                model.addAttribute("attachmentName", stored.originalFileName());
                model.addAttribute("attachmentToken", token);
            } catch (IOException e) {
                throw new IllegalStateException("添付ファイルを一時保存できません。", e);
            }
        }
        return "create-confirm";
    }

    @PostMapping("/todos/new")
    public String createRewrite(@ModelAttribute Todo todo, Model model) {
        model.addAttribute("todo", todo);
        return "create";
    }

    @PostMapping("/todos")
    public String create(@ModelAttribute Todo todo,
            @RequestParam(name = "attachmentToken", required = false) String attachmentToken,
            RedirectAttributes redirectAttributes) throws IOException {
        todoService.create(todo);
        FileStorageService.StoredFile pending = attachmentToken == null ? null : pendingAttachments.remove(attachmentToken);
        if (pending != null) {
            TodoAttachment attachment = new TodoAttachment();
            attachment.setTodoId(todo.getId());
            attachment.setOriginalFileName(pending.originalFileName());
            attachment.setStoredFileName(pending.storedFileName());
            attachment.setContentType(pending.contentType());
            attachment.setFileSize(pending.size());
            try {
                attachmentMapper.insert(attachment);
            } catch (RuntimeException e) {
                fileStorageService.delete(pending.storedFileName());
                throw e;
            }
        }
        redirectAttributes.addFlashAttribute("message", "登録しました");
        return "redirect:/todos";
    }

    @GetMapping("/todos/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            redirectAttributes.addFlashAttribute("message", "見つかりませんでした");
            return "redirect:/todos";
        }
        model.addAttribute("todo", todo);
        model.addAttribute("attachments", attachmentMapper.findByTodoId(id));
        return "edit";
    }

    @PostMapping("/todos/{id}/confirm")
    public String editConfirm(@PathVariable("id") Long id, @Valid @ModelAttribute Todo todo,
            BindingResult bindingResult, Model model) {
        todo.setId(id);
        model.addAttribute("todo", todo);
        model.addAttribute("attachments", attachmentMapper.findByTodoId(id));
        if (bindingResult.hasErrors()) {
            return "edit";
        }
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
        todoService.update(todo);
        redirectAttributes.addFlashAttribute("message", "保存しました");
        return "redirect:/todos";
    }

    @GetMapping("/todos/{id}/delete")
    public String deleteConfirm(@PathVariable("id") Long id, Model model,
            RedirectAttributes redirectAttributes) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            redirectAttributes.addFlashAttribute("message", "見つかりませんでした");
            return "redirect:/todos";
        }
        model.addAttribute("todo", todo);
        return "delete";
    }

    @PostMapping("/todos/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        for (TodoAttachment attachment : attachmentMapper.findByTodoId(id)) {
            try { fileStorageService.delete(attachment.getStoredFileName()); } catch (IOException ignored) { }
        }
        attachmentMapper.deleteByTodoId(id);
        todoService.delete(id);
        redirectAttributes.addFlashAttribute("message", "削除しました");
        return "redirect:/todos";
    }

    @PostMapping("/todos/{id}/attachments")
    public String uploadAttachment(@PathVariable Long id, @RequestParam("attachment") MultipartFile file,
            RedirectAttributes redirectAttributes) throws IOException {
        if (todoService.findById(id) == null) return "redirect:/todos";
        saveAttachment(id, file);
        redirectAttributes.addFlashAttribute("message", "添付ファイルを登録しました。");
        return "redirect:/todos/{id}/edit";
    }

    private void saveAttachment(Long todoId, MultipartFile file) throws IOException {
        FileStorageService.StoredFile stored = fileStorageService.store(file);
        TodoAttachment attachment = new TodoAttachment();
        attachment.setTodoId(todoId);
        attachment.setOriginalFileName(stored.originalFileName());
        attachment.setStoredFileName(stored.storedFileName());
        attachment.setContentType(stored.contentType());
        attachment.setFileSize(stored.size());
        try {
            attachmentMapper.insert(attachment);
        } catch (RuntimeException e) {
            fileStorageService.delete(stored.storedFileName());
            throw e;
        }
    }

    @GetMapping("/attachments/{id}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long id) throws IOException {
        TodoAttachment attachment = attachmentMapper.findById(id);
        if (attachment == null) return ResponseEntity.notFound().build();
        Resource resource = fileStorageService.load(attachment.getStoredFileName());
        String contentType = attachment.getContentType() == null ? "application/octet-stream" : attachment.getContentType();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(attachment.getOriginalFileName(), StandardCharsets.UTF_8).build().toString())
                .body(resource);
    }

    @PostMapping("/attachments/{id}/delete")
    public String deleteAttachment(@PathVariable Long id, @RequestParam Long todoId,
            RedirectAttributes redirectAttributes) throws IOException {
        TodoAttachment attachment = attachmentMapper.findById(id);
        if (attachment != null && attachment.getTodoId().equals(todoId)) {
            attachmentMapper.deleteById(id);
            fileStorageService.delete(attachment.getStoredFileName());
        }
        redirectAttributes.addFlashAttribute("message", "添付ファイルを削除しました。");
        return "redirect:/todos/{todoId}/edit";
    }

}
