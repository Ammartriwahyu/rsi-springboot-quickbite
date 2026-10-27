package quickbite.boundary.dto;

import jakarta.validation.constraints.NotBlank;
import quickbite.controller.dto.BatalTransaksiCommand;

public record BatalTransaksiRequest(
        @NotBlank(message = "alasan pembatalan wajib diisi") String alasan) {

    public BatalTransaksiCommand toCommand(String idPesanan) {
        return new BatalTransaksiCommand(idPesanan, alasan);
    }
}
