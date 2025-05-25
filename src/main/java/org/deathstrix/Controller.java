package org.deathstrix;


import org.springframework.web.bind.annotation.*;

@org.springframework.stereotype.Controller
@RequestMapping("/")
public class Controller {
    @GetMapping
    public String miniApp() {
        return "admin/index";
    }

}

