package quickbite.boundary.gateway;

public interface PaymentGatewayAPI {

    String mintaTransaksiPembayaran(double total,
                                    String tokenKartu);
}
