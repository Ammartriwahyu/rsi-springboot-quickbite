package quickbite.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import quickbite.entity.StatusPesanan;
import quickbite.repository.MenuRepository;
import quickbite.repository.PesananRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CheckoutFlowIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired MenuRepository menuRepo;
    @Autowired PesananRepository pesananRepo;

    @Test
    void checkout_sukses_memotong_stok_dan_menyimpan_pesanan() throws Exception {
        int stokM01 = stok("M-01");
        int stokM02 = stok("M-02");

        String json = checkout("P-01", "tok_123", item("M-01", 2), item("M-02", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(75000.0))
                .andReturn().getResponse().getContentAsString();

        String idPesanan = JsonPath.read(json, "$.idPesanan");
        assertThat(pesananRepo.findById(idPesanan)).hasValueSatisfying(p -> {
            assertThat(p.getStatus()).isEqualTo(StatusPesanan.DIPROSES);
            assertThat(p.getTotalBiaya()).isEqualTo(75000.0);
        });
        assertThat(stok("M-01")).isEqualTo(stokM01 - 2);
        assertThat(stok("M-02")).isEqualTo(stokM02 - 1);
    }

    @Test
    void contoh_modul_total_61000() throws Exception {
        checkout("CUST-001", "tok_visa_valid_999", item("MENU-01", 2), item("MENU-02", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(61000.0));
    }

    @Test
    void gateway_menolak_maka_semua_dirollback() throws Exception {
        int stokSebelum = stok("M-01");
        long pesananSebelum = pesananRepo.count();

        checkout("P-01", "invalid_card", item("M-01", 1))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("PAYMENT_FAILED"));

        assertThat(stok("M-01")).isEqualTo(stokSebelum);
        assertThat(pesananRepo.count()).isEqualTo(pesananSebelum);
    }

    @Test
    void stok_tidak_cukup_mengembalikan_409_tanpa_mengubah_stok() throws Exception {
        int stokSebelum = stok("MENU-02");

        checkout("CUST-001", "tok_123", item("MENU-02", 100))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("STOK_HABIS"));

        assertThat(stok("MENU-02")).isEqualTo(stokSebelum);
    }

    @Test
    void menu_sama_dua_kali_dijumlahkan_saat_cek_stok() throws Exception {
        int stokSekarang = stok("MENU-02");
        int separuhLebih = stokSekarang / 2 + 1;

        checkout("CUST-001", "tok_123", item("MENU-02", separuhLebih), item("MENU-02", separuhLebih))
                .andExpect(status().isConflict());

        assertThat(stok("MENU-02")).isEqualTo(stokSekarang);
    }

    @Test
    void pelanggan_atau_menu_tidak_ada_mengembalikan_404() throws Exception {
        checkout("P-99", "tok_123", item("M-01", 1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Pelanggan P-99 tidak ditemukan"));

        checkout("P-01", "tok_123", item("M-99", 1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Menu M-99 tidak ditemukan"));
    }

    @Test
    void batal_mengembalikan_stok_dan_tidak_bisa_diulang() throws Exception {
        int stokAwal = stok("M-01");
        String json = checkout("P-01", "tok_123", item("M-01", 3))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String idPesanan = JsonPath.read(json, "$.idPesanan");
        assertThat(stok("M-01")).isEqualTo(stokAwal - 3);

        batal(idPesanan)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BATAL"));
        assertThat(stok("M-01")).isEqualTo(stokAwal);
        assertThat(pesananRepo.findById(idPesanan).orElseThrow().getStatus())
                .isEqualTo(StatusPesanan.BATAL);

        batal(idPesanan)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TIDAK_BISA_DIBATALKAN"));
        assertThat(stok("M-01")).isEqualTo(stokAwal);
    }

    @Test
    void batal_pesanan_tidak_ada_mengembalikan_404() throws Exception {
        batal("ORD-TIDAK-ADA").andExpect(status().isNotFound());
    }

    private int stok(String idMenu) {
        return menuRepo.findById(idMenu).orElseThrow().getStok();
    }

    private static String item(String idMenu, int qty) {
        return "{\"idMenu\":\"" + idMenu + "\",\"qty\":" + qty + "}";
    }

    private ResultActions checkout(String idPelanggan, String token, String... items) throws Exception {
        String body = "{\"idPelanggan\":\"" + idPelanggan + "\",\"tokenKartu\":\"" + token
                + "\",\"keranjang\":[" + String.join(",", items) + "]}";
        return mvc.perform(post("/api/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private ResultActions batal(String idPesanan) throws Exception {
        return mvc.perform(post("/api/checkout/" + idPesanan + "/batal")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"alasan\":\"Pelanggan ingin mengganti menu\"}"));
    }
}
