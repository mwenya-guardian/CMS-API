package com.cms.dto;


/**
 * Return container for provider metadata. Populate what you can.
 *
 * @param providerMessageId  e.g., Mailgun message-id
 * @param providerStatusCode optional HTTP status or SMTP status code
 * @param rawResponse        optional raw body for logging/debug
 */
public record SendResult(boolean success, String providerMessageId, int providerStatusCode, String rawResponse) {

}

