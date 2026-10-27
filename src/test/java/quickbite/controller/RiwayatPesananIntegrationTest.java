package quickbite.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pesanan hasil checkout tersimpan dan tampil di riwayat, termasuk setelah dibatalkan. */
@SpringBootTest
@AutoConfigureMockMvc
class RiwayatPesananIntegrationTest {

    @Autowired MockMvc mvc;

    @Test
    void pesanan_muncul_di_riwayat_dan_statusnya_ikut_berubah() throws Exception {
        String json = mvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idPelanggan":"CUST-001","tokenKartu":"tok_123",
                                 "keranjang":[{"idMenu":"MENU-01","qty":1}]}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String idPesanan = JsonPath.read(json, "$.idPesanan");
        String filter = "$[?(@.idPesanan == '" + idPesanan + "')]";

        mvc.perform(get("/api/pesanan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(filter + ".namaPelanggan").value("Budi Santoso"))
                .andExpect(jsonPath(filter + ".total").value(18000.0))
                .andExpect(jsonPath(filter + ".status").value("DIPROSES"));

        mvc.perform(post("/api/checkout/" + idPesanan + "/batal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alasan\":\"tes riwayat\"}"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/pesanan"))
                .andExpect(jsonPath(filter + ".status").value("BATAL"));
    }
}
