package br.com.devquote.repository;
import br.com.devquote.entity.Requester;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RequesterRepository extends JpaRepository<Requester, Long> {

    @Query("SELECT r FROM Requester r ORDER BY r.id ASC")
    List<Requester> findAllOrderedById();

    @Query("SELECT r FROM Requester r WHERE " +
            "(:id IS NULL OR r.id = :id) AND " +
            "(:name IS NULL OR :name = '' OR LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:email IS NULL OR :email = '' OR LOWER(r.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:phone IS NULL OR :phone = '' OR LOWER(r.phone) LIKE LOWER(CONCAT('%', :phone, '%'))) AND " +
            "(:createdAt IS NULL OR :createdAt = '' OR CAST(r.createdAt AS STRING) LIKE CONCAT('%', :createdAt, '%')) AND " +
            "(:updatedAt IS NULL OR :updatedAt = '' OR CAST(r.updatedAt AS STRING) LIKE CONCAT('%', :updatedAt, '%'))")
    Page<Requester> findByOptionalFieldsPaginated(
            @Param("id") Long id,
            @Param("name") String name,
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("createdAt") String createdAt,
            @Param("updatedAt") String updatedAt,
            Pageable pageable
    );
}
