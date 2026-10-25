package quickbite.boundary.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import quickbite.controller.exception.PaymentFailedException;

import java.util.Map;

/**
 * Adapter HTTP sesuai slide 19. Hanya ilustrasi: format request ini
 * bukan format API Midtrans yang asli, jadi tidak akan berhasil ke server Midtrans.
 * Aktif hanya kalau quickbite.payment.mode=midtrans.
 */
@Component
@ConditionalOnProperty(name = "quickbite.payment.mode", havingValue = "midtrans")
class MidtransPaymentGateway implements PaymentGatewayAPI {

    private final RestClient restClient;

    MidtransPaymentGateway(RestClient.Builder builder,
                           @Value("${quickbite.payment.base-url}")
                           String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public String mintaTransaksiPembayaran(
            double total, String token) {
        PaymentResponse res;
        try {
            res = restClient.post()
                    .uri("/v1/transactions")
                    .body(Map.of("total", total, "token", token))
                    .retrieve()
                    .body(PaymentResponse.class);
        } catch (RestClientException e) {
            throw new PaymentFailedException(
                    "Gateway tidak dapat dihubungi: " + e.getMessage());
        }
        if (res == null || !res.success()) {
            throw new PaymentFailedException(
                    "Gateway menolak transaksi");
        }
        return res.reference();
    }

    record PaymentResponse(boolean success, String reference) { }
}
