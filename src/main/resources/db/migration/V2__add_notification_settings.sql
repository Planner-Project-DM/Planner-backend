ALTER TABLE user_settings
    ADD COLUMN notifications_enabled        BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN notification_channel         VARCHAR(20) NOT NULL DEFAULT 'PUSH',

    ADD COLUMN notify_friendship_request    BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN notify_friendship_removed    BOOLEAN NOT NULL DEFAULT TRUE,

    ADD COLUMN notify_schedule_item_added   BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN notify_schedule_item_updated BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN notify_schedule_item_deleted BOOLEAN NOT NULL DEFAULT TRUE,

    ADD COLUMN notify_group_member_added    BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN notify_group_member_removed  BOOLEAN NOT NULL DEFAULT TRUE,

    ADD COLUMN notify_fund_item_cost_updated BOOLEAN NOT NULL DEFAULT TRUE;