package quickbite.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import quickbite.entity.Menu;

import java.util.Optional;

public interface MenuRepository
        extends JpaRepository<Menu, String> {

    /** Lock pessimistik: mencegah stok minus saat checkout bersamaan. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Menu m where m.idMenu = :id")
    Optional<Menu> findByIdForUpdate(@Param("id") String id);
}
