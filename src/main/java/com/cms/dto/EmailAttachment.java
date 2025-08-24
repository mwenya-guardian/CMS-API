package com.cms.dto;

/**
 * Simple, provider-agnostic attachment DTO.
 * Use InputStreamSource so the implementation can stream.
 *
 * @param filename    e.g., "invoice.pdf"
 * @param data        a streamable source (e.g., ByteArrayResource, FileSystemResource)
 * @param contentType e.g., "application/pdf"
 * @param inline      true => embed as inline (use Content-ID for images)
 */
public record EmailAttachment(String filename, byte[] data, String contentType, boolean inline) {

}

