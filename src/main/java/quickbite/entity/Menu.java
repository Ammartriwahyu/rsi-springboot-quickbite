package quickbite.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Aturan stok ada di domain, bukan di Controller atau Boundary. */
@Entity
@Table(name = "menu")
public class Menu {

    @Id
    private String idMenu;
    private String namaMenu;
    private double harga;
    private int stok;

    protected Menu() { }

    public Menu(String idMenu, String namaMenu,
                double harga, int stok) {
        this.idMenu = idMenu;
        this.namaMenu = namaMenu;
        this.harga = harga;
        this.stok = stok;
    }

    public boolean validasiStok(int qty) {
        return qty > 0 && stok >= qty;
    }

    public boolean kurangiStok(int qty) {
        if (!validasiStok(qty)) return false;
        this.stok -= qty;
        return true;
    }

    /** Mengembalikan stok saat pesanan dibatalkan. */
    public void tambahStok(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Jumlah stok yang dikembalikan harus lebih dari 0");
        }
        this.stok += qty;
    }

    public String getIdMenu() { return idMenu; }
    public String getNamaMenu() { return namaMenu; }
    public double getHarga() { return harga; }
    public int getStok() { return stok; }
}
