package com.lifequest.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifequest.exception.InvalidStateException;
import com.lifequest.exception.ResourceNotFoundException;
import com.lifequest.model.dto.AchievementDto;
import com.lifequest.model.entity.Achievement;
import com.lifequest.model.entity.AchievementIndex;
import com.lifequest.model.entity.Reward;
import com.lifequest.model.entity.RewardSource;
import com.lifequest.model.entity.Task;
import com.lifequest.model.entity.User;
import com.lifequest.repository.AchievementIndexRepository;
import com.lifequest.repository.AchievementRepository;
import com.lifequest.repository.ChallengeRepository;
import com.lifequest.repository.TaskRepository;
import com.lifequest.repository.TaskSessionRepository;
import com.lifequest.repository.UserRepository;

@Service
public class AchievementService {
	private static final Pattern FIRST_NUMBER = Pattern.compile("(\\d+)");
	private static final List<String> FEATURED_NAMES = List.of(
			"Primer paso", "Principiante", "Constante", "Concentrado",
			"Héroe", "Ratón de biblioteca", "Veterano", "Imparable");
	private static final Map<String, Integer> FEATURED_NAME_ORDER = Map.ofEntries(
			Map.entry("Primer paso", 0), Map.entry("Principiante", 1), Map.entry("Productivo", 1),
			Map.entry("Constante", 2), Map.entry("Concentrado", 3), Map.entry("Enfocado", 3),
			Map.entry("Héroe", 4), Map.entry("Nivel 10", 4), Map.entry("Ratón de biblioteca", 5),
			Map.entry("Maestro del Tiempo", 5), Map.entry("Veterano", 6), Map.entry("Campeón", 6),
			Map.entry("Imparable", 7));
	private static final Set<String> FEATURED_NAME_SET = FEATURED_NAME_ORDER.keySet();
	private final AchievementIndexRepository indexRepository;
	private final AchievementRepository achievementRepository;
	private final TaskRepository taskRepository;
	private final TaskSessionRepository taskSessionRepository;
	private final ChallengeRepository challengeRepository;
	private final UserRepository userRepository;
	private final CharacterStatsService characterStatsService;
	private final RewardService rewardService;
	private final Clock clock;

	public AchievementService(AchievementIndexRepository indexRepository, AchievementRepository achievementRepository,
			TaskRepository taskRepository, TaskSessionRepository taskSessionRepository, UserRepository userRepository,
			ChallengeRepository challengeRepository, CharacterStatsService characterStatsService,
			RewardService rewardService, Clock clock) {
		this.indexRepository = indexRepository;
		this.achievementRepository = achievementRepository;
		this.taskRepository = taskRepository;
		this.taskSessionRepository = taskSessionRepository;
		this.challengeRepository = challengeRepository;
		this.userRepository = userRepository;
		this.characterStatsService = characterStatsService;
		this.rewardService = rewardService;
		this.clock = clock;
	}

	//Listar logros UT 621 - PA 1808
	@Transactional
	public List<AchievementDto> findForUser(Long userId) {
		// Backfill achievements for progress earned before this feature was introduced.
		evaluateAndUnlock(userId);
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		ProgressSnapshot snapshot = snapshot(userId, user);
		//Logros desbloqueados
		Map<Long, Achievement> unlocked = achievementRepository.findByUserId(userId).stream()
				.collect(Collectors.toMap(Achievement::getIndexId, Function.identity()));
		//Devuelve la lista de TODOS los logros, mapeando además los obtenidos por el usuario y ordenados por index
		return indexRepository.findAll().stream()
				.sorted((left, right) -> Integer.compare(featuredOrder(left), featuredOrder(right)))
				.map(index -> toDto(index, unlocked.get(index.getId()), user, snapshot))
				.toList();
	}

	private int featuredOrder(AchievementIndex index) {
		return FEATURED_NAME_ORDER.getOrDefault(index.getName(), FEATURED_NAMES.size());
	}

