package br.com.devquote.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação para verificar permissões específicas em métodos
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {
    
    /**
     * Código do recurso
     */
    String resource();
    
    /**
     * Código da operação
     */
    String operation();
    
    /**
     * Verifica permissão para o próprio usuário logado
     */
    boolean checkCurrentUser() default true;
    
    /**
     * Permite que administradores sempre tenham acesso
     */
    boolean allowAdmin() default true;
}