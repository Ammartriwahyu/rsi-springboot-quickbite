package quickbite.controller;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quickbite.controller.dto.MenuInfo;
import quickbite.controller.dto.PelangganInfo;
import quickbite.repository.MenuRepository;
import quickbite.repository.PelangganRepository;

import java.util.List;

/**
 * Controller BCE untuk data katalog yang dibutuhkan halaman checkout:
 * daftar menu (beserta stok) dan daftar pelanggan.
 */
@Service
public class KatalogController {

    private final MenuRepository menuRepo;
    private final PelangganRepository pelangganRepo;

    public KatalogController(MenuRepository menuRepo,
                             PelangganRepository pelangganRepo) {
        this.menuRepo = menuRepo;
        this.pelangganRepo = pelangganRepo;
    }

    @Transactional(readOnly = true)
    public List<MenuInfo> daftarMenu() {
        return menuRepo.findAll(Sort.by("idMenu")).stream()
                .map(m -> new MenuInfo(
                        m.getIdMenu(), m.getNamaMenu(), m.getHarga(), m.getStok()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PelangganInfo> daftarPelanggan() {
        return pelangganRepo.findAll(Sort.by("idPelanggan")).stream()
                .map(p -> new PelangganInfo(p.getIdPelanggan(), p.getNama()))
                .toList();
    }
}