	@Transactional
	public AchievementDto claim(Long userId, Long indexId) {
		AchievementIndex index = indexRepository.findById(indexId)
				.orElseThrow(() -> new ResourceNotFoundException("Achievement not found"));
		Achievement achievement = achievementRepository.findByUserIdAndIndexId(userId, indexId)
				.orElseThrow(() -> new InvalidStateException("Achievement is not unlocked"));
		if (achievement.getRewardId() == null) {
			throw new InvalidStateException("Achievement has no reward");
		}
		Reward reward = rewardService.getByIdForUpdate(achievement.getRewardId());
		if (reward.getClaimedAt() != null) {
			throw new InvalidStateException("Achievement reward was already claimed");
		}
		long awardedXp = characterStatsService.addXp(userId, reward.getXpReward());
		characterStatsService.addGold(userId, reward.getGoldReward());
		rewardService.claim(reward, awardedXp, reward.getGoldReward(), clock.instant());
		return toDto(index, achievement, userId, userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found")));
	}

	//Evaluar los logros del index y desbloquear los logros de un jugador concreto
	@Transactional
	public List<String> evaluateAndUnlock(Long userId) {
		List<String> newlyUnlocked = new java.util.ArrayList<>();
		User user = userRepository.findByIdForUpdate(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		ProgressSnapshot snapshot = snapshot(userId, user);
		//Lista de logros del usuario
		Map<Long, Achievement> unlocked = achievementRepository.findByUserId(userId).stream()
				.collect(Collectors.toMap(Achievement::getIndexId, Function.identity()));
		
		//Filtro de guardado de logros
		for (AchievementIndex index : indexRepository.findAll()) {
			long target = target(index);
			//Descartados:
			if (unlocked.containsKey(index.getId()) //a) Ya desbloqueados (UT 621 - PA 1811)
					|| "unsupported".equals(requirementType(index)) //b) Sin cubrir
					|| target <= 0 //c) Sin definir
					|| progress(index, user, snapshot) < target //d) Logro no completado (UT 621 - PA 1810)
				) {
				continue;
			}

			//Creando el resto de logros (los que SÍ están desbloqueados)
			Achievement achievement = new Achievement();
			achievement.setUserId(userId);
			achievement.setIndexId(index.getId());
			achievement.setRewardId(rewardService.create(userId, index.getXpReward(), index.getGoldReward(),
					RewardSource.ARCHIVEMENT));
			try {
				//Guardar logro
				achievementRepository.saveAndFlush(achievement);
				newlyUnlocked.add(index.getName());
			} catch (DataIntegrityViolationException exception) {
				if (achievementRepository.findByUserIdAndIndexId(userId, index.getId()).isEmpty()) {
					throw exception;
				}
			}
		}
		//Devolvemos los recién desbloqueados UT 621 PA 1809
		return newlyUnlocked;
	}

	private AchievementDto toDto(AchievementIndex index, Achievement achievement, Long userId, User user) {
		long target = target(index);
		long progress = Math.min(target, progress(index, userId, user));
		boolean claimed = achievement != null && achievement.getRewardId() != null
				&& rewardService.getById(achievement.getRewardId()).getClaimedAt() != null;
		return new AchievementDto(index.getId(), index.getName(), index.getDescription(), index.getIconUrl(),
				progress, target, index.getRequirementUnit(), value(index.getXpReward()), value(index.getGoldReward()),
				achievement != null, achievement == null ? null : achievement.getAcquiredAt(), claimed);
	}

	private AchievementDto toDto(AchievementIndex index, Achievement achievement, User user,
			ProgressSnapshot snapshot) {
		long target = target(index);
		long progress = Math.min(target, progress(index, user, snapshot));
		boolean claimed = achievement != null && achievement.getRewardId() != null
				&& rewardService.getById(achievement.getRewardId()).getClaimedAt() != null;
		return new AchievementDto(index.getId(), index.getName(), index.getDescription(), index.getIconUrl(),
				progress, target, index.getRequirementUnit(), value(index.getXpReward()), value(index.getGoldReward()),
				achievement != null, achievement == null ? null : achievement.getAcquiredAt(), claimed);
	}

	private long progress(AchievementIndex index, Long userId, User user) {
		return switch (requirementType(index)) {
			case "completed_tasks" -> completedTasks(index, userId);
			case "day_streak" -> Math.max(user.getDayStreak(), user.getMaxDayStreak());
			case "focused_tasks" -> focusedChallenges(userId);
			case "level" -> characterStatsService.getByUserId(userId).getLevel();
			case "study_minutes" -> studyMinutes(userId);
			default -> 0;
		};
	}

	private long progress(AchievementIndex index, User user, ProgressSnapshot snapshot) {
		return switch (requirementType(index)) {
			case "completed_tasks" -> snapshot.completedTasks(requirementActivity(index));
			case "day_streak" -> Math.max(user.getDayStreak(), user.getMaxDayStreak());
			case "focused_tasks" -> snapshot.focusedChallenges;
			case "level" -> snapshot.level;
			case "study_minutes" -> snapshot.studyMinutes;
			default -> 0;
		};
	}

	private ProgressSnapshot snapshot(Long userId, User user) {
		List<Task> completed = taskRepository.findCompletedByUserId(userId);
		long focused = focusedChallenges(userId);
		long studyMinutes = completed.stream()
				.filter(task -> task.getActivityType() != null && task.getActivityType().name().equals("STUDY"))
				.mapToLong(task -> Duration.between(task.getStartTime(), task.getEndTime()).toMinutes())
				.sum();
		return new ProgressSnapshot(completed, focused, characterStatsService.getByUserId(userId).getLevel(),
				studyMinutes);
	}

	private record ProgressSnapshot(List<Task> completed, long focusedChallenges, int level, long studyMinutes) {
		private long completedTasks(String activity) {
			return completed.stream()
					.filter(task -> activity == null
							|| (task.getActivityType() != null && activity.equals(task.getActivityType().name())))
					.count();
		}
	}

	private long completedTasks(AchievementIndex index, Long userId) {
		return taskRepository.findCompletedByUserId(userId).stream()
				.filter(task -> requirementActivity(index) == null
						|| (task.getActivityType() != null
								&& requirementActivity(index).equals(task.getActivityType().name())))
				.count();
	}

	private long focusedChallenges(Long userId) {
		List<Long> taskIds = challengeRepository.findByUserIdAndCompletedTrueAndRatingIsNotNull(userId).stream()
				.map(challenge -> challenge.getTaskId())
				.filter(java.util.Objects::nonNull)
				.toList();
		if (taskIds.isEmpty()) return 0;
		return taskSessionRepository.findByTaskIdIn(taskIds).stream()
				.filter(session -> session.getFocusedSeconds() > 0)
				.count();
	}

	private long studyMinutes(Long userId) {
		return taskRepository.findCompletedByUserId(userId).stream()
				.filter(task -> task.getActivityType() != null && task.getActivityType().name().equals("STUDY"))
				.mapToLong(task -> Duration.between(task.getStartTime(), task.getEndTime()).toMinutes())
				.sum();
	}

	private long target(AchievementIndex index) {
		Matcher matcher = FIRST_NUMBER.matcher(index.getDescription() == null ? "" : index.getDescription());
		if (matcher.find()) {
			long parsed = Long.parseLong(matcher.group(1));
			return "study_minutes".equals(requirementType(index)) ? parsed * 60 : parsed;
		}
		BigDecimal target = index.getRequirementTarget();
		return target == null ? 0 : target.longValueExact();
	}

	private String requirementType(AchievementIndex index) {
		if (index.getRequirementType() != null && !index.getRequirementType().isBlank()
				&& !isDefaultRequirement(index)) {
			return index.getRequirementType();
		}
		String description = lowerDescription(index);
		if (description.contains("nivel")) return "level";
		if (description.contains("hora")) return "study_minutes";
		if (description.contains("racha") || description.contains("día")) return "day_streak";
		if (description.contains("foco") || description.contains("concentr")) return "focused_tasks";
		if (description.contains("tarea") || description.contains("misión")) return "completed_tasks";
		if (description.contains("monstruo") || description.contains("jugador")) return "battle";
		if (description.contains("arma") || description.contains("herramienta")) return "weapon";
		return "unsupported";
	}

	private String requirementActivity(AchievementIndex index) {
		if (index.getRequirementActivity() != null && !index.getRequirementActivity().isBlank()) {
			return index.getRequirementActivity();
		}
		String description = lowerDescription(index);
		if (description.contains("trabajo")) return "WORK";
		if (description.contains("entrenamiento")) return "EXERCISE";
		if (description.contains("deporte")) return "SPORTS";
		if (description.contains("vida social")) return "SOCIAL";
		if (description.contains("ocio")) return "ENTERTAINMENT";
		return null;
	}

	private boolean isDefaultRequirement(AchievementIndex index) {
		return "completed_tasks".equals(index.getRequirementType())
				&& (index.getRequirementTarget() == null || index.getRequirementTarget().compareTo(BigDecimal.ONE) == 0)
				&& !FEATURED_NAME_SET.contains(index.getName());
	}

	private String lowerDescription(AchievementIndex index) {
		return index.getDescription() == null ? "" : index.getDescription().toLowerCase(java.util.Locale.ROOT);
	}

	private long value(Long value) {
		return value == null ? 0 : value;
	}
}
