package br.com.devquote.repository;

import br.com.devquote.entity.SubTaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubTaskAttachmentRepository extends JpaRepository<SubTaskAttachment, Long> {

    @Query("SELECT sa FROM SubTaskAttachment sa WHERE sa.subTask.id = :subTaskId ORDER BY sa.createdAt ASC")
    List<SubTaskAttachment> findBySubTaskId(@Param("subTaskId") Long subTaskId);

    @Query("SELECT sa FROM SubTaskAttachment sa WHERE sa.subTask.task.id = :taskId ORDER BY sa.createdAt ASC")
    List<SubTaskAttachment> findByTaskId(@Param("taskId") Long taskId);
}
