package net.dysky.planner.usersettings;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface UserSettingsRepository extends JpaRepository<UserSettings, UUID> {
}
