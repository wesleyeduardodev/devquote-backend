package br.com.devquote.repository;
import br.com.devquote.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.userProfiles up LEFT JOIN FETCH up.profile WHERE u.email = :email AND u.active = true")
    Optional<User> findByEmailWithProfiles(String email);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.userProfiles up LEFT JOIN FETCH up.profile WHERE u.username = :username AND u.active = true")
    Optional<User> findByUsernameWithProfiles(String username);


    @Query("SELECT u FROM User u ORDER BY u.id ASC")
    List<User> findAllOrderedById();
    
    @Query("SELECT u FROM User u WHERE " +
           "(:id IS NULL OR u.id = :id) AND " +
           "(:username IS NULL OR :username = '' OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))) AND " +
           "(:email IS NULL OR :email = '' OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:name IS NULL OR :name = '' OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:active IS NULL OR u.active = :active)")
    Page<User> findAllWithFilters(@Param("id") Long id,
                                   @Param("username") String username,
                                   @Param("email") String email,
                                   @Param("name") String name,
                                   @Param("active") Boolean active,
                                   Pageable pageable);
}

