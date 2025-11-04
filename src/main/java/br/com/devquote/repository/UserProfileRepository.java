package br.com.devquote.repository;
import br.com.devquote.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    @Query("SELECT up FROM UserProfile up WHERE up.user.id = :userId")
    List<UserProfile> findByUserId(@Param("userId") Long userId);

    @Query("SELECT up FROM UserProfile up JOIN FETCH up.user WHERE up.profile.id = :profileId AND up.active = true")
    List<UserProfile> findActiveByProfileId(@Param("profileId") Long profileId);

    @Query("SELECT up FROM UserProfile up WHERE up.user.id = :userId AND up.profile.id = :profileId")
    Optional<UserProfile> findByUserIdAndProfileId(@Param("userId") Long userId, @Param("profileId") Long profileId);
}