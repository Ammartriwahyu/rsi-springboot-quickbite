const API_URL = "/api";

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

function ambilDaftarMenu() {
    return panggilApi("/menu");
}

function ambilDaftarPelanggan() {
    return panggilApi("/pelanggan");
}

function ambilDaftarPesanan() {
    return panggilApi("/pesanan");
}

function kirimCheckout(dataCheckout) {
    return panggilApi("/checkout", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(dataCheckout)
    });
}

function kirimPembatalan(idPesanan, alasan) {
    return panggilApi("/checkout/" + encodeURIComponent(idPesanan) + "/batal", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ alasan: alasan })
    });
}
