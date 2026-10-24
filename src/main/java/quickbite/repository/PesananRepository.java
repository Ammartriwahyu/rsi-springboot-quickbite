package quickbite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import quickbite.entity.Pesanan;

public interface PesananRepository
        extends JpaRepository<Pesanan, String> { }
