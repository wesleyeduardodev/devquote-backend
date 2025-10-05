package br.com.devquote.repository;
import br.com.devquote.entity.SubTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubTaskRepository extends JpaRepository<SubTask, Long> {

    @Query("SELECT s FROM SubTask s WHERE s.task.id = :taskId ORDER BY s.id ASC")
    List<SubTask> findByTaskId(Long taskId);

    void deleteByTaskId(Long taskId);

    @Query("SELECT s FROM SubTask s ORDER BY s.id ASC")
    List<SubTask> findAllOrderedById();

    @Query("SELECT s FROM SubTask s WHERE s.task.id IN :taskIds ORDER BY s.id ASC")
    List<SubTask> findByTaskIdIn(List<Long> taskIds);
}
