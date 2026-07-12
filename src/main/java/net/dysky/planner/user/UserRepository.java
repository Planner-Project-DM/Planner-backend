package net.dysky.planner.user;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

interface UserRepository extends CrudRepository<User, UUID> {
}
