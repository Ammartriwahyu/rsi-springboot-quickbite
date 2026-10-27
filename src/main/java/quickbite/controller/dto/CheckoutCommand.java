package quickbite.controller.dto;

import java.util.List;

public record CheckoutCommand(
        String idPelanggan, String tokenKartu,
        String metodePembayaran, List<Item> items) {

    public record Item(String idMenu, int qty) { }
}
