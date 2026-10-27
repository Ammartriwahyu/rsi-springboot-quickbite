# QuickBite — Checkout & Pembayaran (Spring Boot, pola BCE)

Implementasi studi kasus **QuickBite** (materi 11b, Implementasi Spring Boot: Boundary–Controller–Entity),
dilengkapi bagian yang tidak ada di slide dari **Modul_Spring_order** (gateway tiruan, data seeder,
fitur batalkan transaksi, validasi input).

- Spring Boot 3.5 · Java 21 (bisa dijalankan dengan JDK 21 atau lebih baru)
- Database default: **H2 in-memory** (tanpa instalasi). PostgreSQL tersedia lewat profil `postgres`.
- Payment gateway default: **tiruan**, tidak memproses uang sungguhan.
- Frontend: HTML, CSS, dan JavaScript biasa, tersaji langsung oleh Spring Boot di `http://localhost:8080`.

## Menjalankan

Tidak perlu instal Maven, cukup pakai Maven Wrapper bawaan project.

```bash
./mvnw spring-boot:run
```

Di PowerShell atau CMD: `.\mvnw.cmd spring-boot:run`. Aplikasi jalan di `http://localhost:8080`.

Menjalankan semua test (26 test):

```bash
./mvnw test
```

Melihat isi database: buka `http://localhost:8080/h2-console`, isi JDBC URL
`jdbc:h2:mem:quickbite`, user `sa`, password dikosongkan.

> Data di H2 hilang setiap aplikasi dimatikan, lalu dibuat ulang dari `V2__seed.sql` saat dinyalakan.

### Memakai PostgreSQL (opsional, sesuai slide 30)

Buat database `quickbite` dengan user `quickbite`, lalu:

```bash
DB_PASSWORD=passwordmu ./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

## Halaman frontend

Setelah aplikasi jalan, buka `http://localhost:8080` di browser. Alurnya mengikuti use case checkout:

1. **Pilih pelanggan**, diambil dari `GET /api/pelanggan`
2. **Daftar menu** beserta harga dan stok, diambil dari `GET /api/menu`. Isi jumlah, lalu klik **Tambah**.
3. **Keranjang**: lihat subtotal dan total, atau hapus item
4. **Pembayaran**: pilih metode, isi token kartu, lalu klik **Checkout** (`POST /api/checkout`)
5. **Riwayat pesanan**: klik **Batalkan** untuk membatalkan pesanan (`POST /api/checkout/{id}/batal`)

Pesan sukses tampil dalam kotak hijau, dan pesan error dari backend tampil dalam kotak merah lengkap
dengan kode HTTP-nya. Stok sengaja tidak dicek di browser supaya aturan stok tetap dijalankan oleh
entity `Menu` di backend. Isi token `invalid_card` untuk mencoba pembayaran yang ditolak.

File frontend ada di `src/main/resources/static/`:

| File | Isi |
|---|---|
| `index.html` | Struktur halaman |
| `css/style.css` | Tampilan |
| `js/api.js` | Fungsi pemanggil endpoint backend (`fetch`) |
| `js/app.js` | Logika keranjang, checkout, riwayat, dan pembatalan |

## Endpoint

| Method | URL | Kegunaan |
|---|---|---|
| GET | `/api/menu` | Daftar menu beserta harga dan stok |
| GET | `/api/pelanggan` | Daftar pelanggan |
| POST | `/api/checkout` | Checkout keranjang dan bayar |
| POST | `/api/checkout/{idPesanan}/batal` | Batalkan pesanan, stok dikembalikan |

| Kode | `code` di JSON | Kapan |
|---|---|---|
| 200 | — | Berhasil |
| 400 | `VALIDASI_GAGAL` / `FORMAT_TIDAK_VALID` | Input kosong, qty < 1, JSON rusak |
| 404 | `NOT_FOUND` | Pelanggan, menu, atau pesanan tidak ada |
| 409 | `STOK_HABIS` | Stok tidak cukup |
| 409 | `TIDAK_BISA_DIBATALKAN` | Pesanan sudah dikirim, selesai, atau sudah dibatalkan |
| 502 | `PAYMENT_FAILED` | Gateway menolak (token `invalid_card`) |

### Contoh request

Contoh body request ada di folder [`test-requests/`](test-requests). Jalankan dari folder project
(di PowerShell gunakan `curl.exe`, bukan `curl`):

```bash
curl.exe -X POST http://localhost:8080/api/checkout -H "Content-Type: application/json" -d @test-requests/1-checkout-sukses.json
```

