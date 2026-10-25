package quickbite.controller.exception;

/** Data (pelanggan, menu, pesanan) tidak ada di database. */
public class ResourceNotFoundException
        extends RuntimeException {

    public ResourceNotFoundException(String resource) {
        super(resource + " tidak ditemukan");
    }
}
