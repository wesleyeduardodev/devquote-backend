package br.com.devquote.repository;
import br.com.devquote.entity.Requester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RequesterRepository extends JpaRepository<Requester, Long> {

    @Query("SELECT r FROM Requester r ORDER BY r.id ASC")
    List<Requester> findAllOrderedById();
}
