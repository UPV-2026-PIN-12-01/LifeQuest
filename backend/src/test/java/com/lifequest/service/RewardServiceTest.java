package com.lifequest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.lifequest.model.entity.Reward;
import com.lifequest.model.entity.RewardSource;
import com.lifequest.model.entity.RewardSourceConverter;
import com.lifequest.repository.RewardRepository;

class RewardServiceTest {

	@Test
	void persistsRewardWithItsSource() {
		RewardRepository repository = mock(RewardRepository.class);
		when(repository.save(any(Reward.class))).thenAnswer(invocation -> {
			Reward saved = invocation.getArgument(0);
			return saved;
		});
		RewardService service = new RewardService(repository);

		service.create(7L, 30L, 5L, RewardSource.TASK);

		ArgumentCaptor<Reward> saved = ArgumentCaptor.forClass(Reward.class);
		verify(repository).save(saved.capture());
		assertThat(saved.getValue().getUserId()).isEqualTo(7L);
		assertThat(saved.getValue().getXpReward()).isEqualTo(30L);
		assertThat(saved.getValue().getGoldReward()).isEqualTo(5L);
		assertThat(saved.getValue().getSource()).isEqualTo(RewardSource.TASK);
	}

	@Test
	void mapsEveryDatabaseSourceValueExactly() {
		RewardSourceConverter converter = new RewardSourceConverter();

		for (RewardSource source : RewardSource.values()) {
			assertThat(converter.convertToDatabaseColumn(source)).isEqualTo(source.databaseValue());
			assertThat(converter.convertToEntityAttribute(source.databaseValue())).isEqualTo(source);
		}
	}

	@Test
	void persistsClaimedAtAndGrantedXpImmediately() {
		RewardRepository repository = mock(RewardRepository.class);
		Reward reward = new Reward();
		reward.setXpReward(30L);
		Instant claimedAt = Instant.parse("2026-11-05T15:00:00Z");
		RewardService service = new RewardService(repository);

		service.claim(reward, 35L, claimedAt);

		verify(repository).saveAndFlush(reward);
		assertThat(reward.getClaimedAt()).isEqualTo(claimedAt);
		assertThat(reward.getXpReward()).isEqualTo(35L);
	}

	@Test
	void updatesXpUnderLockWithoutClearingAnExistingClaimTimestamp() {
		RewardRepository repository = mock(RewardRepository.class);
		Reward reward = new Reward();
		Instant claimedAt = Instant.parse("2026-11-05T15:00:00Z");
		reward.setClaimedAt(claimedAt);
		when(repository.findByIdForUpdate(9L)).thenReturn(java.util.Optional.of(reward));
		RewardService service = new RewardService(repository);

		service.updateXp(9L, 40L, RewardSource.TASK);

		verify(repository).findByIdForUpdate(9L);
		assertThat(reward.getXpReward()).isEqualTo(40L);
		assertThat(reward.getClaimedAt()).isEqualTo(claimedAt);
	}
}
