package com.kgboilers.service;

/** Immutable mail payload, delivered only after the callback lead commits. */
public record CallbackRequestedEvent(String name, String phone, String email, String service, String message) {}
