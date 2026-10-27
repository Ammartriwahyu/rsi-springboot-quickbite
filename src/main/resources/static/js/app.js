let daftarMenu = [];
let daftarPelanggan = [];
let keranjang = [];
let riwayat = [];

const JUDUL_ERROR = {
    STOK_HABIS: "Stok tidak mencukupi",
    NOT_FOUND: "Data tidak ditemukan",
    PAYMENT_FAILED: "Pembayaran ditolak",
    VALIDASI_GAGAL: "Input belum lengkap",
    FORMAT_TIDAK_VALID: "Format data salah",
    TIDAK_BISA_DIBATALKAN: "Pesanan tidak bisa dibatalkan",
    SERVER_MATI: "Server tidak dapat dihubungi"
};

function formatRupiah(angka) {
    return "Rp " + Number(angka).toLocaleString("id-ID");
}

function formatWaktu(tanggal) {
    return new Date(tanggal).toLocaleString("id-ID", {
        day: "2-digit", month: "2-digit", year: "numeric",
        hour: "2-digit", minute: "2-digit"
    });
}

function buatSel(teks) {
    const td = document.createElement("td");
    td.textContent = teks;
    return td;
}

function barisKosong(tbody, teks, jumlahKolom = 5) {
    tbody.innerHTML = "";
    const tr = document.createElement("tr");
    const td = buatSel(teks);
    td.colSpan = jumlahKolom;
    td.className = "kosong";
    tr.appendChild(td);
    tbody.appendChild(tr);
}

function tampilkanPesan(jenis, judul, daftarDetail) {
    const kotakPesan = document.getElementById("pesan");
    kotakPesan.innerHTML = "";
    kotakPesan.className = "pesan " + jenis;

    const strong = document.createElement("strong");
    strong.textContent = judul;
    kotakPesan.appendChild(strong);

    if (daftarDetail && daftarDetail.length > 0) {
        const ul = document.createElement("ul");
        for (const detail of daftarDetail) {
            const li = document.createElement("li");
            li.textContent = detail;
            ul.appendChild(li);
        }
        kotakPesan.appendChild(ul);
    }

    kotakPesan.hidden = false;
    window.scrollTo({ top: 0, behavior: "smooth" });
}

function tampilkanError(hasil) {
    const data = hasil.data || {};
    let judul = JUDUL_ERROR[data.code] || "Terjadi kesalahan";
    if (hasil.status) {
        judul += " (HTTP " + hasil.status + ")";
    }

    const detail = [];
    if (data.message) {
        detail.push(data.message);
    }
    if (data.errors) {
        for (const field in data.errors) {
            detail.push(field + ": " + data.errors[field]);
        }
    }
    tampilkanPesan("error", judul, detail);
}

async function muatPelanggan() {
    const hasil = await ambilDaftarPelanggan();
    const select = document.getElementById("pilih-pelanggan");
    select.innerHTML = "";

    if (!hasil.ok) {
        select.innerHTML = '<option value="">-- gagal memuat pelanggan --</option>';
        tampilkanError(hasil);
        return;
    }

    daftarPelanggan = hasil.data;

    const opsiAwal = document.createElement("option");
    opsiAwal.value = "";
    opsiAwal.textContent = "-- pilih pelanggan --";
    select.appendChild(opsiAwal);

    for (const pelanggan of daftarPelanggan) {
        const opsi = document.createElement("option");
        opsi.value = pelanggan.idPelanggan;
        opsi.textContent = pelanggan.nama + " (" + pelanggan.idPelanggan + ")";
        select.appendChild(opsi);
    }
}

async function muatMenu() {
    const hasil = await ambilDaftarMenu();
    const tbody = document.getElementById("tabel-menu");

    if (!hasil.ok) {
        barisKosong(tbody, "Gagal memuat menu");
        tampilkanError(hasil);
        return;
    }

    daftarMenu = hasil.data;
    tampilkanMenu();
}

function tampilkanMenu() {
    const tbody = document.getElementById("tabel-menu");
    tbody.innerHTML = "";

    if (daftarMenu.length === 0) {
        barisKosong(tbody, "Belum ada menu");
        return;
    }

    for (const menu of daftarMenu) {
        const tr = document.createElement("tr");
        tr.appendChild(buatSel(menu.namaMenu));
        tr.appendChild(buatSel(formatRupiah(menu.harga)));
        tr.appendChild(buatSel(menu.stok > 0 ? menu.stok : "Habis"));

        const tdJumlah = document.createElement("td");
        const inputJumlah = document.createElement("input");
        inputJumlah.type = "number";
        inputJumlah.min = "1";
        inputJumlah.value = "1";
        tdJumlah.appendChild(inputJumlah);
        tr.appendChild(tdJumlah);

        const tdAksi = document.createElement("td");
        const tombol = document.createElement("button");
        tombol.textContent = "Tambah";
        tombol.disabled = menu.stok <= 0;
        tombol.onclick = function () {
            tambahKeKeranjang(menu, inputJumlah.value);
        };
        tdAksi.appendChild(tombol);
        tr.appendChild(tdAksi);

        tbody.appendChild(tr);
    }
}

function tambahKeKeranjang(menu, jumlahInput) {
    const qty = parseInt(jumlahInput, 10);
    if (isNaN(qty) || qty < 1) {
        tampilkanPesan("error", "Jumlah tidak valid", ["Jumlah minimal 1."]);
        return;
    }

    const itemLama = keranjang.find(item => item.idMenu === menu.idMenu);
    if (itemLama) {
        itemLama.qty += qty;
    } else {
        keranjang.push({
            idMenu: menu.idMenu,
            namaMenu: menu.namaMenu,
            harga: menu.harga,
            qty: qty
        });
    }
    tampilkanKeranjang();
}

