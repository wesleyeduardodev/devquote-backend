package br.com.devquote.repository;

import br.com.devquote.entity.FieldPermission;
import br.com.devquote.entity.FieldPermissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FieldPermissionRepository extends JpaRepository<FieldPermission, Long> {

    @Query("SELECT fp FROM FieldPermission fp WHERE fp.profile.id = :profileId AND fp.active = true")
    List<FieldPermission> findActiveByProfileId(@Param("profileId") Long profileId);

    @Query("SELECT fp FROM FieldPermission fp WHERE fp.resource.id = :resourceId AND fp.active = true")
    List<FieldPermission> findActiveByResourceId(@Param("resourceId") Long resourceId);

    @Query("""
        SELECT fp FROM FieldPermission fp 
        WHERE fp.profile.id = :profileId 
        AND fp.resource.code = :resourceCode 
        AND fp.fieldName = :fieldName 
        AND fp.active = true
    """)
    Optional<FieldPermission> findByProfileIdAndResourceCodeAndFieldName(
        @Param("profileId") Long profileId,
        @Param("resourceCode") String resourceCode,
        @Param("fieldName") String fieldName
    );

    @Query("""
        SELECT fp FROM FieldPermission fp 
        JOIN fp.profile p 
        JOIN UserProfile up ON up.profile = p
        WHERE up.user.id = :userId 
        AND fp.resource.code = :resourceCode 
        AND fp.fieldName = :fieldName 
        AND fp.active = true 
        AND up.active = true
    """)
    List<FieldPermission> findUserFieldPermissions(
        @Param("userId") Long userId,
        @Param("resourceCode") String resourceCode,
        @Param("fieldName") String fieldName
    );

    @Query("""
        SELECT fp.permissionType FROM FieldPermission fp 
        JOIN fp.profile p 
        JOIN UserProfile up ON up.profile = p
        WHERE up.user.id = :userId 
        AND fp.resource.code = :resourceCode 
        AND fp.fieldName = :fieldName 
        AND fp.active = true 
        AND up.active = true
        ORDER BY p.level ASC
    """)
    List<FieldPermissionType> findUserFieldPermissionTypes(
        @Param("userId") Long userId,
        @Param("resourceCode") String resourceCode,
        @Param("fieldName") String fieldName
    );
}