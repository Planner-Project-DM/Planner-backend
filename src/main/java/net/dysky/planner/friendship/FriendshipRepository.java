package net.dysky.planner.friendship;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface FriendshipRepository extends JpaRepository<Friendship, UUID> {
}
