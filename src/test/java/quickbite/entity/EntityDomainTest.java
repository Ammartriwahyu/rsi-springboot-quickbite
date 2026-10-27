package quickbite.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntityDomainTest {

    private final Pelanggan pelanggan =
            new Pelanggan("P-01", "Andi", "andi@example.com", "0812", "Malang");

    @Test
    void menu_validasi_dan_kurangi_stok() {
        Menu menu = new Menu("M-01", "Nasi Goreng", 25000, 5);

        assertThat(menu.validasiStok(5)).isTrue();
        assertThat(menu.validasiStok(6)).isFalse();
        assertThat(menu.validasiStok(0)).isFalse();

        assertThat(menu.kurangiStok(3)).isTrue();
        assertThat(menu.getStok()).isEqualTo(2);
        assertThat(menu.kurangiStok(3)).isFalse();
        assertThat(menu.getStok()).isEqualTo(2);

        menu.tambahStok(3);
        assertThat(menu.getStok()).isEqualTo(5);
    }

    @Test
    void pesanan_menghitung_total_dari_semua_item() {
        Menu nasi = new Menu("M-01", "Nasi Goreng", 25000, 10);
        Menu kopi = new Menu("M-02", "Kopi", 18000, 10);
        Pesanan pesanan = new Pesanan("ORD-1", LocalDateTime.now(), pelanggan);

        pesanan.tambahItem(new ItemPesanan("I-1", 2, nasi));
        pesanan.tambahItem(new ItemPesanan("I-2", 1, kopi));

        assertThat(pesanan.hitungTotal()).isEqualTo(68000.0);
        assertThat(pesanan.getItems()).hasSize(2);
        assertThat(pesanan.getStatus()).isEqualTo(StatusPesanan.MENUNGGU_BAYAR);
    }

    @Test
    void pesanan_hanya_bisa_dibatalkan_sebelum_dikirim() {
        Pesanan pesanan = new Pesanan("ORD-1", LocalDateTime.now(), pelanggan);
        pesanan.updateStatus(StatusPesanan.DIPROSES);
        assertThat(pesanan.bisaDibatalkan()).isTrue();

        pesanan.batalkan();
        assertThat(pesanan.getStatus()).isEqualTo(StatusPesanan.BATAL);

        assertThat(pesanan.bisaDibatalkan()).isFalse();
        assertThatThrownBy(pesanan::batalkan).isInstanceOf(IllegalStateException.class);

        Pesanan dikirim = new Pesanan("ORD-2", LocalDateTime.now(), pelanggan);
        dikirim.updateStatus(StatusPesanan.DIKIRIM);
        assertThat(dikirim.bisaDibatalkan()).isFalse();
    }

    @Test
    void pembayaran_terverifikasi_jika_sukses_dan_ada_referensi() {
        Pesanan pesanan = new Pesanan("ORD-1", LocalDateTime.now(), pelanggan);

        assertThat(new Pembayaran("PAY-1", pesanan, 1000, "KARTU_KREDIT", "SUKSES", "REF-1")
                .prosesVerifikasi()).isTrue();
        assertThat(new Pembayaran("PAY-2", pesanan, 1000, "KARTU_KREDIT", "SUKSES", null)
                .prosesVerifikasi()).isFalse();
        assertThat(new Pembayaran("PAY-3", pesanan, 1000, "KARTU_KREDIT", "GAGAL", "REF-3")
                .prosesVerifikasi()).isFalse();
    }
}
