package br.com.devquote.repository;
import br.com.devquote.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    // NOVO SISTEMA: Busca com perfis
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.userProfiles up LEFT JOIN FETCH up.profile WHERE u.email = :email AND u.active = true")
    Optional<User> findByEmailWithProfiles(String email);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.userProfiles up LEFT JOIN FETCH up.profile WHERE u.username = :username AND u.active = true")
    Optional<User> findByUsernameWithProfiles(String username);

    // SISTEMA ANTERIOR: Mantido para compatibilidade
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.email = :email")
    Optional<User> findByEmailWithRolesAndPermissions(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.username = :username")
    Optional<User> findByUsernameWithRolesAndPermissions(String username);

    @Query("SELECT u FROM User u ORDER BY u.id ASC")
    List<User> findAllOrderedById();
}

