package br.com.devquote.repository;
import br.com.devquote.entity.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {

    @Query("SELECT ta FROM TaskAttachment ta WHERE ta.task.id = :taskId ORDER BY ta.createdAt ASC")
    List<TaskAttachment> findByTaskId(@Param("taskId") Long taskId);
}