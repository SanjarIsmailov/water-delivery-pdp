package com.example.demo.controller;

import com.example.demo.entity.Courier;
import com.example.demo.repo.CourierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/courier")
public class CourierCrudController {
    private final CourierRepository courierRepository;

    @GetMapping("/")
    public String getCouriers(Model model) {
        model.addAttribute("couriers", courierRepository.findAll());
        model.addAttribute("courier", new Courier()); // Add an empty Courier object for the form
        return "courier";
    }

    @PostMapping("/add")
    public String addCourier(@ModelAttribute Courier courier) {
        courierRepository.save(courier);
        return "redirect:/courier/";
    }

    @PostMapping("/delete")
    public String deleteCourier(@RequestParam UUID id) {
        courierRepository.deleteById(id);
        return "redirect:/courier/";
    }
    @GetMapping("/edit")
    public String editCourier(@RequestParam UUID id, Model model) {
        Courier courier = courierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid courier ID"));
        model.addAttribute("courier", courier);
        return "courier";
    }

    @PostMapping("/update")
    public String updateCourier(@ModelAttribute Courier courier) {
        courierRepository.save(courier);
        return "redirect:/courier/";
    }
}
