package quickbite.boundary;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import quickbite.boundary.dto.BatalTransaksiRequest;
import quickbite.boundary.dto.BatalTransaksiResponse;
import quickbite.boundary.dto.CheckoutRequest;
import quickbite.boundary.dto.CheckoutResponse;
import quickbite.controller.CheckoutController;
import quickbite.controller.dto.BatalTransaksiResult;
import quickbite.controller.dto.CheckoutResult;

/**
 * Boundary (CheckoutUI di DCD 8b). Hanya menangani HTTP, validasi input,
 * dan konversi DTO ↔ Command/Result. Tidak ada logika bisnis di sini.
 */
@RestController
@RequestMapping("/api/checkout")
public class CheckoutBoundary {

    private final CheckoutController checkoutController;

    public CheckoutBoundary(
            CheckoutController checkoutController) {
        this.checkoutController = checkoutController;
    }

    /** klikTombolCheckout(keranjangData) */
    @PostMapping
    public ResponseEntity<CheckoutResponse> checkout(
            @Valid @RequestBody CheckoutRequest request) {

        CheckoutResult hasil =
                checkoutController.prosesCheckout(
                        request.toCommand());
        return ResponseEntity.ok(new CheckoutResponse(
                hasil.idPesanan(), hasil.total()));
    }

    /** batalkanTransaksi(alasan) */
    @PostMapping("/{idPesanan}/batal")
    public ResponseEntity<BatalTransaksiResponse> batal(
            @PathVariable String idPesanan,
            @Valid @RequestBody BatalTransaksiRequest request) {

        BatalTransaksiResult hasil =
                checkoutController.batalkanTransaksi(
                        request.toCommand(idPesanan));
        return ResponseEntity.ok(new BatalTransaksiResponse(
                hasil.idPesanan(), hasil.status(), hasil.alasan()));
    }
}
