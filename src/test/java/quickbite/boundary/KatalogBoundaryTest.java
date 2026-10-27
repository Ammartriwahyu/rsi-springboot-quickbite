package quickbite.boundary;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import quickbite.controller.KatalogController;
import quickbite.controller.dto.MenuInfo;
import quickbite.controller.dto.PelangganInfo;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KatalogBoundary.class)
class KatalogBoundaryTest {

    @Autowired MockMvc mvc;
    @MockitoBean KatalogController katalogController;

    @Test
    void daftar_menu_mengembalikan_nama_harga_dan_stok() throws Exception {
        given(katalogController.daftarMenu())
                .willReturn(List.of(new MenuInfo("M-01", "Nasi Goreng", 25000.0, 50)));

        mvc.perform(get("/api/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idMenu").value("M-01"))
                .andExpect(jsonPath("$[0].namaMenu").value("Nasi Goreng"))
                .andExpect(jsonPath("$[0].harga").value(25000.0))
                .andExpect(jsonPath("$[0].stok").value(50));
    }

    @Test
    void daftar_pelanggan_mengembalikan_id_dan_nama() throws Exception {
        given(katalogController.daftarPelanggan())
                .willReturn(List.of(new PelangganInfo("P-01", "Andi Wijaya")));

        mvc.perform(get("/api/pelanggan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idPelanggan").value("P-01"))
                .andExpect(jsonPath("$[0].nama").value("Andi Wijaya"));
    }
}
