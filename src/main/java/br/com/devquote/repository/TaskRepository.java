package br.com.devquote.repository;
import br.com.devquote.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t ORDER BY t.id ASC")
    List<Task> findAllOrderedById();
}
