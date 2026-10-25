package quickbite.controller.exception;

import quickbite.entity.StatusPesanan;

/** Pesanan sudah dikirim, selesai, atau sudah dibatalkan sebelumnya. */
public class PesananTidakBisaDibatalkanException
        extends RuntimeException {

    public PesananTidakBisaDibatalkanException(String idPesanan, StatusPesanan status) {
        super("Pesanan " + idPesanan + " berstatus " + status
            + " dan tidak dapat dibatalkan");
    }
}
