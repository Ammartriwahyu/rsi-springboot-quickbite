package quickbite.boundary;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import quickbite.controller.CheckoutController;
import quickbite.controller.dto.BatalTransaksiResult;
import quickbite.controller.dto.CheckoutResult;
import quickbite.controller.exception.PaymentFailedException;
import quickbite.controller.exception.PesananTidakBisaDibatalkanException;
import quickbite.controller.exception.ResourceNotFoundException;
import quickbite.controller.exception.StokTidakCukupException;
import quickbite.entity.StatusPesanan;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Uji kontrak HTTP tanpa database: Controller di-mock (slide 31). */
@WebMvcTest(CheckoutBoundary.class)
class CheckoutBoundaryTest {

    private static final String BODY_VALID = """
            {"idPelanggan":"P-01",
             "tokenKartu":"tok_123",
             "keranjang":[
               {"idMenu":"M-01","qty":2}]}
            """;

    @Autowired MockMvc mvc;
    @MockitoBean CheckoutController checkoutController;

    @Test
    void checkout_berhasil_mengembalikan_id_dan_total() throws Exception {
        given(checkoutController.prosesCheckout(any()))
                .willReturn(new CheckoutResult("P-01", 50000.0));

        mvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPesanan").value("P-01"))
                .andExpect(jsonPath("$.total").value(50000.0));
    }

    @Test
    void checkout_stok_habis_mengembalikan_409() throws Exception {
        given(checkoutController.prosesCheckout(any()))
                .willThrow(new StokTidakCukupException("M-01"));

        mvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("STOK_HABIS"));
    }

    @Test
    void checkout_data_tidak_ada_mengembalikan_404() throws Exception {
        given(checkoutController.prosesCheckout(any()))
                .willThrow(new ResourceNotFoundException("Menu M-99"));

        mvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Menu M-99 tidak ditemukan"));
    }

    @Test
    void checkout_gateway_gagal_mengembalikan_502() throws Exception {
        given(checkoutController.prosesCheckout(any()))
                .willThrow(new PaymentFailedException("Gateway menolak transaksi"));

        mvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALID))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("PAYMENT_FAILED"));
    }

    @Test
    void checkout_input_tidak_valid_mengembalikan_400() throws Exception {
        mvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idPelanggan":"",
                                 "tokenKartu":"tok_123",
                                 "keranjang":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDASI_GAGAL"))
                .andExpect(jsonPath("$.errors.idPelanggan").exists())
                .andExpect(jsonPath("$.errors.keranjang").exists());
    }

    @Test
    void checkout_qty_nol_mengembalikan_400() throws Exception {
        mvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idPelanggan":"P-01",
                                 "tokenKartu":"tok_123",
                                 "keranjang":[{"idMenu":"M-01","qty":0}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDASI_GAGAL"));
    }

    @Test
    void checkout_json_rusak_mengembalikan_400() throws Exception {
        mvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{bukan json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("FORMAT_TIDAK_VALID"));
    }

    @Test
    void batal_berhasil_mengembalikan_200() throws Exception {
        given(checkoutController.batalkanTransaksi(any()))
                .willReturn(new BatalTransaksiResult("ORD-1", "BATAL", "ganti menu"));

        mvc.perform(post("/api/checkout/ORD-1/batal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alasan\":\"ganti menu\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPesanan").value("ORD-1"))
                .andExpect(jsonPath("$.status").value("BATAL"));
    }

    @Test
    void batal_pesanan_sudah_dikirim_mengembalikan_409() throws Exception {
        given(checkoutController.batalkanTransaksi(any()))
                .willThrow(new PesananTidakBisaDibatalkanException("ORD-1", StatusPesanan.DIKIRIM));

        mvc.perform(post("/api/checkout/ORD-1/batal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alasan\":\"ganti menu\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TIDAK_BISA_DIBATALKAN"));
    }

    @Test
    void batal_tanpa_alasan_mengembalikan_400() throws Exception {
        mvc.perform(post("/api/checkout/ORD-1/batal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alasan\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.alasan").exists());
    }
}
