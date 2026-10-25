package quickbite.boundary.dto;

import jakarta.validation.constraints.NotBlank;
import quickbite.controller.dto.BatalTransaksiCommand;

/** Kontrak HTTP untuk POST /api/checkout/{idPesanan}/batal. */
public record BatalTransaksiRequest(
        @NotBlank(message = "alasan pembatalan wajib diisi") String alasan) {

    public BatalTransaksiCommand toCommand(String idPesanan) {
        return new BatalTransaksiCommand(idPesanan, alasan);
    }
}
