package quickbite.controller;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quickbite.controller.dto.PesananInfo;
import quickbite.repository.PesananRepository;

import java.util.List;

/** Controller BCE untuk menampilkan riwayat pesanan (terbaru di atas). */
@Service
public class RiwayatPesananController {

    private final PesananRepository pesananRepo;

    public RiwayatPesananController(PesananRepository pesananRepo) {
        this.pesananRepo = pesananRepo;
    }

    @Transactional(readOnly = true)
    public List<PesananInfo> daftarPesanan() {
        return pesananRepo.findAll(Sort.by(Sort.Direction.DESC, "tanggal")).stream()
                .map(p -> new PesananInfo(
                        p.getIdPesanan(),
                        p.getPelanggan().getNama(),
                        p.getTanggal(),
                        p.getTotalBiaya(),
                        p.getStatus().name()))
                .toList();
    }
}
