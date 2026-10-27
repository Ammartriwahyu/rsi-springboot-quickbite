package quickbite.controller.exception;

public class ResourceNotFoundException
        extends RuntimeException {

    public ResourceNotFoundException(String resource) {
        super(resource + " tidak ditemukan");
    }
}
