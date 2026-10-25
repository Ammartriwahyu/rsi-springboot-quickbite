package quickbite.controller.exception;

/** Payment gateway menolak atau gagal memproses transaksi. */
public class PaymentFailedException
        extends RuntimeException {

    public PaymentFailedException(String message) {
        super(message);
    }
}
