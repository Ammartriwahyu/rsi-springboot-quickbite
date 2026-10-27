package quickbite.boundary;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import quickbite.boundary.dto.PesananResponse;
import quickbite.controller.RiwayatPesananController;

import java.util.List;

@RestController
@RequestMapping("/api/pesanan")
public class RiwayatPesananBoundary {

    private final RiwayatPesananController riwayatController;

    public RiwayatPesananBoundary(RiwayatPesananController riwayatController) {
        this.riwayatController = riwayatController;
    }

    @GetMapping
    public List<PesananResponse> daftarPesanan() {
        return riwayatController.daftarPesanan().stream()
                .map(p -> new PesananResponse(
                        p.idPesanan(), p.namaPelanggan(), p.tanggal(), p.total(), p.status()))
                .toList();
    }
}
