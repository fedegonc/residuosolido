package com.residuosolido.app.controller;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Inyecta el {@code User} autenticado en un parámetro de handler method
 * (null si la sesión es anónima). Resuelto por {@link CurrentUserArgumentResolver}.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}
