package quickbite.boundary;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import quickbite.boundary.dto.MenuResponse;
import quickbite.boundary.dto.PelangganResponse;
import quickbite.controller.KatalogController;

import java.util.List;

/** Boundary untuk data yang ditampilkan di halaman checkout. */
@RestController
@RequestMapping("/api")
public class KatalogBoundary {

    private final KatalogController katalogController;

    public KatalogBoundary(KatalogController katalogController) {
        this.katalogController = katalogController;
    }

    @GetMapping("/menu")
    public List<MenuResponse> daftarMenu() {
        return katalogController.daftarMenu().stream()
                .map(m -> new MenuResponse(m.idMenu(), m.namaMenu(), m.harga(), m.stok()))
                .toList();
    }

    @GetMapping("/pelanggan")
    public List<PelangganResponse> daftarPelanggan() {
        return katalogController.daftarPelanggan().stream()
                .map(p -> new PelangganResponse(p.idPelanggan(), p.nama()))
                .toList();
    }
}