function hapusDariKeranjang(idMenu) {
    keranjang = keranjang.filter(item => item.idMenu !== idMenu);
    tampilkanKeranjang();
}

function tampilkanKeranjang() {
    const tbody = document.getElementById("tabel-keranjang");
    tbody.innerHTML = "";
    let total = 0;

    if (keranjang.length === 0) {
        barisKosong(tbody, "Keranjang masih kosong");
    }

    for (const item of keranjang) {
        const subtotal = item.harga * item.qty;
        total += subtotal;

        const tr = document.createElement("tr");
        tr.appendChild(buatSel(item.namaMenu));
        tr.appendChild(buatSel(formatRupiah(item.harga)));
        tr.appendChild(buatSel(item.qty));
        tr.appendChild(buatSel(formatRupiah(subtotal)));

        const tdAksi = document.createElement("td");
        const tombolHapus = document.createElement("button");
        tombolHapus.textContent = "Hapus";
        tombolHapus.className = "tombol-hapus";
        tombolHapus.onclick = function () {
            hapusDariKeranjang(item.idMenu);
        };
        tdAksi.appendChild(tombolHapus);
        tr.appendChild(tdAksi);

        tbody.appendChild(tr);
    }

    document.getElementById("total-biaya").textContent = formatRupiah(total);
}

async function prosesCheckout(event) {
    event.preventDefault();

    const idPelanggan = document.getElementById("pilih-pelanggan").value;
    const metode = document.getElementById("metode").value;
    const tokenKartu = document.getElementById("token-kartu").value.trim();

    const kurang = [];
    if (!idPelanggan) kurang.push("Pilih pelanggan terlebih dahulu.");
    if (keranjang.length === 0) kurang.push("Keranjang masih kosong.");
    if (!tokenKartu) kurang.push("Token kartu wajib diisi.");
    if (kurang.length > 0) {
        tampilkanPesan("error", "Data belum lengkap", kurang);
        return;
    }

    const dataCheckout = {
        idPelanggan: idPelanggan,
        tokenKartu: tokenKartu,
        metodePembayaran: metode,
        keranjang: keranjang.map(item => ({ idMenu: item.idMenu, qty: item.qty }))
    };

    const tombol = document.getElementById("tombol-checkout");
    tombol.disabled = true;
    tombol.textContent = "Memproses...";

    const hasil = await kirimCheckout(dataCheckout);

    if (hasil.ok) {
        tampilkanPesan("sukses", "Checkout berhasil!", [
            "ID Pesanan: " + hasil.data.idPesanan,
            "Total dibayar: " + formatRupiah(hasil.data.total)
        ]);
        keranjang = [];
        tampilkanKeranjang();
    } else {
        tampilkanError(hasil);
    }

    tombol.disabled = false;
    tombol.textContent = "Checkout";
    await muatMenu();
    await muatRiwayat();
}

async function muatRiwayat() {
    const hasil = await ambilDaftarPesanan();

    if (!hasil.ok) {
        barisKosong(document.getElementById("tabel-riwayat"), "Gagal memuat riwayat", 6);
        return;
    }

    riwayat = hasil.data;
    tampilkanRiwayat();
}

function tampilkanRiwayat() {
    const tbody = document.getElementById("tabel-riwayat");
    tbody.innerHTML = "";

    if (riwayat.length === 0) {
        barisKosong(tbody, "Belum ada pesanan", 6);
        return;
    }

    for (const pesanan of riwayat) {
        const tr = document.createElement("tr");
        tr.appendChild(buatSel(formatWaktu(pesanan.tanggal)));

        const tdId = buatSel(pesanan.idPesanan.substring(0, 8) + "...");
        tdId.title = pesanan.idPesanan;
        tr.appendChild(tdId);
        tr.appendChild(buatSel(pesanan.namaPelanggan));
        tr.appendChild(buatSel(formatRupiah(pesanan.total)));

        const tdStatus = buatSel(pesanan.status);
        tdStatus.className = "status status-" + pesanan.status;
        tr.appendChild(tdStatus);

        const tdAksi = document.createElement("td");
        if (pesanan.status === "DIPROSES" || pesanan.status === "MENUNGGU_BAYAR") {
            const tombolBatal = document.createElement("button");
            tombolBatal.textContent = "Batalkan";
            tombolBatal.className = "tombol-hapus";
            tombolBatal.onclick = function () {
                batalkanPesanan(pesanan);
            };
            tdAksi.appendChild(tombolBatal);
        } else {
            tdAksi.textContent = "-";
        }
        tr.appendChild(tdAksi);

        tbody.appendChild(tr);
    }
}

async function batalkanPesanan(pesanan) {
    const alasan = prompt("Alasan pembatalan pesanan:");
    if (alasan === null) {
        return;
    }

    const hasil = await kirimPembatalan(pesanan.idPesanan, alasan.trim());

    if (hasil.ok) {
        tampilkanPesan("sukses", "Pesanan berhasil dibatalkan", [
            "ID Pesanan: " + hasil.data.idPesanan,
            "Alasan: " + hasil.data.alasan,
            "Stok menu sudah dikembalikan."
        ]);
    } else {
        tampilkanError(hasil);
    }

    await muatMenu();
    await muatRiwayat();
}

document.getElementById("form-checkout").addEventListener("submit", prosesCheckout);
muatPelanggan();
muatMenu();
muatRiwayat();
