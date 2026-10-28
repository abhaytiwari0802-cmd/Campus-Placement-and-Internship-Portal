package com.portal.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class CareerPrepControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CareerPrepController()).build();
    }

    @Test
    @DisplayName("GET /career-prep should return index hub")
    void testHub() throws Exception {
        mockMvc.perform(get("/career-prep"))
                .andExpect(status().isOk())
                .andExpect(view().name("career-prep/index"));
    }

    @Test
    @DisplayName("GET /career-prep/aptitude should return aptitude view")
    void testAptitude() throws Exception {
        mockMvc.perform(get("/career-prep/aptitude"))
                .andExpect(status().isOk())
                .andExpect(view().name("career-prep/aptitude"));
    }

    @Test
    @DisplayName("GET /career-prep/technical-java should return java view")
    void testTechnicalJava() throws Exception {
        mockMvc.perform(get("/career-prep/technical-java"))
                .andExpect(status().isOk())
                .andExpect(view().name("career-prep/technical-java"));
    }

    @Test
    @DisplayName("GET /career-prep/technical-dbms should return dbms view")
    void testTechnicalDbms() throws Exception {
        mockMvc.perform(get("/career-prep/technical-dbms"))
                .andExpect(status().isOk())
                .andExpect(view().name("career-prep/technical-dbms"));
    }

    @Test
    @DisplayName("GET /career-prep/dsa should return dsa view")
    void testDsa() throws Exception {
        mockMvc.perform(get("/career-prep/dsa"))
                .andExpect(status().isOk())
                .andExpect(view().name("career-prep/dsa"));
    }

    @Test
    @DisplayName("GET /career-prep/hr-interview should return hr interview view")
    void testHrInterview() throws Exception {
        mockMvc.perform(get("/career-prep/hr-interview"))
                .andExpect(status().isOk())
                .andExpect(view().name("career-prep/hr-interview"));
    }

    @Test
    @DisplayName("GET /career-prep/weekly-plan should return weekly plan view")
    void testWeeklyPlan() throws Exception {
        mockMvc.perform(get("/career-prep/weekly-plan"))
                .andExpect(status().isOk())
                .andExpect(view().name("career-prep/weekly-plan"));
    }
}
