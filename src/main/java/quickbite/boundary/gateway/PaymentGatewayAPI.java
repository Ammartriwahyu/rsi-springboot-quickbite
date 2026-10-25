package quickbite.boundary.gateway;

/**
 * Boundary eksternal (outbound) ke payment gateway pihak ketiga.
 * Controller hanya mengenal interface ini, bukan implementasinya.
 */
public interface PaymentGatewayAPI {

    /**
     * @return kode referensi transaksi dari gateway
     * @throws quickbite.controller.exception.PaymentFailedException kalau gateway menolak
     */
    String mintaTransaksiPembayaran(double total,
                                    String tokenKartu);
}
