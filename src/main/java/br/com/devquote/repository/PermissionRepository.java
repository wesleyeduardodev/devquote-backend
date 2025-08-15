package br.com.devquote.repository;
import br.com.devquote.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);

    Boolean existsByName(String name);

    Set<Permission> findByNameIn(Set<String> names);

    Set<Permission> findByScreenPathIsNotNull();

    @Query("SELECT p FROM Permission p ORDER BY p.id ASC")
    List<Permission> findAllOrderedById();
}
