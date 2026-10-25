package quickbite.boundary.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import quickbite.controller.dto.CheckoutCommand;

import java.util.List;

/** Kontrak HTTP untuk POST /api/checkout. metodePembayaran boleh dikosongkan. */
public record CheckoutRequest(
        @NotBlank(message = "idPelanggan wajib diisi") String idPelanggan,
        @NotBlank(message = "tokenKartu wajib diisi") String tokenKartu,
        String metodePembayaran,
        @NotEmpty(message = "keranjang tidak boleh kosong") List<@Valid CartItem> keranjang) {

    public record CartItem(
            @NotBlank(message = "idMenu wajib diisi") String idMenu,
            @Min(value = 1, message = "qty minimal 1") int qty) { }

    /** Map DTO web → Command, supaya Controller tidak bergantung pada DTO HTTP. */
    public CheckoutCommand toCommand() {
        return new CheckoutCommand(idPelanggan, tokenKartu, metodePembayaran,
                keranjang.stream()
                        .map(i -> new CheckoutCommand.Item(
                                i.idMenu(), i.qty()))
                        .toList());
    }
}
