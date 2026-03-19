CREATE TABLE IF NOT EXISTS PRMT_PROMOTION
(
    id                              BIGINT          NOT NULL AUTO_INCREMENT,
    name                            VARCHAR(100)    NOT NULL,
    slogan                          VARCHAR(255)    NOT NULL,
    content_url                     VARCHAR(500)    NOT NULL,
    image_url                       VARCHAR(500)    NOT NULL,
    max_participation_count         INT             NOT NULL,
    accumulated_participation_count INT             NOT NULL DEFAULT 0,
    reward_amount                   BIGINT          NOT NULL,
    participation_start_at          DATETIME(6)     NOT NULL,
    participation_end_at            DATETIME(6)     NOT NULL,
    completion_policy               VARCHAR(20)     NOT NULL,
    content_deadline_days           INT,
    content_start_at                DATETIME(6),
    content_end_at                  DATETIME(6),
    created_at                      DATETIME(6)     NOT NULL,
    updated_at                      DATETIME(6),
    created_by                      BIGINT          NOT NULL DEFAULT 0,
    updated_by                      BIGINT                   DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS PRMT_PARTICIPATION_POLICY
(
    id                           BIGINT       NOT NULL AUTO_INCREMENT,
    promotion_id                 BIGINT       NOT NULL,
    policy_code                  VARCHAR(50)  NOT NULL,
    prerequisite_promotion_id    BIGINT,
    personal_participation_limit INT,
    created_at                   DATETIME(6)  NOT NULL,
    updated_at                   DATETIME(6),
    created_by                   BIGINT       NOT NULL DEFAULT 0,
    updated_by                   BIGINT                DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS PRMT_PARTICIPATION
(
    id                   BIGINT      NOT NULL AUTO_INCREMENT,
    promotion_id         BIGINT      NOT NULL,
    user_id              BIGINT      NOT NULL,
    participation_date   DATE        NOT NULL,
    participation_status VARCHAR(30) NOT NULL,
    reward_amount        BIGINT      NOT NULL,
    created_at           DATETIME(6) NOT NULL,
    updated_at           DATETIME(6),
    created_by           BIGINT      NOT NULL DEFAULT 0,
    updated_by           BIGINT               DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_participation (promotion_id, user_id, participation_date)
);

CREATE TABLE IF NOT EXISTS PRMT_PARTICIPATION_HISTORY
(
    id                   BIGINT      NOT NULL AUTO_INCREMENT,
    participation_id     BIGINT      NOT NULL,
    participation_status VARCHAR(30) NOT NULL,
    created_at           DATETIME(6) NOT NULL,
    updated_at           DATETIME(6),
    created_by           BIGINT      NOT NULL DEFAULT 0,
    updated_by           BIGINT               DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS PRMT_KAFKA_PUBLISH_FAILURE
(
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    promotion_id   BIGINT      NOT NULL,
    user_id        BIGINT      NOT NULL,
    participated_at DATETIME(6) NOT NULL,
    start_at       DATETIME(6) NOT NULL,
    status         VARCHAR(20) NOT NULL,
    retry_count    INT         NOT NULL DEFAULT 0,
    created_at     DATETIME(6) NOT NULL,
    updated_at     DATETIME(6),
    PRIMARY KEY (id)
);
