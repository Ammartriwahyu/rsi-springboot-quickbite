package quickbite.boundary;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import quickbite.controller.RiwayatPesananController;
import quickbite.controller.dto.PesananInfo;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RiwayatPesananBoundary.class)
class RiwayatPesananBoundaryTest {

    @Autowired MockMvc mvc;
    @MockitoBean RiwayatPesananController riwayatController;

    @Test
    void daftar_pesanan_mengembalikan_data_riwayat() throws Exception {
        given(riwayatController.daftarPesanan()).willReturn(List.of(
                new PesananInfo("ORD-1", "Andi Wijaya",
                        LocalDateTime.of(2026, 10, 27, 13, 30), 75000.0, "DIPROSES")));

        mvc.perform(get("/api/pesanan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idPesanan").value("ORD-1"))
                .andExpect(jsonPath("$[0].namaPelanggan").value("Andi Wijaya"))
                .andExpect(jsonPath("$[0].tanggal").value("2026-10-27T13:30:00"))
                .andExpect(jsonPath("$[0].total").value(75000.0))
                .andExpect(jsonPath("$[0].status").value("DIPROSES"));
    }
}
