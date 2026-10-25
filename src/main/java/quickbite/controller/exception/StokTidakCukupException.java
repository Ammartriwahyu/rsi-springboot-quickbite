package quickbite.controller.exception;

public class StokTidakCukupException
        extends RuntimeException {

    public StokTidakCukupException(String idMenu) {
        super("Stok tidak mencukupi untuk menu "
            + idMenu);
    }
}
