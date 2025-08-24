package br.com.devquote.repository;

import br.com.devquote.entity.ResourcePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourcePermissionRepository extends JpaRepository<ResourcePermission, Long> {

    @Query("SELECT rp FROM ResourcePermission rp WHERE rp.profile.id = :profileId AND rp.active = true")
    List<ResourcePermission> findActiveByProfileId(@Param("profileId") Long profileId);

    @Query("SELECT rp FROM ResourcePermission rp WHERE rp.resource.id = :resourceId AND rp.active = true")
    List<ResourcePermission> findActiveByResourceId(@Param("resourceId") Long resourceId);

    @Query("SELECT rp FROM ResourcePermission rp WHERE rp.profile.id = :profileId AND rp.resource.code = :resourceCode AND rp.operation.code = :operationCode AND rp.active = true")
    Optional<ResourcePermission> findByProfileIdAndResourceCodeAndOperationCode(
        @Param("profileId") Long profileId,
        @Param("resourceCode") String resourceCode,
        @Param("operationCode") String operationCode
    );

    @Query("""
        SELECT rp FROM ResourcePermission rp 
        JOIN rp.profile p 
        JOIN rp.resource r 
        JOIN rp.operation o
        WHERE p.code = :profileCode 
        AND r.code = :resourceCode 
        AND o.code = :operationCode 
        AND rp.active = true 
        AND rp.granted = true
    """)
    Optional<ResourcePermission> findGrantedPermission(
        @Param("profileCode") String profileCode,
        @Param("resourceCode") String resourceCode,
        @Param("operationCode") String operationCode
    );

    @Query("""
        SELECT CASE WHEN COUNT(rp) > 0 THEN true ELSE false END 
        FROM ResourcePermission rp 
        JOIN rp.profile p 
        JOIN UserProfile up ON up.profile = p
        WHERE up.user.id = :userId 
        AND rp.resource.code = :resourceCode 
        AND rp.operation.code = :operationCode 
        AND rp.active = true 
        AND rp.granted = true 
        AND up.active = true
    """)
    boolean hasUserPermission(
        @Param("userId") Long userId,
        @Param("resourceCode") String resourceCode,
        @Param("operationCode") String operationCode
    );
}