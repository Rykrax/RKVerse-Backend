package dev.rykrax.rkverse.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate stringRedisTemplate;

    public void set(String key, String value, Duration duration) {
        stringRedisTemplate.opsForValue().set(key, value, duration);
    }

    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * Chống spam: SET key value EX timeout NX (Atomic)
     * Trả về true nếu lock thành công (chưa từng request trong khoảng timeout)
     * Trả về false nếu đang trong thời gian cooldown
     */
    public Boolean setIfAbsent(String key, String value, Duration duration) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key, value, duration));
    }

    /**
     * Kiểm tra Key có tồn tại trên Redis hay không
     */
    public Boolean hasKey(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    /**
     * Xóa một Key bất kỳ
     */
    public Boolean delete(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.delete(key));
    }

    /**
     * Thiết lập hoặc gia hạn thời gian sống (TTL) cho một Key
     */
    public Boolean expire(String key, Duration duration) {
        return Boolean.TRUE.equals(stringRedisTemplate.expire(key, duration));
    }

    // ==========================================
    // 2. CÁC THAO TÁC VỚI HASH (GOM VIEW TẠM)
    // ==========================================

    /**
     * Tăng số lượng view cho một thực thể trong Hash (HINCRBY - Atomic)
     * @param hashKey Tên Hash (vd: "chapter:views", "comic:views")
     * @param field   Khóa phụ (vd: chapterId, comicId)
     * @param delta   Số lượng tăng thêm (thường là 1)
     */
    public Long hashIncrement(String hashKey, String field, long delta) {
        return stringRedisTemplate.opsForHash().increment(hashKey, field, delta);
    }

    /**
     * Lấy số view tạm thời của 1 item trong Hash
     */
    public Long getHashViewCount(String hashKey, String field) {
        Object val = stringRedisTemplate.opsForHash().get(hashKey, field);
        return val != null ? Long.parseLong(val.toString()) : 0L;
    }

    /**
     * Lấy toàn bộ danh sách view đang chờ đồng bộ trong Hash
     * Trả về Map<String, String> để dễ xử lý vòng lặp
     */
    public Map<Object, Object> getHashEntries(String hashKey) {
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(hashKey);
        return entries != null ? entries : Collections.emptyMap();
    }

    // ==========================================
    // 3. ATOMIC ROTATE (ĐỒNG BỘ THEO LÔ - BATCH SYNC)
    // ==========================================

    /**
     * Đổi tên Key nguyên tử (Atomic Rename) để đóng băng mẻ view cần sync xuống DB.
     * Trả về false nếu key nguồn không tồn tại (trong 10p không có view mới).
     */
    public boolean renameSafely(String oldKey, String newKey) {
        try {
            stringRedisTemplate.rename(oldKey, newKey);
            return true;
        } catch (Exception e) {
            // Redis ném ngoại lệ nếu oldKey không tồn tại
            return false;
        }
    }
}