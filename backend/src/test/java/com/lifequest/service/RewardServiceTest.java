package com.lifequest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
}
