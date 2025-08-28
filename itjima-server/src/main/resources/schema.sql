-- 사용자 테이블
CREATE TABLE IF NOT EXISTS `USERS`
(
    `id`                          bigint       NOT NULL AUTO_INCREMENT COMMENT '사용자ID',
    `name`                        varchar(64)  NOT NULL COMMENT '이름',
    `email`                       varchar(256) NOT NULL COMMENT '이메일',
    `password`                    varchar(256) NOT NULL COMMENT '비밀번호',
    `phone`                       varchar(32)  NOT NULL COMMENT '전화번호',
    `created_at`                  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '가입일',
    `updated_at`                  datetime              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    `provider`                    varchar(20)  NOT NULL DEFAULT 'LOCAL' COMMENT '가입경로',
    `provider_id`                 varchar(255)          DEFAULT NULL COMMENT 'SNS 고유ID',
    `status`                      varchar(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT '회원 상태',
    `deleted_at`                  datetime              DEFAULT NULL COMMENT '탈퇴일',
    `email_verified`              tinyint(1)   NOT NULL DEFAULT '0' COMMENT '이메일 인증 여부',
    `email_verification_token`    varchar(255)          DEFAULT NULL COMMENT '이메일 인증 토큰',
    `email_token_generated_at`    datetime              DEFAULT NULL COMMENT '이메일 인증 토큰 생성 시간',
    `password_reset_token`        varchar(255)          DEFAULT NULL COMMENT '비밀번호 재설정 토큰',
    `password_token_generated_at` datetime              DEFAULT NULL COMMENT '비밀번호 재설정 토큰 생성 시간',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='사용자'

  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='사용자';

-- 리프레시 토큰 테이블
CREATE TABLE IF NOT EXISTS `REFRESH_TOKENS`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`     bigint       NOT NULL COMMENT '사용자ID',
    `token`       varchar(512) NOT NULL COMMENT '리프레시 토큰',
    `expiry_date` datetime     NOT NULL COMMENT '만료일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    CONSTRAINT `fk_refresh_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `USERS` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='리프레시 토큰';

-- 대여품목 테이블
CREATE TABLE IF NOT EXISTS `ITEMS`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '대여품목ID',
    `user_id`     bigint       NOT NULL COMMENT '품목 등록자ID',
    `type`        varchar(8)   NOT NULL COMMENT '구분 (MONEY, OBJECT)',
    `title`       varchar(100) NOT NULL COMMENT '품목명',
    `description` text         NULL COMMENT '설명',
    `status`      varchar(20)  NOT NULL DEFAULT 'AVAILABLE',
    `file_url`    varchar(2048) COMMENT '파일 경로',
    `file_type`   varchar(100) COMMENT '파일 타입',
    `created_at`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_items_user` FOREIGN KEY (`user_id`) REFERENCES `USERS` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='대여품목';

-- 대여 계약 테이블
CREATE TABLE IF NOT EXISTS `AGREEMENTS`
(
    `id`         bigint         NOT NULL AUTO_INCREMENT COMMENT '대여ID',
    `item_id`    bigint         NULL COMMENT '대여품목ID (금전 거래 시 NULL 가능)',
    `status`     varchar(20)    NOT NULL COMMENT '상태',
    `amount`     decimal(10, 0) NULL COMMENT '금액',
    `due_at`     datetime       NOT NULL COMMENT '반납예정일',
    `terms`      text           NULL COMMENT '메모',
    `created_at` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `updated_at` datetime       NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_agreements_item` FOREIGN KEY (`item_id`) REFERENCES `ITEMS` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='대여 계약';

-- 대여 참여자 테이블
CREATE TABLE IF NOT EXISTS `AGREEMENT_PARTIES`
(
    `id`           bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `agreement_id` bigint      NOT NULL COMMENT '대여ID',
    `user_id`      bigint      NOT NULL COMMENT '사용자ID',
    `role`         varchar(10) NOT NULL COMMENT '역할 (LENDER, BORROWER)',
    `confirm_at`   datetime    NULL COMMENT '수락/확인 일시',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_agreement_parties_agreement` FOREIGN KEY (`agreement_id`) REFERENCES `AGREEMENTS` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_agreement_parties_user` FOREIGN KEY (`user_id`) REFERENCES `USERS` (`id`),
    UNIQUE KEY `uk_agreement_user` (`agreement_id`, `user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='대여 참여자';

-- 상환/거래 기록 테이블
CREATE TABLE IF NOT EXISTS `TRANSACTIONS`
(
    `id`           bigint         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `agreement_id` bigint         NOT NULL COMMENT '대여ID',
    `type`         varchar(10)    NOT NULL COMMENT '거래유형 (REPAYMENT)',
    `amount`       decimal(10, 0) NOT NULL COMMENT '거래금액',
    `status`       varchar(20)    NOT NULL COMMENT '상태 (PENDING, CONFIRMED, REJECTED)',
    `created_at`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '기록일',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_transactions_agreement` FOREIGN KEY (`agreement_id`) REFERENCES `AGREEMENTS` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='상환/거래 기록';

-- 일정/리마인드 테이블
CREATE TABLE IF NOT EXISTS `SCHEDULES`
(
    `id`            bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `agreement_id`  bigint      NOT NULL COMMENT '대여ID',
    `schedule_type` varchar(10) NOT NULL COMMENT '일정 유형 (D-3, D-1, D-DAY)',
    `due_at`        datetime    NOT NULL COMMENT '예정일',
    `notified`      tinyint(1)  NOT NULL DEFAULT '0' COMMENT '알림발송여부',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_schedules_agreement` FOREIGN KEY (`agreement_id`) REFERENCES `AGREEMENTS` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='일정/리마인드';

-- 알림 테이블
CREATE TABLE IF NOT EXISTS `NOTIFICATIONS`
(
    `id`           bigint        NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `agreement_id` bigint        NOT NULL COMMENT '대여ID',
    `user_id`      bigint        NOT NULL COMMENT '수신자ID',
    `type`         varchar(20)   NOT NULL COMMENT '알림유형',
    `message`      varchar(1024) NOT NULL COMMENT '메시지',
    `read_at`      datetime      NULL COMMENT '읽은시각',
    `created_at`   datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성시각',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_notifications_agreement` FOREIGN KEY (`agreement_id`) REFERENCES `AGREEMENTS` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_notifications_user` FOREIGN KEY (`user_id`) REFERENCES `USERS` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='알림';

-- 활동로그 테이블
CREATE TABLE IF NOT EXISTS `AUDIT_LOGS`
(
    `id`           bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `agreement_id` bigint      NOT NULL COMMENT '대여ID',
    `user_id`      bigint      NOT NULL COMMENT '사용자ID',
    `action`       varchar(30) NOT NULL COMMENT '행동',
    `detail`       text        NULL COMMENT '세부내용',
    `created_at`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '기록시각',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_audit_logs_agreement` FOREIGN KEY (`agreement_id`) REFERENCES `AGREEMENTS` (`id`),
    CONSTRAINT `fk_audit_logs_user` FOREIGN KEY (`user_id`) REFERENCES `USERS` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='활동로그';

-- 태그 테이블
CREATE TABLE IF NOT EXISTS `TAGS`
(
    `id`         bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`    bigint      NOT NULL COMMENT '사용자ID',
    `name`       varchar(32) NOT NULL COMMENT '이름',
    `created_at` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_tags_user` FOREIGN KEY (`user_id`) REFERENCES `USERS` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_user_name` (`user_id`, `name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='태그';

-- 태그-대여 매핑 테이블
CREATE TABLE IF NOT EXISTS `AGREEMENT_TAGS`
(
    `agreement_id` bigint NOT NULL COMMENT '대여ID',
    `tag_id`       bigint NOT NULL COMMENT '태그ID',
    PRIMARY KEY (`agreement_id`, `tag_id`),
    CONSTRAINT `fk_agreement_tags_agreement` FOREIGN KEY (`agreement_id`) REFERENCES `AGREEMENTS` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_agreement_tags_tag` FOREIGN KEY (`tag_id`) REFERENCES `TAGS` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='태그-대여';