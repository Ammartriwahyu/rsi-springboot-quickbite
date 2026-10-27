// ============================================================
// api.js - semua pemanggilan ke backend QuickBite ada di sini
// ============================================================

const API_URL = "/api";

// Fungsi umum untuk memanggil backend.
// Selalu mengembalikan { ok, status, data } supaya mudah dicek di app.js
async function panggilApi(url, opsi) {
    try {
        const response = await fetch(API_URL + url, opsi);
        const teks = await response.text();

        let data = null;
        if (teks) {
            try {
                data = JSON.parse(teks);
            } catch (e) {
                data = null;
            }
        }
        return { ok: response.ok, status: response.status, data: data };
    } catch (error) {
        // fetch gagal total, biasanya karena server belum dijalankan
        return {
            ok: false,
            status: 0,
            data: {
                code: "SERVER_MATI",
                message: "Tidak bisa terhubung ke server. Pastikan backend sudah dijalankan."
            }
        };
    }
}

// GET /api/menu
function ambilDaftarMenu() {
    return panggilApi("/menu");
}

// GET /api/pelanggan
function ambilDaftarPelanggan() {
    return panggilApi("/pelanggan");
}

// GET /api/pesanan
function ambilDaftarPesanan() {
    return panggilApi("/pesanan");
}

// POST /api/checkout
function kirimCheckout(dataCheckout) {
    return panggilApi("/checkout", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(dataCheckout)
    });
}

// POST /api/checkout/{idPesanan}/batal
function kirimPembatalan(idPesanan, alasan) {
    return panggilApi("/checkout/" + encodeURIComponent(idPesanan) + "/batal", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ alasan: alasan })
    });
}
