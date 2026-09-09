INSERT INTO roles (name)
VALUES ("ADMIN"),
       ("USER");

INSERT INTO permissions (code, name, resource)
VALUES ("user.select", "Xem danh sách user", "USER"),
       ("user.create", "Thêm mới user", "USER"),
       ("user.delete", "Xóa user", "USER"),
       ("user.update", "Cập nhật user", "USER")