| File | Hasil yang diharapkan |
|---|---|
| `1-checkout-sukses.json` | 200, total 75000 (contoh slide 32) |
| `2-checkout-modul.json` | 200, total 61000 (contoh modul) |
| `3-stok-habis.json` | 409 |
| `4-menu-tidak-ada.json` | 404 |
| `5-gateway-menolak.json` | 502, stok dan pesanan tidak berubah (rollback) |
| `6-input-tidak-valid.json` | 400 |
| `7-batal.json` | Kirim ke `/api/checkout/{idPesanan}/batal` dengan idPesanan dari hasil checkout → 200; kirim lagi → 409 |

Body yang sama bisa dipakai di Postman (Body → raw → JSON).

### Data awal (`V2__seed.sql`)

| Pelanggan | Menu | Harga | Stok |
|---|---|---|---|
| `P-01`, `CUST-001` | `M-01` Nasi Goreng | 25.000 | 50 |
| | `M-02` Ayam Geprek | 25.000 | 50 |
| | `MENU-01` Kopi Susu Gula Aren | 18.000 | 20 |
| | `MENU-02` Croissant Butter | 25.000 | 10 |

## Struktur & pemetaan BCE

```
src/main/java/quickbite/
├── boundary/                     <<boundary>>
│   ├── CheckoutBoundary          @RestController (CheckoutUI)
│   ├── KatalogBoundary           @RestController: GET menu & pelanggan untuk halaman checkout
│   ├── ApiExceptionHandler       error domain → HTTP
│   ├── dto/                      CheckoutRequest/Response, BatalTransaksiRequest/Response,
│   │                             MenuResponse, PelangganResponse, ApiError
│   └── gateway/
│       ├── PaymentGatewayAPI     interface outbound
│       ├── MockPaymentGateway    tiruan, aktif default
│       └── MidtransPaymentGateway adapter HTTP slide 19 (ilustrasi, mode=midtrans)
├── controller/                   <<controller>>
│   ├── CheckoutController        @Service @Transactional (orkestrator use case)
│   ├── KatalogController         @Service: menyiapkan daftar menu & pelanggan
│   ├── dto/                      CheckoutCommand/Result, BatalTransaksiCommand/Result,
│   │                             MenuInfo, PelangganInfo
│   └── exception/                StokTidakCukup, ResourceNotFound, PaymentFailed, PesananTidakBisaDibatalkan
├── entity/                       <<entity>>  Pelanggan, Menu, Pesanan, ItemPesanan, Pembayaran, StatusPesanan
└── repository/                   adapter persistensi (MenuRepository dengan pessimistic lock)

src/main/resources/static/        frontend (index.html, css/, js/)
```

Aturan dependensi: `boundary → controller → entity`. Entity tidak meng-import boundary/controller,
Boundary tidak mengakses repository, dan Controller tidak mengembalikan entity.

### Traceability Sequence 8b → kode

| Pesan di sequence | Implementasi |
|---|---|
| `klikTombolCheckout(keranjangData)` | `POST /api/checkout` → `CheckoutBoundary.checkout()` |
| `prosesCheckout(idPelanggan, keranjangData)` | `CheckoutController.prosesCheckout(CheckoutCommand)` |
| `Menu.validasiStok(qty)` | `menu.validasiStok(qty)` |
| `new Pesanan` + `tambahItem` | `new Pesanan(...)` + `pesanan.tambahItem(...)` |
| `mintaTransaksiPembayaran(total, token)` | `paymentGateway.mintaTransaksiPembayaran(...)` |
| `new Pembayaran`, `updateStatus`, `kurangiStok` | `new Pembayaran(...)`, `updateStatus()`, `kurangiStok()` |
| `checkoutBerhasil(idPesanan, total)` | HTTP 200 + `CheckoutResponse` |
| `batalkanTransaksi(alasan)` | `POST /api/checkout/{id}/batal` → `CheckoutController.batalkanTransaksi()` → `pesanan.batalkan()` + `menu.tambahStok()` |

## Catatan perbedaan dari slide & modul

- **Gateway tiruan** (dari modul) dipakai karena format request `MidtransPaymentGateway` di slide 19
  bukan format API Midtrans yang asli. Adapter slide tetap ada, aktif dengan `PAYMENT_MODE=midtrans`.
- **Fitur batal** (dari modul) dipindah aturannya ke entity (`Pesanan.batalkan()`, `Menu.tambahStok()`)
  supaya tidak menjadi anemic entity. Pesanan yang sudah `BATAL` juga ditolak, supaya stok tidak
  dikembalikan dua kali.
- Menu yang muncul lebih dari sekali di keranjang dijumlahkan dulu qty-nya sebelum cek stok.
- `metodePembayaran` (dari modul) boleh dikosongkan; default `KARTU_KREDIT`.
- Test Boundary memakai `@MockitoBean`, pengganti `@MockBean` (slide 31) yang sudah deprecated.
- Seperti catatan produksi slide 26, panggilan gateway masih berada di dalam `@Transactional`.
  Idempotency-Key dan pemisahan transaksi tidak diimplementasikan.
