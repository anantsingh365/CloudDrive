Create Schema CloudDriveTest;

INSERT INTO `CloudDriveTest`.`users`
(`id`,
`email`,
`name`,
`password`)
VALUES
(1,
"test@gmail.com",
"anant",
"1234");

INSERT INTO `CloudDriveTest`.`users_roles`
(`user_id`,
`role_id`)
VALUES
(1,idusers_roles,
1);

INSERT INTO `CloudDriveTest`.`roles`
(`id`,
`name`)
VALUES
(1,
"ROLE_USER");
