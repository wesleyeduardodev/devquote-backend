package br.com.devquote.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação para verificar perfis específicos
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresProfile {
    
    /**
     * Lista de códigos de perfis (usuário precisa ter pelo menos um)
     */
    String[] value();
    
    /**
     * Se true, usuário deve ter TODOS os perfis listados
     */
    boolean requireAll() default false;
}