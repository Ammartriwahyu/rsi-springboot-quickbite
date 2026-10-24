package quickbite.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pelanggan")
public class Pelanggan {

    @Id
    private String idPelanggan;
    private String nama;
    private String email;
    private String nomorTelepon;
    private String alamat;

    @OneToMany(mappedBy = "pelanggan")
    private List<Pesanan> daftarPesanan = new ArrayList<>();

    protected Pelanggan() { }              // wajib untuk JPA

    public Pelanggan(String id, String nama, String email,
                     String telepon, String alamat) {
        this.idPelanggan = id;
        this.nama = nama;
        this.email = email;
        this.nomorTelepon = telepon;
        this.alamat = alamat;
    }

    public String getInfoPelanggan() {
        return nama + " <" + email + ">";
    }

    public void ubahAlamat(String alamatBaru) {
        if (alamatBaru == null || alamatBaru.isBlank()) {
            throw new IllegalArgumentException("Alamat baru tidak boleh kosong");
        }
        this.alamat = alamatBaru;
    }

    public String getIdPelanggan() { return idPelanggan; }
    public String getNama() { return nama; }
    public String getEmail() { return email; }
    public String getNomorTelepon() { return nomorTelepon; }
    public String getAlamat() { return alamat; }
}
