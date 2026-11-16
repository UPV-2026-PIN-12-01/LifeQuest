package com.lifequest.model.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "achievement_index")
public class AchievementIndex {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String description;
	@Column(name = "xp_reward")
	private Long xpReward;
	@Column(name = "gold_reward")
	private Long goldReward;
	@Column(name = "icon_url")
	private String iconUrl;
	@Column(name = "requirement_type")
	private String requirementType;
	@Column(name = "requirement_target")
	private BigDecimal requirementTarget;
	@Column(name = "requirement_unit")
	private String requirementUnit;
	@Column(name = "requirement_activity")
	private String requirementActivity;

	public Long getId() { return id; }
	public String getName() { return name; }
	public String getDescription() { return description; }
	public Long getXpReward() { return xpReward; }
	public Long getGoldReward() { return goldReward; }
	public String getIconUrl() { return iconUrl; }
	public String getRequirementType() { return requirementType; }
	public BigDecimal getRequirementTarget() { return requirementTarget; }
	public String getRequirementUnit() { return requirementUnit; }
	public String getRequirementActivity() { return requirementActivity; }
}
