package com.sobready.backend.enums;

/**
 * In NestJS you'd write:  enum ProductType { EAU_DE_PARFUM = "EAU_DE_PARFUM", ... }
 * In Java, enums are a special class. They serialize to JSON as strings automatically.
 */
public enum ProductType {
    EAU_DE_PARFUM,
    EAU_DE_TOILETTE,
    PARFUM,
    BODY_SPRAY
}
