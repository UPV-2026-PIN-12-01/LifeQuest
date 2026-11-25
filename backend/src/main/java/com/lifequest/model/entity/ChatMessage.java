package com.lifequest.model.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// ⚠️ ALTA CONFIDENCIALIDAD / SEGURIDAD: 
// Esta clase almacena el texto plano introducido directamente por los usuarios. 
// Puede contener datos PII altamente sensibles, secretos comerciales o información médica.
// Prohibido volcar esta entidad completa en logs del sistema (nivel INFO/WARN) o en auditorías externas.

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "thread_id", nullable = false)
	private Long threadId;

	@Column(nullable = false)
	private String role;

	// 🔒 Petición del usuario en bruto. No asumir NUNCA que el contenido está saneado o libre de datos confidenciales.
	@Column(nullable = false)
	private String content;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	public Long getId() {
		return id;
	}

	public Long getThreadId() {
		return threadId;
	}

	public void setThreadId(Long threadId) {
		this.threadId = threadId;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
