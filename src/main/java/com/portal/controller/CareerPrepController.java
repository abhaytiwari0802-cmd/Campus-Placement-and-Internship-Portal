package com.portal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/career-prep")
public class CareerPrepController {

    @GetMapping
    public String hub() {
        return "career-prep/index";
    }

    @GetMapping("/aptitude")
    public String aptitude() {
        return "career-prep/aptitude";
    }

    @GetMapping("/technical-java")
    public String technicalJava() {
        return "career-prep/technical-java";
    }

    @GetMapping("/technical-dbms")
    public String technicalDbms() {
        return "career-prep/technical-dbms";
    }

    @GetMapping("/dsa")
    public String dsa() {
        return "career-prep/dsa";
    }

    @GetMapping("/hr-interview")
    public String hrInterview() {
        return "career-prep/hr-interview";
    }

    @GetMapping("/weekly-plan")
    public String weeklyPlan() {
        return "career-prep/weekly-plan";
    }
}
