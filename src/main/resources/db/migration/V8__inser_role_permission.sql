INSERT IGNORE INTO permissions (code, name, resource)
VALUES ("comic.select", "Xem danh sách comic", "COMIC"),
       ("comic.create", "Tạo mới comic", "COMIC"),
       ("comic.delete", "Xóa comic", "COMIC"),
       ("comic.selectDetail", "Xem chi tiết comic", "COMIC"),
       ("comic.update", "Cập nhật comic", "COMIC");

INSERT IGNORE INTO role_permissions (role_id, permission_id)
VALUES (1,5),
       (1,6),
       (1,7),
       (1,8),
       (1,9),
       (2,5),
       (2,8);