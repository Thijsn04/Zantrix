package com.zantrix.interop;

import com.zantrix.iam.AuditLoggable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/interop")
public class InteropController {

    private final IntegrationChannelRepository channelRepository;
    private final MessageLogRepository messageLogRepository;

    public InteropController(IntegrationChannelRepository channelRepository, MessageLogRepository messageLogRepository) {
        this.channelRepository = channelRepository;
        this.messageLogRepository = messageLogRepository;
    }

    @GetMapping("/channels")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @AuditLoggable
    public List<IntegrationChannelEntity> getChannels() {
        return channelRepository.findAll();
    }

    @PostMapping("/channels")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @AuditLoggable
    public IntegrationChannelEntity createChannel(@RequestBody IntegrationChannelEntity channel) {
        return channelRepository.save(channel);
    }

    @GetMapping("/messages")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @AuditLoggable
    public List<MessageLogEntity> getMessages() {
        return messageLogRepository.findAll();
    }

    @PostMapping("/messages/{id}/reshoot")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @AuditLoggable
    public ResponseEntity<?> reshootMessage(@PathVariable UUID id) {
        MessageLogEntity message = messageLogRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Message not found"));
        
        // Requeue the message for processing
        message.setStatus(MessageLogEntity.Status.RETRYING);
        messageLogRepository.save(message);
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/webhook/{partnerId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @AuditLoggable
    public ResponseEntity<String> receiveWebhook(@PathVariable String partnerId, @RequestBody String payload) {
        MessageLogEntity log = new MessageLogEntity();
        log.setChannelId(UUID.randomUUID()); // placeholder
        log.setTimestamp(java.time.OffsetDateTime.now());
        log.setRawPayload(payload);
        log.setStatus(MessageLogEntity.Status.PROCESSED);
        messageLogRepository.save(log);
        
        return ResponseEntity.ok("Received");
    }
}
