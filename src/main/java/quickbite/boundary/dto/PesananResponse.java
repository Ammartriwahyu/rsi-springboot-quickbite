package quickbite.boundary.dto;

import java.time.LocalDateTime;

public record PesananResponse(
        String idPesanan, String namaPelanggan,
        LocalDateTime tanggal, double total, String status) { }
