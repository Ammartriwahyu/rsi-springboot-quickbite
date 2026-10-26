# QuickBite — Checkout & Pembayaran (Spring Boot, pola BCE)

Implementasi studi kasus **QuickBite** (materi 11b, Implementasi Spring Boot: Boundary–Controller–Entity),
dilengkapi bagian yang tidak ada di slide dari **Modul_Spring_order** (gateway tiruan, data seeder,
fitur batalkan transaksi, validasi input).

- Spring Boot 3.5 · Java 21 (bisa dijalankan dengan JDK 21 atau lebih baru)
- Database default: **H2 in-memory** (tanpa instalasi). PostgreSQL tersedia lewat profil `postgres`.
- Payment gateway default: **tiruan**, tidak memproses uang sungguhan.

## Menjalankan

Tidak perlu instal Maven, cukup pakai Maven Wrapper bawaan project.

```bash
./mvnw spring-boot:run
```

Di PowerShell atau CMD: `.\mvnw.cmd spring-boot:run`. Aplikasi jalan di `http://localhost:8080`.

Menjalankan semua test (22 test):

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

## Endpoint

| Method | URL | Kegunaan |
|---|---|---|
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
│   ├── ApiExceptionHandler       error domain → HTTP
│   ├── dto/                      CheckoutRequest/Response, BatalTransaksiRequest/Response, ApiError
│   └── gateway/
│       ├── PaymentGatewayAPI     interface outbound
│       ├── MockPaymentGateway    tiruan, aktif default
│       └── MidtransPaymentGateway adapter HTTP slide 19 (ilustrasi, mode=midtrans)
├── controller/                   <<controller>>
│   ├── CheckoutController        @Service @Transactional (orkestrator use case)
│   ├── dto/                      CheckoutCommand/Result, BatalTransaksiCommand/Result
│   └── exception/                StokTidakCukup, ResourceNotFound, PaymentFailed, PesananTidakBisaDibatalkan
├── entity/                       <<entity>>  Pelanggan, Menu, Pesanan, ItemPesanan, Pembayaran, StatusPesanan
└── repository/                   adapter persistensi (MenuRepository dengan pessimistic lock)
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
