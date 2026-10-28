package com.portal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CampusPortalApplicationTests {

    @Test
    @DisplayName("Application class should be instantiable")
    void contextLoads() {
        CampusPortalApplication app = new CampusPortalApplication();
        assertNotNull(app);
    }
}
