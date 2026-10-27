package quickbite.controller.dto;

import java.time.LocalDateTime;

public record PesananInfo(
        String idPesanan, String namaPelanggan,
        LocalDateTime tanggal, double total, String status) { }
