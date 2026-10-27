CREATE TABLE pelanggan (
    id_pelanggan   VARCHAR(36)  PRIMARY KEY,
    nama           VARCHAR(100) NOT NULL,
    email          VARCHAR(100) NOT NULL UNIQUE,
    nomor_telepon  VARCHAR(20),
    alamat         VARCHAR(255)
);

CREATE TABLE menu (
    id_menu    VARCHAR(36)      PRIMARY KEY,
    nama_menu  VARCHAR(100)     NOT NULL,
    harga      DOUBLE PRECISION NOT NULL,
    stok       INTEGER          NOT NULL CHECK (stok >= 0)
);

CREATE TABLE pesanan (
    id_pesanan    VARCHAR(36)      PRIMARY KEY,
    tanggal       TIMESTAMP        NOT NULL,
    total_biaya   DOUBLE PRECISION NOT NULL,
    status        VARCHAR(20)      NOT NULL,
    id_pelanggan  VARCHAR(36)      NOT NULL REFERENCES pelanggan (id_pelanggan)
);

CREATE TABLE item_pesanan (
    id_item     VARCHAR(36)      PRIMARY KEY,
    jumlah      INTEGER          NOT NULL CHECK (jumlah > 0),
    subtotal    DOUBLE PRECISION NOT NULL,
    id_pesanan  VARCHAR(36)      NOT NULL REFERENCES pesanan (id_pesanan) ON DELETE CASCADE,
    id_menu     VARCHAR(36)      NOT NULL REFERENCES menu (id_menu)
);

CREATE TABLE pembayaran (
    id_pembayaran   VARCHAR(36)      PRIMARY KEY,
    jumlah_bayar    DOUBLE PRECISION NOT NULL,
    metode          VARCHAR(50)      NOT NULL,
    status_bayar    VARCHAR(50)      NOT NULL,
    kode_referensi  VARCHAR(100),
    id_pesanan      VARCHAR(36)      NOT NULL UNIQUE REFERENCES pesanan (id_pesanan) ON DELETE CASCADE
);
