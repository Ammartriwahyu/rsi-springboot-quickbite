package quickbite.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quickbite.boundary.gateway.PaymentGatewayAPI;
import quickbite.controller.dto.BatalTransaksiCommand;
import quickbite.controller.dto.BatalTransaksiResult;
import quickbite.controller.dto.CheckoutCommand;
import quickbite.controller.dto.CheckoutResult;
import quickbite.controller.exception.PesananTidakBisaDibatalkanException;
import quickbite.controller.exception.ResourceNotFoundException;
import quickbite.controller.exception.StokTidakCukupException;
import quickbite.entity.ItemPesanan;
import quickbite.entity.Menu;
import quickbite.entity.Pelanggan;
import quickbite.entity.Pembayaran;
import quickbite.entity.Pesanan;
import quickbite.entity.StatusPesanan;
import quickbite.repository.MenuRepository;
import quickbite.repository.PelangganRepository;
import quickbite.repository.PesananRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller BCE: orkestrator use case checkout dan pembatalan.
 * Ini application service (@Service), bukan @RestController.
 */
@Service
public class CheckoutController {

    private static final Logger log = LoggerFactory.getLogger(CheckoutController.class);
    private static final String METODE_DEFAULT = "KARTU_KREDIT";

    private final PelangganRepository pelangganRepo;
    private final MenuRepository menuRepo;
    private final PesananRepository pesananRepo;
    private final PaymentGatewayAPI paymentGateway;

    public CheckoutController(
            PelangganRepository pelangganRepo,
            MenuRepository menuRepo,
            PesananRepository pesananRepo,
            PaymentGatewayAPI paymentGateway) {
        this.pelangganRepo = pelangganRepo;
        this.menuRepo = menuRepo;
        this.pesananRepo = pesananRepo;
        this.paymentGateway = paymentGateway;
    }

    @Transactional
    public CheckoutResult prosesCheckout(CheckoutCommand cmd) {
        // 1. Validasi pelanggan
        Pelanggan pelanggan = pelangganRepo
                .findById(cmd.idPelanggan())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Pelanggan " + cmd.idPelanggan()));

        // 2. Validasi menu dan stok (menu yang sama di keranjang dijumlahkan dulu)
        List<ResolvedItem> items = new ArrayList<>();
        for (Map.Entry<String, Integer> ci : gabungkanQty(cmd.items()).entrySet()) {
            Menu menu = menuRepo
                    .findByIdForUpdate(ci.getKey())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Menu " + ci.getKey()));
            if (!menu.validasiStok(ci.getValue())) {
                throw new StokTidakCukupException(ci.getKey());
            }
            items.add(new ResolvedItem(menu, ci.getValue()));
        }

        // 3. Bentuk pesanan (komposisi Pesanan ◆ ItemPesanan)
        Pesanan pesanan = new Pesanan(
                UUID.randomUUID().toString(),
                LocalDateTime.now(), pelanggan);
        for (ResolvedItem ri : items) {
            pesanan.tambahItem(new ItemPesanan(
                    UUID.randomUUID().toString(),
                    ri.qty(), ri.menu()));
        }
        double total = pesanan.hitungTotal();

        // 4. Minta pembayaran ke boundary eksternal.
        //    Kalau gagal, PaymentFailedException membuat @Transactional rollback.
        String kodeRef = paymentGateway
                .mintaTransaksiPembayaran(
                        total, cmd.tokenKartu());

        String metode = cmd.metodePembayaran() == null || cmd.metodePembayaran().isBlank()
                ? METODE_DEFAULT : cmd.metodePembayaran();
        Pembayaran pembayaran = new Pembayaran(
                UUID.randomUUID().toString(), pesanan,
                total, metode, "SUKSES", kodeRef);
        pesanan.setPembayaran(pembayaran);

        // 5. Potong stok, ubah status, simpan (cascade ikut menyimpan item & pembayaran)
        for (ResolvedItem ri : items) {
            if (!ri.menu().kurangiStok(ri.qty())) {
                throw new StokTidakCukupException(ri.menu().getIdMenu());
            }
        }
        pesanan.updateStatus(pembayaran.prosesVerifikasi()
                ? StatusPesanan.DIPROSES
                : StatusPesanan.MENUNGGU_BAYAR);
        pesananRepo.save(pesanan);

        return new CheckoutResult(
                pesanan.getIdPesanan(), total);
    }

    @Transactional
    public BatalTransaksiResult batalkanTransaksi(BatalTransaksiCommand cmd) {
        Pesanan pesanan = pesananRepo
                .findById(cmd.idPesanan())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Pesanan " + cmd.idPesanan()));

        if (!pesanan.bisaDibatalkan()) {
            throw new PesananTidakBisaDibatalkanException(
                    pesanan.getIdPesanan(), pesanan.getStatus());
        }

        // Kembalikan stok tiap menu yang dipesan
        for (ItemPesanan item : pesanan.getItems()) {
            Menu menu = menuRepo
                    .findByIdForUpdate(item.getMenu().getIdMenu())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Menu " + item.getMenu().getIdMenu()));
            menu.tambahStok(item.getJumlah());
        }
        pesanan.batalkan();
        pesananRepo.save(pesanan);

        log.info("Pesanan {} dibatalkan. Alasan: {}", cmd.idPesanan(), cmd.alasan());
        return new BatalTransaksiResult(
                pesanan.getIdPesanan(), pesanan.getStatus().name(), cmd.alasan());
    }

    /** Menu yang muncul lebih dari sekali di keranjang dijumlahkan qty-nya. */
    private static Map<String, Integer> gabungkanQty(List<CheckoutCommand.Item> items) {
        Map<String, Integer> hasil = new LinkedHashMap<>();
        for (CheckoutCommand.Item item : items) {
            hasil.merge(item.idMenu(), item.qty(), Integer::sum);
        }
        return hasil;
    }

    private record ResolvedItem(Menu menu, int qty) { }
}
