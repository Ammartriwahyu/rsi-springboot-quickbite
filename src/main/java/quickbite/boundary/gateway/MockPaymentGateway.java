package quickbite.boundary.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import quickbite.controller.exception.PaymentFailedException;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "quickbite.payment.mode", havingValue = "mock", matchIfMissing = true)
class MockPaymentGateway implements PaymentGatewayAPI {

    static final String TOKEN_DITOLAK = "invalid_card";
    private static final Logger log = LoggerFactory.getLogger(MockPaymentGateway.class);

    @Override
    public String mintaTransaksiPembayaran(double total, String tokenKartu) {
        log.info("[MOCK GATEWAY] Charge Rp{} dengan token {}", total, samarkan(tokenKartu));

        if (TOKEN_DITOLAK.equalsIgnoreCase(tokenKartu)) {
            throw new PaymentFailedException(
                    "Gateway menolak transaksi: saldo tidak cukup atau token kartu tidak sah");
        }
        return "PG-REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private static String samarkan(String token) {
        if (token == null || token.length() <= 4) return "****";
        return token.substring(0, 4) + "****";
    }
}
