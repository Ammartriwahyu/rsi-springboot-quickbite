-- Data awal untuk uji coba.
-- P-01, M-01, M-02 : contoh di slide 11b (2 x M-01 + 1 x M-02 = 75.000)
-- CUST-001, MENU-01, MENU-02 : contoh di modul (2 x MENU-01 + 1 x MENU-02 = 61.000)

INSERT INTO pelanggan (id_pelanggan, nama, email, nomor_telepon, alamat) VALUES
    ('P-01',     'Andi Wijaya',  'andi@example.com',  '081200000001', 'Jl. Veteran, Malang'),
    ('CUST-001', 'Budi Santoso', 'budi@example.com',  '08123456789',  'Jl. Sudirman No. 45, Jakarta');

INSERT INTO menu (id_menu, nama_menu, harga, stok) VALUES
    ('M-01',    'Nasi Goreng',         25000.0, 50),
    ('M-02',    'Ayam Geprek',         25000.0, 50),
    ('MENU-01', 'Kopi Susu Gula Aren', 18000.0, 20),
    ('MENU-02', 'Croissant Butter',    25000.0, 10);
