package com.lifequest.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lifequest.model.entity.Friendship;
import com.lifequest.model.entity.FriendshipStatus;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

	// The row for a pair, whichever of the two sent the request.
	@Query("""
			select f from Friendship f
			where (f.requesterId = :a and f.addresseeId = :b) or (f.requesterId = :b and f.addresseeId = :a)
			""")
	Optional<Friendship> findBetween(@Param("a") Long a, @Param("b") Long b);

	@Query("""
			select f from Friendship f
			where f.status = :status and (f.requesterId = :userId or f.addresseeId = :userId)
			order by f.acceptedAt desc
			""")
	List<Friendship> findByUserAndStatus(@Param("userId") Long userId, @Param("status") FriendshipStatus status);

	List<Friendship> findByAddresseeIdAndStatusOrderByCreatedAtDesc(Long addresseeId, FriendshipStatus status);

	List<Friendship> findByRequesterIdAndStatusOrderByCreatedAtDesc(Long requesterId, FriendshipStatus status);
}
