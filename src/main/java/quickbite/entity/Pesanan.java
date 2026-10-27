package quickbite.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "pesanan")
public class Pesanan {

    @Id
    private String idPesanan;
    private LocalDateTime tanggal;
    private double totalBiaya;

    @Enumerated(EnumType.STRING)
    private StatusPesanan status = StatusPesanan.MENUNGGU_BAYAR;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pelanggan", nullable = false)
    private Pelanggan pelanggan;

    @OneToMany(mappedBy = "pesanan", cascade = CascadeType.ALL,
               orphanRemoval = true)
    private List<ItemPesanan> items = new ArrayList<>();

    @OneToOne(mappedBy = "pesanan", cascade = CascadeType.ALL)
    private Pembayaran pembayaran;

    protected Pesanan() { }

    public Pesanan(String id, LocalDateTime tanggal,
                   Pelanggan pelanggan) {
        this.idPesanan = id;
        this.tanggal = tanggal;
        this.pelanggan = pelanggan;
    }

    public void tambahItem(ItemPesanan item) {
        item.setPesanan(this);
        items.add(item);
    }

    public double hitungTotal() {
        totalBiaya = items.stream()
                .mapToDouble(ItemPesanan::hitungSubtotal)
                .sum();
        return totalBiaya;
    }

    public void updateStatus(StatusPesanan statusBaru) {
        this.status = statusBaru;
    }

    public boolean bisaDibatalkan() {
        return status == StatusPesanan.MENUNGGU_BAYAR
            || status == StatusPesanan.DIPROSES;
    }

    public void batalkan() {
        if (!bisaDibatalkan()) {
            throw new IllegalStateException(
                "Pesanan berstatus " + status + " tidak dapat dibatalkan");
        }
        this.status = StatusPesanan.BATAL;
    }

    public void setPembayaran(Pembayaran pembayaran) {
        this.pembayaran = pembayaran;
    }

    public String getIdPesanan() { return idPesanan; }
    public LocalDateTime getTanggal() { return tanggal; }
    public double getTotalBiaya() { return totalBiaya; }
    public StatusPesanan getStatus() { return status; }
    public Pelanggan getPelanggan() { return pelanggan; }
    public Pembayaran getPembayaran() { return pembayaran; }
    public List<ItemPesanan> getItems() { return Collections.unmodifiableList(items); }
}
