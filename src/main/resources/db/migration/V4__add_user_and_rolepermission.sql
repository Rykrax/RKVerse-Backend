INSERT IGNORE INTO users (username, password, email)
VALUES ("rykrax", "$2a$12$1A3cpob8amFNk2rZDQ2TP.vUvHON4GeVGW2fDAGLLyrYyP88lh3ne", "rykrax.ksnd.1122@gmail.com");

INSERT IGNORE INTO user_roles (user_id, role_id)
VALUES (1,1),
       (2,2);

INSERT IGNORE INTO role_permissions (role_id, permission_id)
VALUES (1,1),
       (1,2),
       (1,3),
       (1,4),
       (2,4)

