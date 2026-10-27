package quickbite.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Endpoint katalog membaca data dari V2__seed.sql, dan halaman frontend tersaji di "/". */
@SpringBootTest
@AutoConfigureMockMvc
class KatalogIntegrationTest {

    @Autowired MockMvc mvc;

    @Test
    void daftar_menu_dan_pelanggan_dari_database() throws Exception {
        mvc.perform(get("/api/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].idMenu", hasItems("M-01", "M-02", "MENU-01", "MENU-02")));

        mvc.perform(get("/api/pelanggan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].idPelanggan", hasItems("P-01", "CUST-001")));
    }

    @Test
    void halaman_frontend_tersedia() throws Exception {
        mvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("QuickBite")));
    }
}
