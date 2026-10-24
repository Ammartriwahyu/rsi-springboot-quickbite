package quickbite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import quickbite.entity.Pelanggan;

public interface PelangganRepository
        extends JpaRepository<Pelanggan, String> { }
