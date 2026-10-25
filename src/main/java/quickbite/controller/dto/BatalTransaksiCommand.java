package quickbite.controller.dto;

/** Input use case batalkanTransaksi (dari modul). */
public record BatalTransaksiCommand(
        String idPesanan, String alasan) { }
