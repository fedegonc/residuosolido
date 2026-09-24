package com.residuosolido.app.exception;

/**
 * Fuente única de verdad para TODAS las claves i18n que nacen en el servidor:
 * las que viajan como mensaje de excepción (error.*) y las que se flashean
 * desde controllers (flash.*, auth.*).
 *
 * Antes eran strings crudos dispersos en ~60 sitios — un typo en cualquiera
 * producía drift silencioso contra i18n/{lang}.json (bugs reales: login.error,
 * mat_*). Ahora el código compila o no compila; el drift es imposible.
 *
 * Contrato: code() es la clave "con puntos" que recibe MessageSource;
 * serverKey() es la clave final en los JSON ("_server_" + puntos→guiones).
 * ServerMessageContractTest verifica que cada serverKey() exista en es/pt.
 */
public enum ServerMessage {

    // ── auth.* ──
    AUTH_LOGIN_BLOCKED("auth.login_blocked"),
    AUTH_LOGIN_ERROR("auth.login_error"),
    AUTH_LOGIN_SUCCESS("auth.login_success"),

    // ── error.* (lanzadas como excepción desde dominio/servicios) ──
    ERROR_IMAGE_INVALID_EXTENSION("error.image.invalid_extension"),
    ERROR_IMAGE_INVALID_TYPE("error.image.invalid_type"),
    ERROR_IMAGE_TOO_LARGE("error.image.too_large"),
    ERROR_NAME_REQUIRED("error.name.required"),
    ERROR_NAME_TOO_LONG("error.name.too_long"),
    ERROR_PHONE_INVALID("error.phone.invalid"),
    ERROR_PHONE_INVALID_DDD("error.phone.invalid_ddd"),
    ERROR_PHONE_INVALID_FIRST_DIGIT("error.phone.invalid_first_digit"),
    ERROR_PHONE_INVALID_LENGTH("error.phone.invalid_length"),
    ERROR_PHONE_REQUIRED("error.phone.required"),
    ERROR_PHONE_UNSUPPORTED_COUNTRY("error.phone.unsupported_country"),
    ERROR_PROFILE_CITY_REQUIRED("error.profile.city_required"),
    ERROR_PROFILE_PHONE_REQUIRED("error.profile.phone_required"),
    ERROR_REGISTER_EMAIL_EXISTS("error.register.email_exists"),
    ERROR_REGISTER_EMAIL_INVALID("error.register.email_invalid"),
    ERROR_REGISTER_IDENTITY_EXISTS("error.register.identity_exists"),
    ERROR_REGISTER_PHONE_REQUIRED("error.register.phone_required"),
    ERROR_REGISTER_PIN_INVALID("error.register.pin_invalid"),
    ERROR_REGISTER_USERNAME_EXISTS("error.register.username_exists"),
    ERROR_REGISTER_USERNAME_REQUIRED("error.register.username_required"),
    ERROR_REGISTER_USERNAME_TOO_LONG("error.register.username_too_long"),
    ERROR_REQUEST_ACCEPT_NOT_PENDING("error.request.accept_not_pending"),
    ERROR_REQUEST_ADDRESS_REQUIRED("error.request.address_required"),
    ERROR_REQUEST_ASSIGN_NOT_ORGANIZATION("error.request.assign_not_organization"),
    ERROR_REQUEST_CITIZEN_REQUIRED("error.request.citizen_required"),
    ERROR_REQUEST_CITY_REQUIRED("error.request.city_required"),
    ERROR_REQUEST_COMPLETE_NOT_IN_PROGRESS("error.request.complete_not_in_progress"),
    ERROR_REQUEST_GUEST_NAME_REQUIRED("error.request.guest_name_required"),
    ERROR_REQUEST_GUEST_PHONE_REQUIRED("error.request.guest_phone_required"),
    ERROR_REQUEST_MATERIALS_NOT_ACCEPTED("error.request.materials_not_accepted"),
    ERROR_REQUEST_MATERIALS_REQUIRED("error.request.materials_required"),
    ERROR_REQUEST_ORGANIZATION_NOT_FOUND("error.request.organization_not_found"),
    ERROR_REQUEST_ORGANIZATION_NOT_IN_CITY("error.request.organization_not_in_city"),
    ERROR_REQUEST_ORGANIZATION_REQUIRED("error.request.organization_required"),
    ERROR_REQUEST_ORGANIZATION_UNAVAILABLE("error.request.organization_unavailable"),
    ERROR_REQUEST_REJECT_INVALID_STATE("error.request.reject_invalid_state"),
    ERROR_REQUEST_SLOT_REQUIRED("error.request.slot_required"),
    ERROR_USER_NOT_FOUND("error.user.not_found"),

    // ── flash.* (mensajes que sobreviven un redirect) ──
    FLASH_ERROR_ACCESS_DENIED("flash.error.access_denied"),
    FLASH_ERROR_GENERIC("flash.error.generic"),
    FLASH_ERROR_NOT_FOUND_AUTH("flash.error.not_found_auth"),
    FLASH_ERROR_NOT_FOUND_GUEST("flash.error.not_found_guest"),
    FLASH_ORG_PROFILE_LOAD_ERROR("flash.org.profile_load_error"),
    FLASH_ORG_REQUEST_ACCEPTED("flash.org.request_accepted"),
    FLASH_ORG_REQUEST_COMPLETED("flash.org.request_completed"),
    FLASH_ORG_REQUEST_NOT_FOUND("flash.org.request_not_found"),
    FLASH_ORG_REQUEST_NOT_OWNED("flash.org.request_not_owned"),
    FLASH_ORG_REQUEST_REJECTED("flash.org.request_rejected"),
    FLASH_PROFILE_UPDATE_ERROR("flash.profile.update_error"),
    FLASH_PROFILE_UPDATED("flash.profile.updated"),
    FLASH_REQUEST_CONCURRENT_MODIFICATION("flash.request.concurrent_modification"),
    FLASH_REQUEST_CREATED("flash.request.created"),
    FLASH_REQUEST_CREATE_ERROR("flash.request.create_error"),
    FLASH_REQUEST_DELETED("flash.request.deleted"),
    FLASH_REQUEST_DELETE_CONCURRENT("flash.request.delete.concurrent"),
    FLASH_REQUEST_EDIT_PENDING_ONLY("flash.request.edit.pending_only"),
    FLASH_REQUEST_IMAGE_UPLOAD_FAILED("flash.request.image_upload_failed"),
    FLASH_REQUEST_NOT_FOUND("flash.request.not_found"),
    FLASH_REQUEST_NOT_OWNED("flash.request.not_owned"),
    FLASH_REQUEST_RATE_LIMITED("flash.request.rate_limited"),
    FLASH_REQUEST_UPDATED("flash.request.updated");

    private final String code;

    ServerMessage(String code) {
        this.code = code;
    }

    /** Clave "con puntos" que consume MessageSource. */
    public String code() {
        return code;
    }

    /** Clave final en i18n/{lang}.json: "_server_" + puntos→guiones. */
    public String serverKey() {
        return "_server_" + code.replace('.', '_');
    }
}
